package com.xilli.stealthnet.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.xilli.stealthnet.databinding.FragmentReportScreenBinding
import com.xilli.stealthnet.ui.viewmodels.SharedViewmodel


class ReportScreenFragment : Fragment() {
    private var binding:FragmentReportScreenBinding?=null
//    val countryName = Utility.countryName
    private  var viewModel: SharedViewmodel?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReportScreenBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[SharedViewmodel::class.java]

        // Observe changes to the averages
        viewModel?.getAverageRxSpeed()?.observe(viewLifecycleOwner) { avgRxSpeed ->
            binding?.downloadTime?.text = avgRxSpeed

        }

        viewModel?.getAverageTxSpeed()?.observe(viewLifecycleOwner) { avgTxSpeed ->
            binding?.UploadTime?.text = avgTxSpeed
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicklistener()
        datasheet()


    }

    private fun datasheet() {
//        binding?.textView9?.text = countryName
//        binding?.connectionReportIp?.let { Utility.showIP(it) }
        val elapsedTime = arguments?.getString("elapsedTime")
        binding?.durationTime?.text = elapsedTime
        binding?.datausageTime?.text =  viewModel?.totalDataUsage1
    }


    private fun clicklistener() {
        binding?.imageView5?.setOnClickListener {
            val action = ReportScreenFragmentDirections.actionReportScreenFragmentToHomeFragment()
            findNavController().navigate(action)
        }
        binding?.backHome?.setOnClickListener {
            val action = ReportScreenFragmentDirections.actionReportScreenFragmentToHomeFragment()
            findNavController().navigate(action)
        }
        binding?.connectionagain?.setOnClickListener {
            val action = ReportScreenFragmentDirections.actionReportScreenFragmentToServerListFragment()
            findNavController().navigate(action)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val action = ReportScreenFragmentDirections.actionReportScreenFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }
}