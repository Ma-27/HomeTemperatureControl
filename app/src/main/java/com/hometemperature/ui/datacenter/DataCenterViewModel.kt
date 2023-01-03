package com.hometemperature.ui.datacenter

import android.app.Application
import androidx.lifecycle.*
import com.hometemperature.bean.item.DataItem
import com.hometemperature.database.AppRepository
import com.hometemperature.network.NetWorkServiceFactory
import kotlinx.coroutines.launch
import timber.log.Timber

class DataCenterViewModel(application: Application) : AndroidViewModel(application) {
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

    //最新一个item的的时间戳，用来防止重复发送数据
    private val _latestItemTimestamp = MutableLiveData<Long>().apply {
        value = 0
    }
    val latestItemTimestamp: LiveData<Long>
        get() = _latestItemTimestamp

    //发送和接受数据的列表
    private val _dataList = repository.dataList
    var dataList: LiveData<MutableList<DataItem>> = _dataList

    //发送数据到已连接的主机。注意：该方法为网络方法，要在网络进程中处理
    fun sendDataToHost(repository: AppRepository) {
        viewModelScope.launch {
            repository.sendData(repository)
        }
    }

    //将接收和发送的数据存入列表
    fun addDataToDataList(repository: AppRepository, dataItem: DataItem) {
        repository.addDataItemToList(dataItem)
    }

    //修改发送缓存，相当于去直接发送数据
    fun modifySendCache(data: String) {
        repository.setDataSendCache(data)
    }

    //修改发送时间，避免重新发起数据发送请求
    fun modifyLatestTimestamp(time: Long) {
        _latestItemTimestamp.postValue(time)
        Timber.d("修改发送时间$time")
    }

    //获取以秒做单位的时间戳
    private fun getTime(): Int {
        val timeStamp = System.currentTimeMillis()
        val timeStampSec = (timeStamp / 1000).toInt()
        Timber.d("时间戳$timeStampSec")
        return timeStampSec
    }
}