package com.hometemperature.ui.datacenter

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hometemperature.bean.item.DataItem
import com.hometemperature.database.AppRepository
import com.hometemperature.network.NetWorkServiceFactory
import kotlinx.coroutines.launch

class DataCenterViewModel(private val application: Application) : ViewModel() {
    private val _repository: AppRepository = AppRepository.getInstance(
        NetWorkServiceFactory().buildIotConnectionService(application),
        NetWorkServiceFactory().buildIotTransmissionService()
    )
    var repository: AppRepository = _repository

    //检查网络状态，暂存网络状态
    private val _networkStatus = _repository.networkStatus
    val networkStatus: LiveData<String>
        get() = _networkStatus

    //数据接收缓存
    private val _dataReceiveCache = _repository.dataReceiveCache
    val dataReceiveCache: LiveData<String>
        get() = _dataReceiveCache

    //数据发送缓存
    private val _dataSendCache = _repository.dataSendCache
    val dataSendCache: LiveData<String>
        get() = _dataSendCache

    //发送和接受数据的列表
    private val _dataList = repository.dataList
    var dataList: LiveData<MutableList<DataItem>> = _dataList

    //发送数据到已连接的主机。注意：该方法为网络方法，要在网络进程中处理
    fun sendDataToHost(repository: AppRepository) {
        //发起携程，从IO线程处理
        viewModelScope.launch {
            repository.sendData(repository)
        }
    }

    //将接收和发送的数据存入列表
    fun addDataToDataList(repository: AppRepository, dataItem: DataItem) {
        repository.addDataItemToList(dataItem)
    }

    //修改发送缓存，相当于去直接发送数据
    fun modifyReceiveCache(data: String) {
        repository.setDataReceiveCache(data)
    }

}