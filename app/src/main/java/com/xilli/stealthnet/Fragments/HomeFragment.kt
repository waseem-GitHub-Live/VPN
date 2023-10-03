package com.xilli.stealthnet.Fragments

import android.app.AlertDialog
import android.os.Bundle
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
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieDrawable
import com.xilli.stealthnet.R
import com.xilli.stealthnet.Utils.ActiveServer
import com.xilli.stealthnet.databinding.FragmentHomeBinding
import com.xilli.stealthnet.model.Countries
import com.xilli.stealthnet.ui.viewmodels.VpnViewModel
import top.oneconnectapi.app.core.OpenVPNThread

class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private lateinit var viewModel: VpnViewModel
    var selectedCountry: Countries? = null
    private var isFirst = true
    private var connectBtnTextView: ImageView? = null
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
        connectBtnTextView = binding?.root?.findViewById(R.id.imageView4)
        timerTextView = binding?.root?.findViewById(R.id.timeline)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicklistner()
        backpressed()
        val countryName = arguments?.getString("countryName")
        val flagUrl = arguments?.getString("flagUrl")
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
//        binding?.imageView4?.setOnClickListener {
//            val intent = VpnService.prepare(requireContext())
//            if (intent != null) {
//                startActivityForResult(intent, VPN_PERMISSION_REQUEST_CODE)
//            } else {
//                isButtonClicked = false
//                if (selectedCountry != null) {
//                    loadLottieAnimation()
//                    binding?.power?.visibility = View.GONE
//                    binding?.lottieAnimationView?.visibility = View.VISIBLE
//
//                    Handler().postDelayed({
//                        val action = HomeFragmentDirections.actionHomeFragmentToRateScreenFragment()
//                        findNavController().navigate(action)
//
//                    }, 3000)
//                    isNavigationInProgress = true
//                    isButtonClicked = true
//                    isNavigationInProgress = false
//
//                }
//            }
//        }
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