package com.hometemperature.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.hometemperature.bean.item.WifiItem
import com.hometemperature.database.AppRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val _repository: AppRepository = AppRepository.getInstance(application)
    var repository: AppRepository = _repository

    //菜单中刷新按钮是否按下
    private val _refreshIsChecked = _repository.refreshIsChecked
    val refreshIsChecked: LiveData<Boolean>
        get() = _refreshIsChecked

    //检查网络状态，暂存网络状态
    private val _networkStatus = _repository.networkStatus
    val networkStatus: LiveData<String>
        get() = _networkStatus

    //菜单中显示的wifi列表
    private val _wifiList = _repository.wifiList
    var wifiList: LiveData<List<WifiItem>> = _wifiList


    /**
     * 各自字段的setter方法
     */

    //按下refresh按钮之后
    fun setRefreshChecked() {
        _repository.setRefreshChecked()
    }

    //停止refresh按钮之后
    fun clearRefreshChecked() {
        _repository.clearRefreshChecked()
    }

    fun setNetWorkStatus(value: String) {
        _repository.setNetWorkStatus(value)
    }

    fun setWifiList(list: List<WifiItem>) {
        _repository.setWifiList(list)
    }

    fun setWifiItem(item: WifiItem) {
        _repository.setWifiItem(item)
    }


    //获取wifi列表
    fun checkNetworkState(repository: AppRepository) {
        repository.checkNetWorkState(repository)
    }

    //连接到给定的wifi
    fun connectToSpecifiedWifi(item: WifiItem, repository: AppRepository) {
        repository.connectToSpecifiedWifi(item, repository)
    }

    //断开与现有wifi的连接
    fun disConnectWifi(repository: AppRepository) {
        repository.disConnectWifi(repository)
    }


    init {

    }
}