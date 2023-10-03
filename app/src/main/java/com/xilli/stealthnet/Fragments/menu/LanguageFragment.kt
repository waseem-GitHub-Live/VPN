package com.xilli.stealthnet.ui.menu

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.xilli.stealthnet.R
import com.xilli.stealthnet.databinding.FragmentLanguageBinding


class LanguageFragment : Fragment() {
    private var binding: FragmentLanguageBinding? = null
    private var selectedTextView: TextView? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLanguageBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicklistener()
        viewcreated()
        restoreSelection()
    }

    private fun viewcreated() {
        val textViewList = listOf(
            binding?.english,
            binding?.Spanish,
            binding?.Korean,
            binding?.Portugal,
            binding?.Italian
        )

        textViewList.forEach { textView ->
            textView?.setOnClickListener { onTextViewClicked(textView) }
        }
    }

    private fun onTextViewClicked(clickedTextView: TextView) {
        selectedTextView?.background = getDrawable(R.drawable.background_black_card)
        clickedTextView.background = getDrawable(R.drawable.selector_background)
        selectedTextView = clickedTextView

        // Store the ID of the selected TextView
        sharedPreferences.edit().putInt("selected_textview_id", clickedTextView.id).apply()
    }

    private fun restoreSelection() {
        val selectedTextViewId = sharedPreferences.getInt("selected_textview_id", -1)
        if (selectedTextViewId != -1) {
            selectedTextView = binding?.root?.findViewById(selectedTextViewId)
            selectedTextView?.background = getDrawable(R.drawable.selector_background)
        }
    }

    private fun getDrawable(resourceId: Int): Drawable? {
        return context?.let { context ->
            context.resources.getDrawable(resourceId)
        }
    }

    private fun clicklistener() {
        binding?.backfromLanguage?.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}