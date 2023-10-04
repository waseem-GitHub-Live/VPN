package com.xilli.stealthnet.Fragments

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.xilli.stealthnet.R
import com.xilli.stealthnet.databinding.FragmentOnboardingScreenBinding

class OnboardingScreenFragment : Fragment() {
    private var binding: FragmentOnboardingScreenBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnboardingScreenBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicklistner()
        textcolor()

    }

    private fun textcolor() {
        val fullText = getString(R.string.description_onboarding_screen)
        val keyword = "Stealth"

        val spannable = SpannableStringBuilder(fullText)

        val startIndex = fullText.indexOf(keyword)
        if (startIndex != -1) {
            val endIndex = startIndex + keyword.length
            val color = ContextCompat.getColor(requireContext(), R.color.selected)
            spannable.setSpan(
                ForegroundColorSpan(color),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding?.textView21?.text = spannable
    }
    private fun clicklistner() {
        binding?.crossAccept?.setOnClickListener {
            requireActivity().finish()
        }
        binding?.acceptandcontiue?.setOnClickListener {
            onboardingCompleted()
            val action = OnboardingScreenFragmentDirections.actionOnboardingScreenFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }
    private fun onboardingCompleted() {
        val sharedPreferences = requireContext().getSharedPreferences("onboarding", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("completed", true)
        editor.apply()
    }

}