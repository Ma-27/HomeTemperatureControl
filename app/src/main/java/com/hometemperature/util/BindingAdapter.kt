package com.hometemperature.util

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hometemperature.bean.WifiItem
import com.hometemperature.ui.home.WifiListAdapter

class BindingAdapter

/**
 *设置浏览历史上的一天的recycler view数据绑定
 */
@BindingAdapter("app:setWifiDataList")
fun commitWifiList(listView: RecyclerView, items: List<WifiItem>?) {
    items?.let {
        (listView.adapter as WifiListAdapter).submitList(items)
    }
}