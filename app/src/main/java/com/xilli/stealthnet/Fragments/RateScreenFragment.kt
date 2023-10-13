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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.xilli.stealthnet.Activities.CountDownService
import com.xilli.stealthnet.Activities.MainActivity.Companion.selectedCountry
import com.xilli.stealthnet.Activities.SharedPreferencesHelper
import com.xilli.stealthnet.R
import com.xilli.stealthnet.databinding.FragmentRateScreenBinding
import com.xilli.stealthnet.helper.Utils.showIP
import com.xilli.stealthnet.helper.Utils.updateUI
import com.xilli.stealthnet.model.Countries
import com.xilli.stealthnet.Fragments.viewmodels.SharedViewmodel
import com.xilli.stealthnet.helper.Utils.handler
import com.xilli.stealthnet.helper.Utils.sharedPreferences
import top.oneconnectapi.app.core.OpenVPNThread
import kotlin.math.abs

class RateScreenFragment : Fragment() {
    private var elapsedTimeMillis: Long = 0
    private var rxBytes: Long = 0
    private var txBytes: Long = 0
    private var binding: FragmentRateScreenBinding? = null
    private val mHandler = Handler()
    private var mStartRX: Long = 0
    private var mStartTX: Long = 0
    private var backPressedOnce = false
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var countdownValue = 4
    private var countDownTimer: CountDownTimer? = null
    private var countryName: String? = null
    private var flagUrl: String? = null
    private var isTimerRunning = false
    private val viewModel by viewModels<SharedViewmodel>()
    private var dataUsageBeforeFragment: Long = 0
    private var dataUsageInFragment: Long = 0
    private var dataUsageInFragmentnew: Long = 0
    private var lastMeasurementTime: Long = System.currentTimeMillis()
    private var savedTime: Long = 0L
    companion object {
        var type = ""
        const val KEY_REMAINING_TIME = "remaining_time"
    }
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val timeLeft = intent?.getLongExtra("time_left", 0)
            updateCountdownTextView(timeLeft)
        }
    }
    var mactivity : FragmentActivity?=null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mactivity=requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        mactivity=null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
        val filter = IntentFilter("COUNTDOWN_TICK")
        requireContext().registerReceiver(receiver, filter)
        if (isTimerRunning) {
            val serviceIntent = Intent(requireContext(), CountDownService::class.java)
            requireContext().stopService(serviceIntent)
        }
        isTimerRunning = true

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
//        dataUsageBeforeFragment = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()
        val serviceIntent = Intent(requireContext(), CountDownService::class.java)
        requireContext().startService(serviceIntent)
//        datausage()


        return binding?.root
    }
    override fun onPause() {
        super.onPause()
        // Save the current time when leaving the fragment
        savedTime = System.currentTimeMillis()
    }
    override fun onResume() {
        super.onResume()
        // Get the current time when returning to the fragment
        val currentTime = System.currentTimeMillis()

        // Calculate the time elapsed
        val timeElapsed = currentTime - savedTime

        // You can now use the 'timeElapsed' value as needed, e.g., display it in a TextView
        val timeElapsedInSeconds = timeElapsed / 1000

        // Save the time elapsed in SharedPreferences
        sharedPreferences.edit().putLong("timeElapsedInSeconds", timeElapsedInSeconds).apply()
    }
//    private fun datausage() {
//        val currentTime = System.currentTimeMillis()
//
//        // Calculate the time elapsed
//        val timeElapsed = currentTime - savedTime
//
//        // You can now use the 'timeElapsed' value as needed, e.g., display it in a TextView
//        val timeElapsedInSeconds = timeElapsed / 1000
//
//        // Save the time elapsed in SharedPreferences
//        sharedPreferences.edit().putLong("timeElapsedInSeconds", timeElapsedInSeconds).apply()
//
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mactivity.let {
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

    override fun onStart() {
        super.onStart()


    }
    private fun data() {
        val savedDataUsage = sharedPreferences.getLong("timeElapsedInSeconds", 0L)
        if (savedDataUsage > 0) {
            mStartRX = TrafficStats.getTotalRxBytes()
            mStartTX = TrafficStats.getTotalTxBytes()

            dataUsageInFragmentnew =
                (TrafficStats.getTotalRxBytes() - mStartRX) + (TrafficStats.getTotalTxBytes() - mStartTX)

            val downloadSpeed =
                ((TrafficStats.getTotalRxBytes() - mStartRX) /savedDataUsage.toFloat())
            val uploadSpeed =
                ((TrafficStats.getTotalTxBytes() - mStartTX)/savedDataUsage.toFloat())

            val editor = sharedPreferences.edit()
            editor.putLong("dataUsageInFragment", dataUsageInFragmentnew)
            editor.putFloat("downloadSpeed", downloadSpeed)
            editor.putFloat("uploadSpeed", uploadSpeed)
            editor.apply()
        } else {
            val editor = sharedPreferences.edit()
            editor.putLong("dataUsageInFragment", 0L)
            editor.putFloat("downloadSpeed", 0.0f)
            editor.putFloat("uploadSpeed", 0.0f)
            editor.apply()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        val serviceIntent = Intent(requireContext(), CountDownService::class.java)
        requireContext().stopService(serviceIntent)

        requireContext().unregisterReceiver(receiver)
        dataUsageInFragment = (TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()) - dataUsageBeforeFragment
        val editor = sharedPreferences.edit()
        editor.putLong("dataUsageInFragment", dataUsageInFragment)
        editor.apply()

    }



    private fun updateCountdownTextView(timeLeft: Long?) {
        val textViewCountdown = view?.findViewById<TextView>(R.id.timeline)
        if (textViewCountdown != null && timeLeft != null) {
            val minutes = (timeLeft / 1000) / 60
            val seconds = (timeLeft / 1000) % 60
            val countdownText = String.format("%02d:%02d", minutes, seconds)
            textViewCountdown.text = countdownText
        }
    }
    private fun datasheet() {
        binding?.flagimageView?.let {
            Glide.with(this)
                .load(selectedCountry?.flagUrl)
                .into(it)
        }
        binding?.flagName?.text = selectedCountry?.country
    }

    private fun saveVPNIP(vpnIP: String) {if (mactivity!=null) {
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
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
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
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
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
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.constraintlayoutmenu)
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

    private fun disconnectmethod() {
        data()

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
                val disconnectViewColor = ContextCompat.getColor(requireContext(), R.color.disconnectview)
                disconnectTextView.setBackgroundResource(R.drawable.disconnect_timer_drawable)
                disconnectTextView.setTextColor(disconnectViewColor)// Example: Change text color to white
            }

            override fun onFinish() {

                disconnectTextView.setOnClickListener {

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

}

