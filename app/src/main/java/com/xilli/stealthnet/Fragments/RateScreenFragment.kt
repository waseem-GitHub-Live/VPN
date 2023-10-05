package com.xilli.stealthnet.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.net.TrafficStats
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.xilli.stealthnet.Activities.Utility
import com.xilli.stealthnet.R
import com.xilli.stealthnet.Utils.ActiveServer
import com.xilli.stealthnet.databinding.FragmentRateScreenBinding
import com.xilli.stealthnet.helper.Utils
import com.xilli.stealthnet.helper.Utils.showIP
import com.xilli.stealthnet.helper.Utils.updateUI
import com.xilli.stealthnet.model.Countries
import com.xilli.stealthnet.ui.viewmodels.SharedViewmodel
import top.oneconnectapi.app.core.OpenVPNThread
import kotlin.math.abs


class RateScreenFragment : Fragment(){
    private var elapsedTimeMillis: Long = 0
    private var rxBytes: Long = 0
    private var txBytes: Long = 0
    private var binding: FragmentRateScreenBinding? = null
    private val mHandler = Handler()
    private var startTimeMillis: Long = 0
    private var mStartRX: Long = 0
    private var mStartTX: Long = 0
    private var backPressedOnce = false
    private var isVpnStarted = false
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var countdownValue = 4
    private var countDownTimer: CountDownTimer? = null
    private var viewModel: SharedViewmodel?=null
    private val handler = Handler(Looper.getMainLooper())
    var selectedCountry: Countries? = null
    private var isFirst = true
    private var countryName: String? = null
    private var flagUrl: String? = null
    val countryName1 = Utils.countryName
    val flagUrl1 = Utils.flagUrl
    companion object {
        var type = ""
        val activeServer = ActiveServer()
        var STATUS = "DISCONNECTED"
    }
    @JvmField
    var flagName: TextView? = null
    @JvmField
    var imgFlag: ImageView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRateScreenBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[SharedViewmodel::class.java]
        binding?.lifecycleOwner = viewLifecycleOwner
        if (savedInstanceState != null) {
            // Restore saved data
            elapsedTimeMillis = savedInstanceState.getLong("elapsedTimeMillis", 0)
            rxBytes = savedInstanceState.getLong("rxBytes", 0)
            txBytes = savedInstanceState.getLong("txBytes", 0)
            countryName = savedInstanceState.getString("countryName")
            flagUrl = savedInstanceState.getString("flagUrl")
        }

        // Populate UI elements with restored data
        binding?.timeline?.text = formatElapsedTime(elapsedTimeMillis)
        binding?.textView4?.text = formatBytes(rxBytes)
        binding?.uploaddata?.text = formatBytes(txBytes)
        binding?.flagName?.text = countryName
        if (flagUrl != null) {
            Glide.with(this)
                .load(flagUrl)
                .into(binding?.flagimageView!!)
        }
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val storedStartTime = sharedPreferences.getLong("startTime", 0)
        clicklistner()
        setupBackPressedCallback()
        startRunnable()
        updateTrafficStats()
        datasheet()
        binding?.vpnIp?.let { showIP(requireContext(),it) }
        terraformed()

    }

    private fun terraformed() {
        viewModel?.startTimer()
        mHandler.post(object : Runnable {
            override fun run() {
                val elapsedTimeMillis = viewModel?.getElapsedTime()
                val elapsedTimeFormatted = elapsedTimeMillis?.let { formatElapsedTime(it) }
                binding?.timeline?.text = elapsedTimeFormatted
                mHandler.postDelayed(this, 1000)
            }
        })
    }
    private fun formatElapsedTime(elapsedTimeMillis: Long): String {
        val seconds = (elapsedTimeMillis / 1000) % 60
        val minutes = (elapsedTimeMillis / (1000 * 60)) % 60
        val hours = (elapsedTimeMillis / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    private fun datasheet() {
        if (countryName1 != null && flagUrl1 != null) {
            binding?.flagimageView?.let {
                Glide.with(this)
                    .load(flagUrl1)
                    .into(it)
            }
            binding?.flagName?.text = countryName1
        }
    }
    override fun onResume() {
        super.onResume()

        // Check if the vpnService reference is not null
//        vpnService?.updateStartTime(System.currentTimeMillis())
    }

    private fun startRunnable() {
        mRunnable.run()
    }
    private fun updateTrafficStats() {
        val totalDataUsage = calculateTotalDataUsage()

        viewModel?.totalDataUsage1 = totalDataUsage

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

        viewModel?.setAverageRxSpeed(avgRxSpeed)
        viewModel?.setAverageTxSpeed(avgTxSpeed)
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
                    val bundle = Bundle()
                    viewModel?.stopTimer()
                    bundle.putString("elapsedTime", binding?.timeline?.text.toString())
                    findNavController().navigate(R.id.reportScreenFragment, bundle)
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
            Toast.makeText(requireContext(), "vpn Disconnected", Toast.LENGTH_SHORT).show()
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
        countDownTimer?.cancel()
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
    }
}