package com.xilli.stealthnet.Fragments

import android.content.Context
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
import com.xilli.stealthnet.databinding.FragmentReportScreenBinding
import com.xilli.stealthnet.Fragments.viewmodels.SharedViewmodel
import com.xilli.stealthnet.helper.Utils.sharedPreferences


class ReportScreenFragment : Fragment() {
    private var binding: FragmentReportScreenBinding? = null

    //    val countryName = Utility.countryName
    private var viewModel: SharedViewmodel?=null
    private lateinit var sharedrefrence: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReportScreenBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[SharedViewmodel::class.java]
        sharedrefrence = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
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
        val duration = sharedPreferences.getString("duration", "00:00:00")
        binding?.durationTime?.text = duration
        val sumBytes = sharedPreferences.getLong("sum", 0L)
        val byteIn = sharedPreferences.getString("byteIn", " ")
        val byteOut = sharedPreferences.getString("byteOut", " ")
        val sumBytesHumanReadable = formatDataUsage(sumBytes)
        binding?.datausageTime?.text = sumBytesHumanReadable
        binding?.downloadTime?.text = byteIn
        binding?.UploadTime?.text = byteOut
    }
    fun formatDataUsage(bytes: Long): String {
        val kb = bytes / 1024
        val mb = kb / 1024
        val gb = mb / 1024

        return when {
            gb > 1 -> String.format("%.2f GB", gb.toDouble())
            mb > 1 -> String.format("%.2f MB", mb.toDouble())
            kb > 1 -> String.format("%.2f KB", kb.toDouble())
            else -> String.format("%d Bytes", bytes)
        }
    }

    private fun getSavedVPNIP(): String {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("vpnIP", "") ?: ""
    }


    private fun clicklistener() {
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

    }
}