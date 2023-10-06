package com.xilli.stealthnet.AdapterWrappers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xilli.stealthnet.Activities.MainActivity
import com.xilli.stealthnet.R
import com.xilli.stealthnet.Utils.Constants.FREE_SERVERS
import com.xilli.stealthnet.Utils.Constants.PREMIUM_SERVERS
import com.xilli.stealthnet.helper.Utils.loadServersvip
import com.xilli.stealthnet.model.Countries

class SearchView_Premium_Adapter(private val context: Context,
    private var dataList: List<Countries>
) : RecyclerView.Adapter<SearchView_Premium_Adapter.ViewHolder>()
{
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

        Glide.with(context)
            .load(data.flagUrl)
            .into(holder.flagImageView)
        holder.flagNameTextView.text = data.country
        holder.signalImageView.setImageResource(data.signal) // Use the signal property

        val isSelected = position == selectedPosition
        holder.constraintLayout.setBackgroundResource(
            if (isSelected) R.drawable.selector_background
            else R.drawable.background_black_card
        )

        // Determine whether to show RadioButton or Crown image based on data source
        val isFreeData = dataList.indexOf(data) < loadServersvip().size // Assuming free data comes before premium data
        val isPremiumData = !isFreeData

        if (isFreeData) {
            // Show RadioButton for free data
            holder.radioButton.visibility = View.VISIBLE
            holder.radioButton.isChecked = isSelected
            holder.crownImageView.visibility = View.GONE
        } else if (isPremiumData) {
            // Show Crown image for premium data
            holder.crownImageView.visibility = View.VISIBLE
            holder.radioButton.visibility = View.GONE
        } else {
            // Neither free nor premium, handle this case as needed (e.g., show an error message)
            Toast.makeText(context, "No country selected", Toast.LENGTH_SHORT).show()
            holder.radioButton.visibility = View.GONE
            holder.crownImageView.visibility = View.GONE
        }

        holder.constraintLayout.setOnClickListener {
            onItemClickListener?.onItemClick(data, position)
        }
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    fun submitList(filteredPremiumServers: List<Countries>) {
        dataList = filteredPremiumServers
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flagImageView: ImageView = itemView.findViewById(R.id.imageView)
        val flagNameTextView: TextView = itemView.findViewById(R.id.flag_name)
//        val vpnIpTextView: TextView = itemView.findViewById(R.id.vpn_ip)
        val signalImageView: ImageView = itemView.findViewById(R.id.signalgreen)
        val crownImageView: ImageView = itemView.findViewById(R.id.radio)
        val radioButton: RadioButton = itemView.findViewById(R.id.radio2)
        val constraintLayout: ConstraintLayout = itemView.findViewById(R.id.constraintLayoutpremium)
        init {
            radioButton.setOnClickListener {
                val position = adapterPosition // Get the adapter position of this ViewHolder
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(dataList[position], position)
                }
            }
        }
    }
}
