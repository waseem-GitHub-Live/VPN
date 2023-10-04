package com.xilli.stealthnet.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xilli.stealthnet.Activities.MainActivity
import com.xilli.stealthnet.AdapterWrappers.SearchView_Free_Adapter
import com.xilli.stealthnet.AdapterWrappers.SearchView_Premium_Adapter
import com.xilli.stealthnet.Utils.Constants
import com.xilli.stealthnet.databinding.FragmentServerListBinding
import com.xilli.stealthnet.model.Countries
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class ServerListFragment : Fragment(), SearchView_Premium_Adapter.OnItemClickListener {
    private var binding: FragmentServerListBinding?=null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterPREMIUM: SearchView_Premium_Adapter
    private lateinit var adapterFREE: SearchView_Free_Adapter
    private var isBackgroundChanged = false
    private var selectedPosition = RecyclerView.NO_POSITION
    private var fragment: Fragment?= null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentServerListBinding.inflate(inflater, container, false)
//        selectedServer = DataItemPremium("Default Server", "10.0.0.5", R.drawable.flag, R.drawable.ic_signal, R.drawable.ic_green_crown)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFreeRecyclerView()
        setupPremiumRecyclerView()
        clicklistner()
//        binding?.constraintLayout2?.performClick()


    }
    private fun setupPremiumRecyclerView() {
        recyclerView = binding?.recyclerView ?: return
        val premiumServers = loadServersvip()
        adapterPREMIUM = SearchView_Premium_Adapter(requireContext(),premiumServers )
        adapterPREMIUM.setOnItemClickListener(this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapterPREMIUM
    }


    private fun setupFreeRecyclerView() {
        recyclerView = binding?.recyclerview2 ?: return
        val freeserver = loadServers()
        adapterFREE = SearchView_Free_Adapter(requireContext(), freeserver, this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapterFREE
        adapterFREE.setOnItemClickListener { position ->
            adapterFREE.setSelectedPosition(position)
            adapterPREMIUM.resetSelection()
            adapterFREE.notifyDataSetChanged()
            adapterPREMIUM.notifyDataSetChanged()
//            binding?.constraintLayout2?.setBackgroundResource(R.drawable.background_black_card)
//            binding?.radio?.isChecked = false
        }
        loadServers()
    }

    private fun clicklistner() {
        binding?.imageView7?.setOnClickListener {
            findNavController().popBackStack()
        }
//        binding?.constraintLayout2?.setOnClickListener {
//            if (::adapterPREMIUM.isInitialized ){
//                adapterPREMIUM.resetSelection()
//                adapterPREMIUM.notifyDataSetChanged()
//            }
//
//            selectedPosition = RecyclerView.NO_POSITION
//            adapterFREE.resetSelection()
//            adapterFREE.notifyDataSetChanged()
//            isBackgroundChanged = !isBackgroundChanged
//
//            binding?.radio?.isChecked = !binding?.radio?.isChecked!!
//            binding?.constraintLayout2?.setBackgroundResource(R.drawable.selector_background)
//        }

    }
    private fun loadServers(): List<Countries> {
        val servers = ArrayList<Countries>()
        try {
            val jsonArray = JSONArray(Constants.FREE_SERVERS)
            for (i in 0 until jsonArray.length()) {
                val `object` = jsonArray[i] as JSONObject
                servers.add(
                    Countries(
                        `object`.getString("serverName"),
                        `object`.getString("flag_url"),
                        `object`.getString("ovpnConfiguration"),
                        `object`.getString("vpnUserName"),
                        `object`.getString("vpnPassword")
                    )
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return servers
    }
    private fun loadServersvip(): List<Countries> {
        val servers = ArrayList<Countries>()
        try {
            val jsonArray = JSONArray(Constants.PREMIUM_SERVERS)
            for (i in 0 until jsonArray.length()) {
                val `object` = jsonArray[i] as JSONObject
                servers.add(
                    Countries(
                        `object`.getString("serverName"),
                        `object`.getString("flag_url"),
                        `object`.getString("ovpnConfiguration"),
                        `object`.getString("vpnUserName"),
                        `object`.getString("vpnPassword")
                    )
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return servers
    }

    override fun onItemClick(country: Countries, position: Int) {
//        if (Config.vip_subscription || Config.all_subscription) {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("c", country)
        intent.putExtra("type", MainActivity.type)
        intent.putExtra("countryName", country.getCountry1())
        intent.putExtra("flagUrl", country.getFlagUrl1())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context?.startActivity(intent)
//            } else {
//                unblockServer()
//            }


        adapterPREMIUM.setSelectedPosition(position)
        adapterFREE.resetSelection()
        adapterPREMIUM.notifyDataSetChanged()
        adapterFREE.notifyDataSetChanged()
//        binding?.constraintLayout2?.setBackgroundResource(R.drawable.background_black_card)

//        binding?.radio?.isChecked = false
    }
}