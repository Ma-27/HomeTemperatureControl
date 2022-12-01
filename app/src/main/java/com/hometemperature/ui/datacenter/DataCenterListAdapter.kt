package com.hometemperature.ui.datacenter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hometemperature.bean.item.DataItem
import com.hometemperature.databinding.ItemDatacenterBinding

class DataCenterListAdapter(private val clickListener: DataCenterClickListener) :
    ListAdapter<DataItem, DataCenterListAdapter.ViewHolder>(DataCenterDiffUtilCallback()) {

    //承载每个item数据的view holder
    class ViewHolder private constructor(private val binding: ItemDatacenterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DataItem, clickListener: DataCenterClickListener) {
            binding.item = item
            binding.executePendingBindings()
            binding.clickListener = clickListener
        }

        companion object {
            fun from(parent: ViewGroup): DataCenterListAdapter.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemDatacenterBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return DataCenterListAdapter.ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }
}

class DataCenterClickListener(val clickListener: (item: DataItem) -> Unit) {
    fun onClick(item: DataItem) = clickListener(item)
}