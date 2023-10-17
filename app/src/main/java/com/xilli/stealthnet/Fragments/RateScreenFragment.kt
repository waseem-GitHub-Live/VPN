package com.xilli.stealthnet.Fragments

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.net.TrafficStats
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bumptech.glide.Glide
import com.xilli.stealthnet.Activities.MainActivity.Companion.selectedCountry
import com.xilli.stealthnet.Activities.Work
import com.xilli.stealthnet.R
import com.xilli.stealthnet.databinding.FragmentRateScreenBinding
import com.xilli.stealthnet.helper.Utils.showIP
import com.xilli.stealthnet.helper.Utils.updateUI
import com.xilli.stealthnet.model.Countries
import com.xilli.stealthnet.Fragments.viewmodels.SharedViewmodel
import com.xilli.stealthnet.Utils.ActiveServer
import com.xilli.stealthnet.helper.Utils
import com.xilli.stealthnet.helper.Utils.findViewById
import com.xilli.stealthnet.helper.Utils.sharedPreferences
import top.oneconnectapi.app.core.OpenVPNThread
import java.util.Objects
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class RateScreenFragment : Fragment() {
    private var elapsedTimeMillis: Long = 0
    private var rxBytes: Long = 0
    private var txBytes: Long = 0
    private var binding: FragmentRateScreenBinding? = null
    private val mHandler = Handler()
    private var mStartRX: Long = 0
    private var mStartTX: Long = 0
    private var isFirst = true
    private var backPressedOnce = false
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var countdownValue = 4
    private var countDownTimer: CountDownTimer? = null
    private var countryName: String? = null
    private var flagUrl: String? = null
    private val viewModel by viewModels<SharedViewmodel>()
    private var dataUsageBeforeFragment: Long = 0
    private var dataUsageInFragment: Long = 0
    private var savedTime: Long = 0L
    private val handler = Handler()
    private var timeRemainingMillis: Long = 1800000
    private var updateTimeRunnable: Runnable? = null
    private var countdownTimer: CountDownTimer? = null
    private val workManager by lazy {
        WorkManager.getInstance(requireContext())
    }
    private var workId: UUID? = null
    private var timerResetNeeded = false
    companion object {
        var type = ""
        const val KEY_REMAINING_TIME = "remaining_time"
    }
    private lateinit var disconnectButton: TextView
    var mactivity: FragmentActivity? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mactivity = requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        mactivity = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRateScreenBinding.inflate(inflater, container, false)
        binding?.lifecycleOwner = viewLifecycleOwner
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val selectedCountryName = sharedPrefs.getString("selectedCountryName", null)
        val selectedCountryFlagUrl = sharedPrefs.getString("selectedCountryFlagUrl", null)
        if (selectedCountryName != null && selectedCountryFlagUrl != null) {
            val selectedItem = Countries(selectedCountryName, selectedCountryFlagUrl, "", "", "")
            viewModel.selectedItem.value = selectedItem
        }
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadcastReceiver, IntentFilter("connectionState"))

        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mactivity.let {
            starttime()
            clicklistner()
            setupBackPressedCallback()
            startRunnable()
            updateTrafficStats()
            datasheet()
            binding?.vpnIp?.let { vpnIp ->
                showIP(requireContext(), vpnIp)
                handler.postDelayed({
                    saveVPNIP(vpnIp.text.toString())
                }, 5000)
            }

        }
    }
    private fun starttime() {
        countdownTimer = object : CountDownTimer(timeRemainingMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
            }
        }.start()
    }
    private fun resetTimer() {
        timeRemainingMillis = 1800000
        updateTimerText()
        countdownTimer?.cancel()
    }

    private fun updateTimerText() {
        val minutes = (timeRemainingMillis / 60000).toInt()
        val seconds = ((timeRemainingMillis % 60000) / 1000).toInt()
        binding?.timeline?.text = String.format("%02d:%02d", minutes, seconds)
    }
    override fun onDestroy() {
        super.onDestroy()
        dataUsageInFragment =
            (TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()) - dataUsageBeforeFragment
        val editor = sharedPreferences.edit()
        editor.putLong("dataUsageInFragment", dataUsageInFragment)
        editor.apply()

    }


    private fun datasheet() {
        binding?.flagimageView?.let {
            Glide.with(this)
                .load(selectedCountry?.flagUrl)
                .into(it)
        }
        binding?.flagName?.text = selectedCountry?.country
    }

    private fun saveVPNIP(vpnIP: String) {
        if (mactivity != null) {
            val sharedPreferences: SharedPreferences =
                mactivity!!.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("vpnIP", vpnIP)
            editor.apply()
        }
    }

    private fun startRunnable() {
        mRunnable.run()
    }

    private fun updateTrafficStats() {
        val totalDataUsage = calculateTotalDataUsage()
        val resetDownload = TrafficStats.getTotalRxBytes()
        val rxBytes = TrafficStats.getTotalRxBytes() - mStartRX
        val formattedRx = formatBytes(rxBytes)
        binding?.textView4?.text = formattedRx
        mStartRX = resetDownload
        val resetUpload = TrafficStats.getTotalTxBytes()
        val txBytes = TrafficStats.getTotalTxBytes() - mStartTX
        val formattedTx = formatBytes(txBytes)
        binding?.uploaddata?.text = formattedTx
        mStartTX = resetUpload
        val avgRxSpeed = calculateAverageSpeed(mStartRX, rxBytes)
        val avgTxSpeed = calculateAverageSpeed(mStartTX, txBytes)
        saveTrafficStats(totalDataUsage, avgRxSpeed, avgTxSpeed)
    }

    private fun saveTrafficStats(totalDataUsage: String, avgRxSpeed: String, avgTxSpeed: String) {
        val sharedPreferences =
            requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("totalDataUsage", totalDataUsage)
        editor.putString("averageRxSpeed", avgRxSpeed)
        editor.putString("averageTxSpeed", avgTxSpeed)
        editor.apply()
    }


    private fun formatBytes(bytes: Long): String {
        val kilo = 1024
        val mega = kilo * kilo
        val giga = mega * kilo

        return when {
            bytes < kilo -> "$bytes B"
            bytes < mega -> String.format("%.2f KB", bytes.toDouble() / kilo)
            bytes < giga -> String.format("%.2f MB", bytes.toDouble() / mega)
            else -> String.format("%.2f GB", bytes.toDouble() / giga)
        }
    }

    private fun calculateAverageSpeed(startBytes: Long, currentBytes: Long): String {
        val bytesTransferred = abs(currentBytes - startBytes)

        return when {
            bytesTransferred < 1024 -> "$bytesTransferred B/s"
            bytesTransferred < 1024 * 1024 -> "${bytesTransferred / 1024} KB/s"
            bytesTransferred < 1024 * 1024 * 1024 -> "${bytesTransferred / (1024 * 1024)} MB/s"
            else -> "${bytesTransferred / (1024 * 1024 * 1024)} GB/s"
        }
    }

    private fun calculateTotalDataUsage(): String {
        val totalRxBytes = TrafficStats.getTotalRxBytes()
        val totalTxBytes = TrafficStats.getTotalTxBytes()

        val totalBytes = totalRxBytes + totalTxBytes
        return formatDataUsage(totalBytes)
    }

    private fun formatDataUsage(bytes: Long): String {
        val kilobytes = bytes / 1024
        val megabytes = kilobytes / 1024
        val gigabytes = megabytes / 1024

        return when {
            gigabytes > 0 -> "$gigabytes GBs"
            megabytes > 0 -> "$megabytes MBs"
            kilobytes > 0 -> "$kilobytes KBs"
            else -> "$bytes Bytes"
        }
    }

    private fun setupBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            private var isDisconnecting = false

            override fun handleOnBackPressed() {
                if (!backPressedOnce && !isDisconnecting) {
                    backPressedOnce = true
                    isDisconnecting = true
                    disconnectmethod()
                } else if (backPressedOnce && !isDisconnecting) {
                    requireActivity().onBackPressed()
                    disconnectmethod()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    private fun alertdialog(dialogInterface: DialogInterface) {
        val alertSheetDialog = dialogInterface as AlertDialog
        val alertdialog = alertSheetDialog.findViewById<View>(
            com.google.android.material.R.id.alertTitle
        )
            ?: return
        alertdialog.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun clicklistner() {
        binding?.menu?.setOnClickListener {
            val drawerLayout =
                requireActivity().findViewById<DrawerLayout>(R.id.constraintlayoutmenu)
            drawerLayout.openDrawer(GravityCompat.START)
        }
        binding?.premimunbutton2?.setOnClickListener {
            findNavController().navigate(RateScreenFragmentDirections.actionRateScreenFragmentToPremiumFragment())
        }
        binding?.crosscancel?.setOnClickListener {
            disconnectmethod()
        }
        binding?.constraintLayout2details?.setOnClickListener {
            val action = RateScreenFragmentDirections.actionRateScreenFragmentToServerListFragment()
            findNavController().navigate(action)
        }
        binding?.navigationView?.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings_menu -> {
                    findNavController().navigate(RateScreenFragmentDirections.actionRateScreenFragmentToSettingFragment())
                }

                R.id.server_menu -> {
                    findNavController().navigate(RateScreenFragmentDirections.actionRateScreenFragmentToServerListFragment())
                }

                R.id.split_menu -> {
                    findNavController().navigate(RateScreenFragmentDirections.actionRateScreenFragmentToSplitTunningFragment2())
                }
            }
            true
        }
    }

    private fun saveDisconnectData(
        duration: String,
        lastPacketReceive: String,
        byteIn: String,
        byteOut: String,
        sumBytes: Long
    ) {
        val sharedPreferences =
            requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putString("duration", duration)
        editor.putString("lastPacketReceive", lastPacketReceive)
        editor.putString("byteIn", byteIn)
        editor.putString("byteOut", byteOut)
        editor.putLong("sum", sumBytes) // Store sumBytes as a Long
        editor.apply()
    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            try {
                val vpnState = intent.getStringExtra("state")
                if (vpnState != null) {
                    if (vpnState == "CONNECTED") {
                        // VPN is connected
                        Utils.updateUI("connected") // You can update UI accordingly
                        Utils.isConnected = true

                        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToRateScreenFragment())

                    } else if (vpnState == "DISCONNECTED") {
                        // VPN is disconnected
                        Utils.updateUI("disconnected") // You can update UI accordingly
                        Utils.isConnected = false
                    }
                    Log.v("yoo", vpnState)
                }

                Objects.requireNonNull(Utils.getIntent().getStringExtra("state")).let {
                    if (it != null) {
                        Utils.updateUI(it)

                    }
                }
                Objects.requireNonNull(intent.getStringExtra("state"))
                    .let {
                        if (it != null) {
                            Log.v("CHECKSTATE", it)

                        }
                    }
                if (isFirst) {
                    if (ActiveServer.getSavedServer(requireContext()).country != null) {
                        selectedCountry = ActiveServer.getSavedServer(requireContext())
                        Utils.imgFlag?.let {
                            Glide.with(requireContext())
                                .load(selectedCountry?.flagUrl)
                                .into(it)
                        }
                        Utils.flagName?.text = selectedCountry?.country
                    }
                    isFirst = false
                }
                Utils.isConnected = true


            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                var duration = intent.getStringExtra("duration")
                var lastPacketReceive = intent.getStringExtra("lastPacketReceive")
                var byteIn = intent.getStringExtra("byteIn")
                var byteOut = intent.getStringExtra("byteOut")
                if (duration == null) duration = "00:00:00"
                if (lastPacketReceive == null) lastPacketReceive = "0"
                if (byteIn == null) byteIn = " "
                if (byteOut == null) byteOut = " "
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut)
                viewModel.lastPacketReceivedLiveData.value = lastPacketReceive
                viewModel.durationLiveData.value = duration
                viewModel.byteInLiveData.value = byteIn
                viewModel.byteOutLiveData.value = byteOut
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun disconnectmethod() {

        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cancel_vpn, null)
        val dialog = AlertDialog.Builder(requireContext(), R.style.TransparentAlertDialogTheme)
            .setView(dialogView)
            .create()
        alertdialog(dialog)
        dialog.show()

        val cancelTextView = dialogView.findViewById<TextView>(R.id.cancel)
        cancelTextView.setOnClickListener {
            dialog.dismiss()
        }

        val disconnectTextView = dialogView.findViewById<TextView>(R.id.disconnct)
        disconnectTextView.text = getString(R.string.disconnect_timer_initial)


        val originalDisconnectBackground = disconnectTextView.background
        val originalDisconnectTextColor = disconnectTextView.currentTextColor

        var remainingTime = countdownValue
        countDownTimer = object : CountDownTimer((countdownValue * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime--
                disconnectTextView.text = "Disconnect ($remainingTime s)"
                // Change the background color and text color here
                val disconnectViewColor =
                    ContextCompat.getColor(requireContext(), R.color.disconnectview)
                disconnectTextView.setBackgroundResource(R.drawable.disconnect_timer_drawable)
                disconnectTextView.setTextColor(disconnectViewColor)// Example: Change text color to white
            }

            override fun onFinish() {
                disconnectTextView.setOnClickListener {
//                    workId?.let {
//                        workManager.cancelWorkById(it)
//                    }
//                    workManager.cancelAllWork()
//                    workId=null
                    resetTimer()
                    val duration = viewModel.durationLiveData.value ?: "00:00:00"
                    val lastPacketReceive = viewModel.lastPacketReceivedLiveData.value ?: "0"
                    val byteIn = viewModel.byteInLiveData.value ?: " "
                    val byteOut = viewModel.byteOutLiveData.value ?: " "
                    val byteInValue = convertToBytes(byteIn)
                    val byteOutValue = convertToBytes(byteOut)
                    val sumBytes = byteInValue + byteOutValue
                    saveDisconnectData(duration, lastPacketReceive, byteIn, byteOut, sumBytes)
                    findNavController().navigate(RateScreenFragmentDirections.actionRateScreenFragmentToReportScreenFragment())
                    disconnectFromVpn()
                    dialog.dismiss()
                }

                disconnectTextView.background = originalDisconnectBackground
                disconnectTextView.setTextColor(originalDisconnectTextColor)
                disconnectTextView.text = getString(R.string.disconnect_timer_initial)
            }
        }
        countDownTimer?.start()
    }

    fun disconnectFromVpn() {
        try {
            OpenVPNThread.stop()
            updateUI("DISCONNECTED")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private val mRunnable: Runnable = object : Runnable {
        override fun run() {
            updateTrafficStats()
            mHandler.postDelayed(this, 1000)
        }
    }

    override fun onDestroyView() {
        mHandler.removeCallbacks(mRunnable)
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
        super.onDestroyView()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the data you want to restore later
        outState.putLong("elapsedTimeMillis", elapsedTimeMillis)
        outState.putLong("rxBytes", rxBytes)
        outState.putLong("txBytes", txBytes)
        outState.putString("countryName", countryName)
        outState.putString("flagUrl", flagUrl)
        val viewModel = ViewModelProvider(requireActivity())[SharedViewmodel::class.java]
        outState.putLong(KEY_REMAINING_TIME, viewModel.getRemainingTime())
    }

    fun updateConnectionStatus(
        duration: String?,
        lastPacketReceive: String?,
        byteIn: String,
        byteOut: String
    ) {
        val byteinKb = byteIn.split("-").toTypedArray()[1]
        val byteoutKb = byteOut.split("-").toTypedArray()[1]

        Utils.textDownloading?.text = byteinKb
        Utils.textUploading?.text = byteoutKb
        Utils.timerTextView?.text = duration
    }

    fun convertToBytes(byteString: String): Long {
        val regex = Regex("([0-9.]+)\\s*([A-Za-z]+)")
        val match = regex.find(byteString)

        if (match != null) {
            val value = match.groupValues[1].toDouble()
            val unit = match.groupValues[2].toLowerCase()

            return when (unit) {
                "b" -> value.toLong()
                "kb" -> (value * 1024).toLong()
                "mb" -> (value * 1024 * 1024).toLong()
                "gb" -> (value * 1024 * 1024 * 1024).toLong()
                else -> 0L // Unsupported unit, handle accordingly
            }
        }
        return 0L // Parsing failed, handle accordingly
    }
}

