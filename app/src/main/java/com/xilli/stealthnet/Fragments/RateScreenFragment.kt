package com.xilli.stealthnet.Fragments

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.xilli.stealthnet.Activities.CountDownService
import com.xilli.stealthnet.R
import com.xilli.stealthnet.databinding.FragmentRateScreenBinding
import com.xilli.stealthnet.helper.Utils.showIP
import com.xilli.stealthnet.helper.Utils.updateUI
import com.xilli.stealthnet.model.Countries
import com.xilli.stealthnet.Fragments.viewmodels.SharedViewmodel
import top.oneconnectapi.app.core.OpenVPNThread
import java.util.Timer
import java.util.TimerTask
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
    private var viewModel: SharedViewmodel?=null
    private var countryName: String? = null
    private var flagUrl: String? = null
    private var isTimerRunning = false
    companion object {
        var type = ""
        const val KEY_REMAINING_TIME = "remaining_time"
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val timeLeft = intent?.getLongExtra("time_left", 0)
            updateCountdownTextView(timeLeft)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = IntentFilter("COUNTDOWN_TICK")
        requireContext().registerReceiver(receiver, filter)

        // Check if the service is already running and stop it
        if (isTimerRunning) {
            val serviceIntent = Intent(requireContext(), CountDownService::class.java)
            requireContext().stopService(serviceIntent)
        }

        // Start the service with the new timer
        val serviceIntent = Intent(requireContext(), CountDownService::class.java)
        requireContext().startService(serviceIntent)
        isTimerRunning = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRateScreenBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[SharedViewmodel::class.java]
        binding?.lifecycleOwner = viewLifecycleOwner
        val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val selectedCountryName = sharedPrefs.getString("selectedCountryName", null)
        val selectedCountryFlagUrl = sharedPrefs.getString("selectedCountryFlagUrl", null)

        if (selectedCountryName != null && selectedCountryFlagUrl != null) {
            val selectedItem = Countries(selectedCountryName, selectedCountryFlagUrl, "", "", "")
            viewModel?.selectedItem?.value = selectedItem
        }
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicklistner()
        setupBackPressedCallback()
        startRunnable()
        updateTrafficStats()
        datasheet()
        binding?.vpnIp?.let { showIP(requireContext(),it) }
//        startCountdownTimer()

    }
    override fun onDestroy() {
        super.onDestroy()
        // Stop the CountdownService when the Fragment is destroyed

            val serviceIntent = Intent(requireContext(), CountDownService::class.java)
            requireContext().stopService(serviceIntent)

        requireContext().unregisterReceiver(receiver)
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


    //    private fun startCountdownTimer() {
//        val viewModel = ViewModelProvider(requireActivity())[SharedViewmodel::class.java]
//        viewModel.startTimer()
//
//        timerTask = object : TimerTask() {
//            override fun run() {
//                activity?.runOnUiThread {
//                    val remainingTime = viewModel.getRemainingTime()
//                    if (remainingTime > 0) {
//                        val elapsedTimeFormatted = formatElapsedTime(remainingTime)
//                        binding?.timeline?.text = elapsedTimeFormatted
//                    } else {
//                        binding?.timeline?.text = "00:00:00"
//                        timerTask?.cancel()
//                    }
//                }
//            }
//        }
//
//        timer.scheduleAtFixedRate(timerTask, 0, 1000)
//    }
//
//    private fun formatElapsedTime(elapsedTimeMillis: Long): String {
//        val seconds = (elapsedTimeMillis / 1000) % 60
//        val minutes = (elapsedTimeMillis / (1000 * 60)) % 60
//        val hours = (elapsedTimeMillis / (1000 * 60 * 60)) % 24
//        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
//    }
//
//    private fun stopCountdownTimer() {
//        timerTask?.cancel()
//        timer.purge()
//        timerTask = null
//    }
//    private inner class TimeTask: TimerTask()
//    {
//        override fun run()
//        {
//            if(dataHelper.timerCounting())
//            {
//                val time = Date().time - dataHelper.startTime()!!.time
//                binding.timeTV.text = timeStringFromLong(time)
//            }
//        }
//    }
    private fun datasheet() {

        viewModel?.selectedItem?.observe(viewLifecycleOwner) { selectedItem ->
            saveSelectedCountry(selectedItem)
            selectedItem?.let { item ->
                binding?.flagName?.text = item.getCountry1()
                binding?.flagimageView?.let {
                    Glide.with(requireContext())
                        .load(item.getFlagUrl1())
                        .into(it)
                }
            }
        }
    }

    private fun saveSelectedCountry(country: Countries?) {
        val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        if (country != null) {
            editor.putString("selectedCountryName", country.getCountry1())
            editor.putString("selectedCountryFlagUrl", country.getFlagUrl1())
        } else {
            // If no item is selected, clear the saved data
            editor.remove("selectedCountryName")
            editor.remove("selectedCountryFlagUrl")
        }

        editor.apply()
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
//        countDownTimer?.cancel()
        mHandler.removeCallbacks(mRunnable)
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
//        requireContext().unbindService(connection)
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
//    private val connection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            val binder = service as CountDownService.LocalBinder
//            countdownService = binder.getService()
//            countdownService?.setCallback(this@RateScreenFragment)
//            countdownService?.startCountdown()
//            isBound = true
//            Log.d("ServiceConnection", "Service connected")
//        }
//
//        override fun onServiceDisconnected(name: ComponentName?) {
//            countdownService = null
//            isBound = false
//            Log.d("ServiceConnection", "Service disconnected")
//        }
//    }


//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//
//        super.onActivityCreated(savedInstanceState)
//        val intent = Intent(requireContext(), CountDownService::class.java)
//        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
//    }
//
//    override fun onTimerTick(remainingTime: Long) {
//        activity?.runOnUiThread {
//            // Update the UI (TextView) here on the main (UI) thread
//            binding?.timeline?.text = formatRemainingTime(remainingTime)
//        }
//    }
//
//    fun formatRemainingTime(remainingTime: Long): String {
//        val minutes = remainingTime / 60000
//        val seconds = (remainingTime % 60000) / 1000
//        return String.format("%02d:%02d", minutes, seconds)
//    }

//    override fun onStop() {
//        super.onStop()
//        val viewModel = ViewModelProvider(requireActivity())[SharedViewmodel::class.java]
//        val remainingTime = viewModel.getRemainingTime()
//
//        val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        editor.putLong("remainingTime", remainingTime)
//        editor.apply()
//    }

//    override fun onResume() {
//        super.onResume()
//        val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
//
//
//            val remainingTime = sharedPreferences.getLong("remainingTime", 1L)
//
//            viewModel?.remainingTimeMillis = remainingTime
//            if (remainingTime > 0) {
//                startCountdownTimer()
//            } else {
//                // Handle the case when the timer has finished
//            }
//    }
}

