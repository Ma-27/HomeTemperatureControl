package com.hometemperature.ui.console

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hometemperature.database.AppRepository
import com.hometemperature.network.NetWorkServiceFactory

class ConsoleViewModel(application: Application) : AndroidViewModel(application) {
    private val _repository: AppRepository = AppRepository.getInstance(
        NetWorkServiceFactory().buildIotConnectionService(application),
        NetWorkServiceFactory().buildIotTransmissionService()
    )
    var repository: AppRepository = _repository

    //TODO 缓存温度调节的目标温度
    private val _targetTemperature = MutableLiveData<Float>().apply {
        value = 15.0f
    }
    val targetTemperature: LiveData<Float>
        get() = _targetTemperature

    //TODO 缓存温度调节的当前温度
    private val _currentTemperature = MutableLiveData<Float>().apply {
        value = 18.0f
    }
    val currentTemperature: LiveData<Float>
        get() = _currentTemperature

    //提示网络状态信息
    fun setNetWorkStatus(message: String) {
        repository.setNetWorkStatus(message)
    }

    //将接收和发送的数据存入列表
    fun setTargetTemperature(temperature: Float) {
        _targetTemperature.value = temperature
    }
}