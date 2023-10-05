package com.xilli.stealthnet.AdapterWrappers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xilli.stealthnet.R
import com.xilli.stealthnet.model.Countries

class CombinedAdapter(
    private val context: Context,
    private val dataList: List<Countries>,
    private val onItemClick: (Countries, Int) -> Unit
) : RecyclerView.Adapter<CombinedAdapter.ViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutResId = if (viewType == 1) {
            R.layout.item_free_server
        } else {
            R.layout.item_premier_server
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        val isSelected = position == selectedPosition

        Glide.with(context)
            .load(data.getFlagUrl1())
            .into(holder.flagImageView)

        holder.flagNameTextView.text = data.country

        if (holder is FreeServerViewHolder) {
            holder.signalImageView.setImageResource(data.signal)
            holder.radioButton.isChecked = data.radiobutton
        } else if (holder is PremiumServerViewHolder) {
            holder.signalImageView.setImageResource(data.signal)
            holder.crownImageView.setImageResource(data.crown)
        }

        holder.constraintLayout.setBackgroundResource(
            if (isSelected) R.drawable.selector_background
            else R.drawable.background_black_card
        )

        holder.itemView.setOnClickListener {
            onItemClick(data, position)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {

        return dataList[position].type
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flagImageView: ImageView = itemView.findViewById(R.id.imageView)
        val flagNameTextView: TextView = itemView.findViewById(R.id.flag_name)
        val constraintLayout: ConstraintLayout = itemView.findViewById(R.id.constraintLayout)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(dataList[position], position)
                }
            }
        }
    }

    inner class FreeServerViewHolder(itemView: View) : ViewHolder(itemView) {
        val signalImageView: ImageView = itemView.findViewById(R.id.signalgreen)
        val radioButton: RadioButton = itemView.findViewById(R.id.radio)
    }

    inner class PremiumServerViewHolder(itemView: View) : ViewHolder(itemView) {
        val signalImageView: ImageView = itemView.findViewById(R.id.signalgreen)
        val crownImageView: ImageView = itemView.findViewById(R.id.radio)
    }
}
