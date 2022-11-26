package com.hometemperature.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hometemperature.bean.item.WifiItem
import com.hometemperature.databinding.ItemWifiBinding

class WifiListAdapter(private val clickListener: WifiListClickListener) :
    ListAdapter<WifiItem, WifiListAdapter.ViewHolder>(WifiListDiffUtilCallback()) {

    //承载每个item数据的view holder
    class ViewHolder private constructor(private val binding: ItemWifiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WifiItem, clickListener: WifiListClickListener) {
            binding.wifiitem = item
            binding.executePendingBindings()
            binding.clickListener = clickListener
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemWifiBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiListAdapter.ViewHolder {
        return WifiListAdapter.ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: WifiListAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }
}

class WifiListClickListener(val clickListener: (item: WifiItem) -> Unit) {
    fun onClick(item: WifiItem) = clickListener(item)
}
