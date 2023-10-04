package com.xilli.stealthnet.Fragments

import android.app.AlertDialog
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.xilli.stealthnet.Activities.MainActivity
import com.xilli.stealthnet.Activities.MainActivity.Companion.selectedCountry
import com.xilli.stealthnet.R
import com.xilli.stealthnet.Utils.ActiveServer
import com.xilli.stealthnet.databinding.FragmentHomeBinding
import com.xilli.stealthnet.helper.Utils.isConnected
import com.xilli.stealthnet.helper.Utils.isVpnConnected
import com.xilli.stealthnet.model.Countries
import com.xilli.stealthnet.ui.viewmodels.VpnViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.oneconnectapi.app.core.OpenVPNThread

class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private lateinit var viewModel: VpnViewModel
    private var isFirst = true
    private var connectionStateTextView: TextView? = null
    private var timerTextView: TextView? = null
    private var isButtonClicked = true
    private var isNavigationInProgress = false
    private val VPN_PERMISSION_REQUEST_CODE = 123
    private val vpnThread = OpenVPNThread()

    companion object {
        var type = ""
        val activeServer = ActiveServer()
    }
    @JvmField
    var flagName: TextView? = null

    @JvmField
    var imgFlag: ImageView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        loadLottieAnimation()
        viewModel = ViewModelProvider(requireActivity())[VpnViewModel::class.java]
        binding?.lifecycleOwner = viewLifecycleOwner
        connectionStateTextView = binding?.root?.findViewById(R.id.textView6)
        timerTextView = binding?.root?.findViewById(R.id.timeline)
        setConnectBtnClickListener()
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicklistner()
        backpressed()

        val countryName = arguments?.getString("countryName")
        val flagUrl = arguments?.getString("flagUrl")
    }

    fun setConnectBtnClickListener() {
        binding?.imageView4?.setOnClickListener {
            if (selectedCountry != null && !isVpnConnected(requireContext())) {
                val mainActivity = activity as? MainActivity
                mainActivity?.showMessage("VPN is Connecting WAIT", "success")

                val intent = VpnService.prepare(requireContext())
                if (intent != null) {
                    val VPN_PERMISSION_REQUEST_CODE = 123
                    startActivityForResult(intent, VPN_PERMISSION_REQUEST_CODE)
                } else {

                    mainActivity?.prepareVpn()
                    mainActivity?.btnConnectDisconnect()
                    loadLottieAnimation()
                    binding?.connect?.text = "Loading...."
                    binding?.power?.visibility = View.GONE
                    binding?.lottieAnimationView?.visibility = View.VISIBLE

                    // Use a coroutine to delay the navigation
                    lifecycleScope.launch {
                        delay(8000) // Delay for 8 seconds
                        binding?.connect?.text = "Connected"
                        val navController = Navigation.findNavController(
                            requireActivity(),
                            R.id.nav_host_fragment
                        )
                        val action = HomeFragmentDirections.actionHomeFragmentToRateScreenFragment()
                        navController.navigate(action)
                    }
                }
            } else {
                val mainActivity = activity as? MainActivity
                mainActivity?.showMessage("Select a server first", "error")
            }
        }

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

        //connect button for vpn still not using correctly!!!

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
}