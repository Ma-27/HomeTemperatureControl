package com.hometemperature.util

import android.view.View
import android.widget.RelativeLayout
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hometemperature.bean.flag.MessageType
import com.hometemperature.bean.item.DataItem
import com.hometemperature.bean.item.WifiItem
import com.hometemperature.ui.datacenter.DataCenterListAdapter
import com.hometemperature.ui.home.WifiListAdapter

class BindingAdapter

//set the list in view model and bind wifi list to recycler view
@BindingAdapter("setWifiDataList")
fun commitWifiList(listView: RecyclerView, items: List<WifiItem>?) {
    items?.let {
        (listView.adapter as WifiListAdapter).submitList(items)
    }
}

//set the list in view model and bind data to data center recycler view
@BindingAdapter("setDataCenterList")
fun commitDataCenterList(listView: RecyclerView, items: MutableList<DataItem>?) {
    items?.let {
        (listView.adapter as DataCenterListAdapter).submitList(items)
    }
}

//set the item visibility, especially in datacenter recycler view
//flag 0 imply the view is a receiver view,flag 1 imply the view is a sender view;flag can only be passed by 0 or 1.
@BindingAdapter("messagetype", "flag")
fun itemVisibility(view: RelativeLayout, messageType: Int, flag: Int) {
    var visibility = View.VISIBLE
    //when flag is 0 and the message is a received message,then set the view visible
    //when flag is 1 and the message is a sender message,then set the view visible
    //if none of the upper two case is fitted,then set the view gone
    visibility = if (
        messageType == MessageType.MESSAGE_RECEIVE && flag == 0 ||
        messageType == MessageType.MESSAGE_SEND && flag == 1
    ) {
        View.VISIBLE
    } else {
        View.GONE
    }
    view.visibility = visibility
}


