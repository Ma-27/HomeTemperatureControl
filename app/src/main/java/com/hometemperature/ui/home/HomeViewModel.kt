package com.hometemperature.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hometemperature.bean.WifiItem

class HomeViewModel : ViewModel() {

    private val _wifiList = MutableLiveData<List<WifiItem>>().apply {
        value = emptyList<WifiItem>()
    }
    var wifiList: LiveData<List<WifiItem>> = _wifiList
        get() = _wifiList

    private val _refreshIsChecked = MutableLiveData<Boolean>().apply {
        value = false
    }
    var refreshIsChecked: LiveData<Boolean> = _refreshIsChecked
        get() = _refreshIsChecked

    fun setWifiList(list: List<WifiItem>) {
        _wifiList.value = list
    }

    fun setRefreshChecked() {
        _refreshIsChecked.value = true
    }

    fun clearRefreshChecked() {
        _refreshIsChecked.value = false
    }

    init {

    }
}