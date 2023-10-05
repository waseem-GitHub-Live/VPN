package com.xilli.stealthnet.Fragments.menu

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.xilli.stealthnet.databinding.FragmentSplitTunningBinding
import com.xilli.stealthnet.helper.Utils.getSharedPreferences


class SplitTunningFragment : Fragment() {
    private var binding:FragmentSplitTunningBinding?=null
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSplitTunningBinding.inflate(inflater, container, false)
        sharedPrefs = requireContext().getSharedPreferences("SplitTunnelingPrefs", Context.MODE_PRIVATE)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicklistner()
        binding?.chromeS?.isChecked = getSplitTunnelingPreference("Chrome")
        binding?.youtubeS?.isChecked = getSplitTunnelingPreference("YouTube")
        binding?.facebookS?.isChecked = getSplitTunnelingPreference("Facebook")
        binding?.whattsappS?.isChecked = getSplitTunnelingPreference("WhatsApp")
    }

    private fun clicklistner() {
        binding?.backfromSplit?.setOnClickListener {
            findNavController().popBackStack()
        }
        binding?.chromeS?.setOnCheckedChangeListener { _, isChecked ->
            setSplitTunnelingPreference("Chrome", isChecked)
        }

        binding?.youtubeS?.setOnCheckedChangeListener { _, isChecked ->
            setSplitTunnelingPreference("YouTube", isChecked)
        }

        binding?.facebookS?.setOnCheckedChangeListener { _, isChecked ->
            setSplitTunnelingPreference("Facebook", isChecked)
        }

        binding?.whattsappS?.setOnCheckedChangeListener { _, isChecked ->
            setSplitTunnelingPreference("WhatsApp", isChecked)
        }

    }

    fun setSplitTunnelingPreference(appName: String, isEnabled: Boolean) {
        val editor = sharedPrefs.edit()
        editor.putBoolean(appName, isEnabled)
        editor.apply()
    }

    fun getSplitTunnelingPreference(appName: String): Boolean {
        return sharedPrefs.getBoolean(appName, false)
    }
}