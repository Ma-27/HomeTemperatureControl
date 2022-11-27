package com.hometemperature.ui.home

import androidx.recyclerview.widget.DiffUtil
import com.hometemperature.bean.item.WifiItem

class WifiListDiffUtilCallback : DiffUtil.ItemCallback<WifiItem>() {
    override fun areContentsTheSame(oldItem: WifiItem, newItem: WifiItem): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: WifiItem, newItem: WifiItem): Boolean {
        return oldItem.wifiName == newItem.wifiName
    }
}