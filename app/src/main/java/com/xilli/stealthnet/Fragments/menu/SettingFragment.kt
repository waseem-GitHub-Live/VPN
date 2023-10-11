package com.xilli.stealthnet.Fragments.menu

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.xilli.stealthnet.Activities.ConnectionStabilityService
import com.xilli.stealthnet.R
import com.xilli.stealthnet.databinding.FragmentSettingBinding
import com.xilli.stealthnet.helper.Utils
import com.xilli.stealthnet.Fragments.viewmodels.SharedViewmodel
import top.oneconnectapi.app.core.OpenVPNThread


class SettingFragment : Fragment() {
    private var binding:FragmentSettingBinding?=null
    private var savedSwitchState: Boolean = false
    private var autoconnectState: Boolean = false
    private var improveState: Boolean = false
    private var savedataState: Boolean = false
    private lateinit var sharedPrefs: SharedPreferences

    companion object {
        private const val KILLSWITCH_STATE_KEY = "killswitch_state"
        private const val AUTOCONNECT_STATE_KEY = "autoconnect_state"
        private const val IMPROVE_STATE_KEY = "improve_state"
        private const val SAVEDATA_STATE_KEY = "savedata_state"
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicklistener()
        killswitchmethod()

    }

    private fun killswitchmethod() {
        val viewModel: SharedViewmodel by viewModels()

        savedSwitchState = sharedPrefs.getBoolean(KILLSWITCH_STATE_KEY, false)
        val autoconnectState = sharedPrefs.getBoolean(AUTOCONNECT_STATE_KEY, false)
        val improveState = sharedPrefs.getBoolean(IMPROVE_STATE_KEY, false)
        val savedataState = sharedPrefs.getBoolean(SAVEDATA_STATE_KEY, false)

        binding?.killswitchconnect?.isChecked = savedSwitchState

        binding?.killswitchconnect?.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isSwitchChecked.value = isChecked

            sharedPrefs.edit().putBoolean(KILLSWITCH_STATE_KEY, isChecked).apply()
        }
        binding?.autoconnectswitch?.isChecked = autoconnectState
        binding?.improve?.isChecked = improveState
        binding?.savedata?.isChecked = savedataState
        binding?.autoconnectswitch?.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isSwitchCheckedauto.value = isChecked

            sharedPrefs.edit().putBoolean(AUTOCONNECT_STATE_KEY, isChecked).apply()
        }
        binding?.improve?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.isSwitchCheckedimprove.value = isChecked
                val serviceIntent = Intent(requireContext(), ConnectionStabilityService::class.java)
                requireContext().startService(serviceIntent)
                sharedPrefs.edit().putBoolean(IMPROVE_STATE_KEY, isChecked).apply()
            }else{
                val serviceIntent = Intent(requireContext(), ConnectionStabilityService::class.java)
                requireContext().stopService(serviceIntent)
            }
        }
        binding?.savedata?.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isSwitchCheckedsavedata.value = isChecked

            sharedPrefs.edit().putBoolean(SAVEDATA_STATE_KEY, isChecked).apply()
        }
    }

    private fun clicklistener() {
        binding?.backfromsettings?.setOnClickListener {
            findNavController().popBackStack()
        }
        binding?.language?.setOnClickListener {
            val action = SettingFragmentDirections.actionSettingFragmentToLanguageFragment()
            findNavController().navigate(action)
        }
//        val oneConnect = OpenVPNService() // Initialize OneConnect instance (ensure you have the necessary API key and context)

        binding?.killswitchconnect?.setOnCheckedChangeListener { _, isChecked ->
            val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkRequest = NetworkRequest.Builder().build()

            connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {

                }

                override fun onLost(network: Network) {
                    disconnectFromVpn()

                }
            })
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KILLSWITCH_STATE_KEY, savedSwitchState)
        outState.putBoolean(SAVEDATA_STATE_KEY, savedataState)
        outState.putBoolean(IMPROVE_STATE_KEY, improveState)
        outState.putBoolean(AUTOCONNECT_STATE_KEY, autoconnectState)
    }
    fun disconnectFromVpn() {
        try {
            OpenVPNThread.stop()
            Utils.updateUI("DISCONNECTED")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun noconnectionD() {
        val alertDialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_improve, null)
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setView(alertDialogView)
        val okButton = alertDialogView.findViewById<TextView>(R.id.Ok)
        val cancelButton = alertDialogView.findViewById<TextView>(R.id.Ok)
        val alertDialog = alertDialogBuilder.create()
        okButton.setOnClickListener {

        }
        cancelButton.setOnClickListener {
            val serviceIntent = Intent(requireContext(), ConnectionStabilityService::class.java)
            requireContext().stopService(serviceIntent)
            binding?.improve?.isChecked = false
            alertDialog.dismiss()
        }
        val dialogWindow = alertDialog.window
        dialogWindow?.setBackgroundDrawableResource(android.R.color.transparent)

        alertDialog.show()
    }
}