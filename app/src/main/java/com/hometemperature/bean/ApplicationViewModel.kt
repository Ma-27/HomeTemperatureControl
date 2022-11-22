package com.hometemperature.bean

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class ApplicationViewModel : ViewModel() {
    //TODO 检查网络状态，暂存网络状态
    private val _networkStatus = MutableLiveData<String>().apply {
        value = "向下滑动来刷新WLAN列表"
    }
    var refreshStatus: LiveData<String> = _networkStatus
        get() = _networkStatus

    //TODO 菜单中显示的wifi列表
    private val _wifiList = MutableLiveData<List<WifiItem>>().apply {
        value = emptyList<WifiItem>()
    }
    var wifiList: LiveData<List<WifiItem>> = _wifiList
        get() = _wifiList


    /**
     * 各自字段的setter方法
     */
    fun setNetWorkStatus(value: String) {
        _networkStatus.postValue(value)
    }

    fun setWifiList(list: List<WifiItem>) {
        _wifiList.postValue(list)
    }

}