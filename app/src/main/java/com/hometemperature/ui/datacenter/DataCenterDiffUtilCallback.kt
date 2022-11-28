package com.hometemperature.ui.datacenter

import androidx.recyclerview.widget.DiffUtil
import com.hometemperature.bean.item.DataItem

class DataCenterDiffUtilCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}