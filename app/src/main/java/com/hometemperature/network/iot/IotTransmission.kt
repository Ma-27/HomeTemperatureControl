package com.hometemperature.network.iot

import com.hometemperature.database.AppRepository
import com.hometemperature.network.NetWorkService

//数据传输接口，通过这里传输数据
interface IotTransmission : NetWorkService {
    suspend fun onReceiveData(repository: AppRepository)

    suspend fun onSendData(repository: AppRepository): Int
}
