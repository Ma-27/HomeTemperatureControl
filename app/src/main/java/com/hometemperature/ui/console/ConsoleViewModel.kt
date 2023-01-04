package com.hometemperature.ui.console

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hometemperature.bean.flag.MessageType
import com.hometemperature.bean.flag.TransmissionStatus
import com.hometemperature.database.AppRepository
import com.hometemperature.network.NetWorkServiceFactory
import com.hometemperature.util.itembuild.DataItemBuilder

class ConsoleViewModel(application: Application) : AndroidViewModel(application) {
    private val _repository: AppRepository = AppRepository.getInstance(
        NetWorkServiceFactory().buildIotConnectionService(application),
        NetWorkServiceFactory().buildIotTransmissionService()
    )
    var repository: AppRepository = _repository

    //TODO 缓存温度调节的目标温度
    private val _targetTemperature = MutableLiveData<Int>().apply {
        value = 15
    }
    val targetTemperature: LiveData<Int>
        get() = _targetTemperature

    //TODO 缓存温度调节的当前温度
    private val _currentTemperature = _repository.currentTemperature
    val currentTemperature: LiveData<Float>
        get() = _currentTemperature

    //TODO 空调系统的开关
    private val _acSwitch = MutableLiveData<Boolean>().apply {
        value = false
    }
    val acSwitch: LiveData<Boolean>
        get() = _acSwitch

    //提示网络状态信息
    fun setNetWorkStatus(message: String) {
        repository.setNetWorkStatus(message)
    }

    //更改空调开启或者关闭
    fun acSwitchStatus(value: Boolean) {
        _acSwitch.value = value
    }

    //将接收和发送的数据存入列表
    fun setTargetTemperature(temperature: Int) {
        _targetTemperature.value = temperature
    }

    //向目标主机发送数据
    fun sendData(data: String) {
        //构建数据对象
        val dataItem = DataItemBuilder.buildDataItem(data)
        dataItem.messageType = MessageType.MESSAGE_SEND
        dataItem.messageStatus = TransmissionStatus.UNKNOWN
        dataItem.event = DataItemBuilder.determineEventString(data, MessageType.MESSAGE_SEND)
        //存入发送数据列表中，发送数据
        repository.addDataItemToList(dataItem)
    }
}