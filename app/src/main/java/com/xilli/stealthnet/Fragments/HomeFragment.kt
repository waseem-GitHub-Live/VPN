package com.xilli.stealthnet.Fragments

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.xilli.stealthnet.Activities.MainActivity
import com.xilli.stealthnet.Activities.MainActivity.Companion.selectedCountry
import com.xilli.stealthnet.Activities.Utility
import com.xilli.stealthnet.Fragments.viewmodels.SharedViewmodel
import com.xilli.stealthnet.R
import com.xilli.stealthnet.Utils.ActiveServer
import com.xilli.stealthnet.databinding.FragmentHomeBinding
import com.xilli.stealthnet.helper.Utils
import com.xilli.stealthnet.helper.Utils.getIntent
import com.xilli.stealthnet.helper.Utils.isConnected
import com.xilli.stealthnet.helper.Utils.showMessage
import com.xilli.stealthnet.model.Countries
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.EasyPermissions
import java.util.Objects

class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private var binding: FragmentHomeBinding? = null
    private var isFirst = true
    private var connectionStateTextView: TextView? = null
    private var timerTextView: TextView? = null
    private val viewModel by viewModels<SharedViewmodel>()
    companion object {
        var type = ""
        var freeServerList = emptyList<Countries>()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        loadLottieAnimation()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadcastReceiver, IntentFilter("connectionState"))
        binding?.lifecycleOwner = viewLifecycleOwner
        connectionStateTextView = binding?.root?.findViewById(R.id.textView6)
        timerTextView = binding?.root?.findViewById(R.id.timeline)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (selectedCountry == null){
            freeServerList = Utils.loadServers()
            selectedCountry = freeServerList[0]
        }

        clicklistner()
        backpressed()
        setConnectBtnClickListener()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionChecker()
        }
    }

    fun setConnectBtnClickListener() {
        val mainActivity = activity as? MainActivity
        val context = requireContext()
        binding?.imageView4?.setOnClickListener {
            if (!Utility.isOnline(context)) {
                noconnectionD()
                binding?.connect?.text = "Wait"
                showMessage("Connect to the internet and start again", "error", context)
            } else if (selectedCountry != null) {
                showMessage("VPN is Connecting WAIT", "success", context)
                binding?.connect?.text = "Authenticating"
                CoroutineScope(Dispatchers.Main).launch {
                    val intent = VpnService.prepare(context)
                    if (intent != null) {
                        val VPN_PERMISSION_REQUEST_CODE = 123
                        startActivityForResult(intent, VPN_PERMISSION_REQUEST_CODE)
                        binding?.connect?.text = "Disconnect"
                    } else {
                        val isVpnConnected = withContext(Dispatchers.IO) {
                            mainActivity?.prepareVpn()
                        }

                        if (isVpnConnected == true) {
                            mainActivity?.btnConnectDisconnect()
                            loadLottieAnimation()
                            binding?.connect?.text = "Loading...."
                            binding?.power?.visibility = View.GONE
                            binding?.lottieAnimationView?.visibility = View.VISIBLE
                        } else {
                            showMessage("VPN connection failed", "error", context)
                        }
                    }
                }
            } else {
                showMessage("Select a server first", "error", context)
            }
        }
    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            try {
                val vpnState = intent.getStringExtra("state")
                if (vpnState != null) {
                    if (vpnState == "CONNECTED") {
                        Utils.updateUI("connected")
                        isConnected = true
                        binding?.connect?.text = "Connected"

                        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToRateScreenFragment())

                    } else if (vpnState == "DISCONNECTED") {

                        Utils.updateUI("disconnected")
                        isConnected = false
                        binding?.connect?.text = "Disconnected"
                        binding?.power?.visibility = View.VISIBLE
                        binding?.lottieAnimationView?.visibility = View.GONE
                        showMessage("Select AGain Please!!", "error")
                    }
                    Log.v("yoo", vpnState)
                }

                Objects.requireNonNull(getIntent().getStringExtra("state")).let {
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
                isConnected = true

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

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    fun showMessage(msg: String?, type: String, context: Context?) {
        context?.let {
            if (type == "success") {
                if (msg != null) {
                    Toasty.success(it, msg, Toast.LENGTH_SHORT, true).show()
                }
            } else if (type == "error") {
                if (msg != null) {
                    Toasty.error(it, msg, Toast.LENGTH_SHORT, true).show()
                }
            } else {
            }
        }
    }

    private fun noconnectionD() {
        val alertDialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_vpn_connection, null)

        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        alertDialogBuilder.setView(alertDialogView)

        val cancelButton = alertDialogView.findViewById<TextView>(R.id.Ok)

        val alertDialog = alertDialogBuilder.create()
        cancelButton.setOnClickListener {

            alertDialog.dismiss()
        }

        val dialogWindow = alertDialog.window
        dialogWindow?.setBackgroundDrawableResource(android.R.color.transparent)

        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        binding?.lottieAnimationView?.playAnimation()
        binding?.lottieAnimationView2?.playAnimation()
    }

    private fun backpressed() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showAlertDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    private fun showAlertDialog() {
        val alertDialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_exit, null)

        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        alertDialogBuilder.setView(alertDialogView)

        val exitButton = alertDialogView.findViewById<ConstraintLayout>(R.id.exitbutton)
        val cancelButton = alertDialogView.findViewById<ConstraintLayout>(R.id.cancelbutton2)

        exitButton.setOnClickListener {
            activity?.finish()
        }
        val alertDialog = alertDialogBuilder.create()
        cancelButton.setOnClickListener {

            alertDialog.dismiss()
        }

        val dialogWindow = alertDialog.window
        dialogWindow?.setBackgroundDrawableResource(android.R.color.transparent)

        alertDialog.show()
    }

    private fun loadLottieAnimation() {
        binding?.lottieAnimationView?.setAnimation(R.raw.loading_animation)
        binding?.lottieAnimationView2?.setAnimation(R.raw.backview)
        binding?.lottieAnimationView2?.repeatCount = LottieDrawable.INFINITE
        binding?.lottieAnimationView?.repeatCount = LottieDrawable.INFINITE
        binding?.lottieAnimationView?.addAnimatorUpdateListener {
        }
        binding?.lottieAnimationView2?.addAnimatorUpdateListener {
        }
        binding?.lottieAnimationView?.playAnimation()
        binding?.lottieAnimationView2?.playAnimation()
    }

    private fun clicklistner() {
        binding?.menu?.setOnClickListener {
            val drawerLayout =
                requireActivity().findViewById<DrawerLayout>(R.id.constraintlayoutmenu)
            drawerLayout.openDrawer(GravityCompat.START)
        }
        binding?.premiumbutton?.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPremiumFragment())
        }
        binding?.constraintLayout2?.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToServerListFragment())
        }
        binding?.navigationView?.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings_menu -> {
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSettingFragment())
                }

                R.id.server_menu -> {
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToServerListFragment())
                }

                R.id.split_menu -> {
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSplitTunningFragment())
                }
            }
            true
        }
    }
    private fun hasNotificationPermission(): Boolean {
        val context = requireContext() // Check if it's not null
        return EasyPermissions.hasPermissions(context, Manifest.permission.POST_NOTIFICATIONS)
    }


    private fun notificationPermissionChecker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
            EasyPermissions.requestPermissions(
                requireActivity(),
                getString(R.string.notificationPermission),
                0,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        TODO("Not yet implemented")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        TODO("Not yet implemented")
    }
}