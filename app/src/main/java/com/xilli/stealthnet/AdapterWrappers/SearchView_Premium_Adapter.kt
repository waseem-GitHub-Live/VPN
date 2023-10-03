package com.xilli.stealthnet.AdapterWrappers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xilli.stealthnet.R
import com.xilli.stealthnet.model.Countries

class SearchView_Premium_Adapter(
    private val dataList: List<Countries>
) : RecyclerView.Adapter<SearchView_Premium_Adapter.ViewHolder>() {
    val context: Context?=null
    private var onItemClickListener: OnItemClickListener?=null
    private var selectedPosition: Int = RecyclerView.NO_POSITION
    var datalist = ArrayList<Countries>()

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
    }
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun resetSelection() {
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_premier_server, parent, false)
        return ViewHolder(view)
    }

    interface OnItemClickListener {
        fun onItemClick(country: Countries, position: Int)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        if (context != null) {
            Glide.with(context)
                .load(data.getFlagUrl1())
                .into(holder.flagImageView)
        }
        holder.flagNameTextView.text = data.country
        holder.signalImageView.setImageResource(data.signal) // Use the signal property
        holder.crownImageView.setImageResource(data.crown)   // Use the crown property


        holder.constraintLayout.setBackgroundResource(
            if (position == selectedPosition) R.drawable.selector_background
            else R.drawable.background_black_card
        )

        holder.constraintLayout.setOnClickListener {
            onItemClickListener?.onItemClick(data,position) // Call onItemClick with the selected data
        }
    }



    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flagImageView: ImageView = itemView.findViewById(R.id.imageView)
        val flagNameTextView: TextView = itemView.findViewById(R.id.flag_name)
//        val vpnIpTextView: TextView = itemView.findViewById(R.id.vpn_ip)
        val signalImageView: ImageView = itemView.findViewById(R.id.signalgreen)
        val crownImageView: ImageView = itemView.findViewById(R.id.radio)
        val constraintLayout: ConstraintLayout = itemView.findViewById(R.id.constraintLayoutpremium)
    }
}
