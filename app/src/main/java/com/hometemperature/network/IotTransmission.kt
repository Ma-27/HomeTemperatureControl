package com.hometemperature.network

import com.hometemperature.database.AppRepository

//数据传输接口，通过这里传输数据
interface IotTransmission : NetWorkService {
    fun onStartConnection(repository: AppRepository)

    fun onReceiveData(repository: AppRepository)

    fun onSendData(repository: AppRepository)

    fun onCloseConnection(repository: AppRepository)
}
