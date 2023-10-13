package com.xilli.stealthnet.Fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.xilli.stealthnet.Activities.MainActivity.Companion.selectedCountry
import com.xilli.stealthnet.Activities.SharedPreferencesHelper
import com.xilli.stealthnet.databinding.FragmentReportScreenBinding
import com.xilli.stealthnet.Fragments.viewmodels.SharedViewmodel


class ReportScreenFragment : Fragment() {
    private var binding: FragmentReportScreenBinding? = null

    //    val countryName = Utility.countryName
    private var viewModel: SharedViewmodel?=null
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var sharedrefrence: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReportScreenBinding.inflate(inflater, container, false)
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
        viewModel = ViewModelProvider(requireActivity())[SharedViewmodel::class.java]
        sharedrefrence = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
//        RateScreenFragment.dataUsage = 0
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicklistener()
        datasheet()
    }

    private fun datasheet() {
        binding?.textView9?.text = selectedCountry?.country
        val savedVPNIP = getSavedVPNIP()
        if (savedVPNIP.isNotEmpty()) {
            binding?.connectionReportIp?.text = savedVPNIP
        }

//        val elapsedTime = arguments?.getString("elapsedTime")
//        binding?.durationTime?.text = elapsedTime
//        binding?.datausageTime?.text = viewModel?.totalDataUsage1
//        binding?.durationTime?.text = vpnName


        val dataUsageInFragment = sharedrefrence.getLong("dataUsageInFragment", 0L)
        val downloadSpeed = sharedrefrence.getFloat("downloadSpeed", 0.0f) // Default to 0.0f if not found
        val uploadSpeed = sharedrefrence.getFloat("uploadSpeed", 0.0f) // Default to 0.0f if not found

// Format data usage
        val formattedDataUsage = formatDataUsage(dataUsageInFragment.toFloat())

// Format download and upload speeds
        val formattedDownloadSpeed = formatDataUsage(downloadSpeed)
        val formattedUploadSpeed = formatDataUsage(uploadSpeed)

// Update the TextViews with the formatted values
        binding?.datausageTime?.text = formattedDataUsage
        binding?.downloadTime?.text = formattedDownloadSpeed
        binding?.UploadTime?.text = formattedUploadSpeed

    }
    fun formatDataRate(average: Float): String {
        val kilobyte = 1024
        val megabyte = kilobyte * 1024

        return when {
            average >= megabyte -> String.format("%.2f MB/s", average / megabyte)
            average >= kilobyte -> String.format("%.2f KB/s", average / kilobyte)
            else -> String.format("%.2f B/s", average)
        }
    }
    private fun formatDataUsage(bytes: Float): String {
        val kilobyte = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024

        return when {
            bytes >= gigabyte -> String.format("%.2f GB", bytes / gigabyte)
            bytes >= megabyte -> String.format("%.2f MB", bytes / megabyte)
            bytes >= kilobyte -> String.format("%.2f KB", bytes / kilobyte)
            else -> "$bytes bytes"
        }
    }
    private fun getSavedVPNIP(): String {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("vpnIP", "") ?: ""
    }


    private fun clicklistener() {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        binding?.imageView5?.setOnClickListener {
            findNavController().navigate(ReportScreenFragmentDirections.actionReportScreenFragmentToHomeFragment())
        }
        binding?.backHome?.setOnClickListener {
            findNavController().navigate(ReportScreenFragmentDirections.actionReportScreenFragmentToHomeFragment())
        }
        binding?.connectionagain?.setOnClickListener {
            findNavController().navigate(ReportScreenFragmentDirections.actionReportScreenFragmentToServerListFragment())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val action = ReportScreenFragmentDirections.actionReportScreenFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        // Remove the data from SharedPreferences in onDestroy
//        val editor = sharedPreferences.edit()
//        editor.remove("dataUsageInFragment")
//        editor.apply()
    }
}