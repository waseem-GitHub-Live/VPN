package com.xilli.stealthnet.AdapterWrappers

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xilli.stealthnet.Activities.MainActivity
import com.xilli.stealthnet.Fragments.HomeFragment
import com.xilli.stealthnet.Fragments.ServerListFragment
import com.xilli.stealthnet.R
import com.xilli.stealthnet.model.Countries

class SearchView_Free_Adapter(private val context: Context,private val dataList: List<Countries>,private val parentFragment: Fragment ) :
    RecyclerView.Adapter<SearchView_Free_Adapter.ViewHolder>() {
    private var onItemClickListener: ((Int) -> Unit)? = null
    private var selectedPosition: Int = RecyclerView.NO_POSITION

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
    }
    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }
    fun resetSelection() {
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_free_server, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        Glide.with(context)
            .load(data.getFlagUrl1())
            .into(holder.flagImageView)
        holder.flagNameTextView.text = data.country
        holder.signalview.setImageResource(data.signal)
        holder.radioButton.isChecked = data.radiobutton
        val isSelected = position == selectedPosition
        holder.radioButton.isChecked = isSelected
        holder.constraintLayout.setBackgroundResource(
            if (isSelected) R.drawable.selector_background
            else R.drawable.background_black_card
        )
        holder.constraintLayout.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("c", data)
            intent.putExtra("type", MainActivity.type)
            intent.putExtra("countryName", data.getCountry1())
            intent.putExtra("flagUrl", data.getFlagUrl1())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flagImageView: ImageView = itemView.findViewById(R.id.imageView200)
        val flagNameTextView: TextView = itemView.findViewById(R.id.flag_name2)
//        val vpnIpTextView: TextView = itemView.findViewById(R.id.vpn_ip2)
        val signalview: ImageView = itemView.findViewById(R.id.signalgreen2)
        val radioButton: RadioButton = itemView.findViewById(R.id.radio2)
        val constraintLayout: ConstraintLayout = itemView.findViewById(R.id.constraintLayout400)

        init {
            radioButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(position)
                }
            }

            constraintLayout.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(position)
                }
            }
        }
    }

}