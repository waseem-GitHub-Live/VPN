//package com.xilli.stealthnet.ui.menu
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.ui.NavigationUI
//import com.xilli.stealthnet.R
//import com.xilli.stealthnet.databinding.FragmentMenuBinding
//
//class MenuFragment : Fragment() {
//    private var binding: FragmentMenuBinding? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = FragmentMenuBinding.inflate(inflater, container, false)
//
//        return binding?.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Set up the navigation drawer
//        binding?.navigationView.let {
//            if (it != null) {
//                NavigationUI.setupWithNavController(it, findNavController())
//            }
//        }
//
//        // Handle navigation item clicks
//
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        binding = null
//    }
//}
