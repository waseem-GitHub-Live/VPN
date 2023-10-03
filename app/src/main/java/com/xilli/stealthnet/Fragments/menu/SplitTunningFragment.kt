package com.xilli.stealthnet.ui.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.xilli.stealthnet.R
import com.xilli.stealthnet.databinding.FragmentSettingBinding
import com.xilli.stealthnet.databinding.FragmentSplitTunningBinding


class SplitTunningFragment : Fragment() {
    private var binding:FragmentSplitTunningBinding?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSplitTunningBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicklistner()
    }

    private fun clicklistner() {
        binding?.backfromSplit?.setOnClickListener {
            findNavController().popBackStack()
        }

    }
}