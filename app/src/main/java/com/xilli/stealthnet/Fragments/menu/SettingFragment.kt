package com.xilli.stealthnet.Fragments.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.xilli.stealthnet.databinding.FragmentSettingBinding


class SettingFragment : Fragment() {
    private var binding:FragmentSettingBinding?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicklistener()
    }

    private fun clicklistener() {
        binding?.backfromsettings?.setOnClickListener {
            findNavController().popBackStack()
        }
        binding?.language?.setOnClickListener {
            val action = SettingFragmentDirections.actionSettingFragmentToLanguageFragment()
            findNavController().navigate(action)
        }
    }
}