package com.hometemperature.util

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hometemperature.bean.WifiItem
import com.hometemperature.ui.home.WifiListAdapter

class BindingAdapter

//TODO set the list in view model and bind wifi list to recycler view
@BindingAdapter("setWifiDataList")
fun commitWifiList(listView: RecyclerView, items: List<WifiItem>?) {
    items?.let {
        (listView.adapter as WifiListAdapter).submitList(items)
    }
}