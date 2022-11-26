package com.hometemperature.network

import android.content.Context
import com.hometemperature.database.AppRepository
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class IotTransmissionImpl(context: Context) : IotTransmission {
    //当开始传输数据时
    override fun onStartConnection(repository: AppRepository) {
        val socket = repository.socket.value
        val inputstrae: InputStream = socket!!.getInputStream()
        val inputStreamReader = InputStreamReader(inputstrae)
        val br = BufferedReader(inputStreamReader)
        //读取客户端数据
        var data: String = br.readLine()
        while (data.isNotEmpty()) {
            System.out.println("服务器接收到客户端的数据：$data")
        }

        Timber.d("成功输入数据")
        repository.setNetWorkStatus(data)
        //关闭输入流
        socket.shutdownInput()

    }

    override fun onReceiveData(repository: AppRepository) {
        TODO("Not yet implemented")
    }

    override fun onSendData(repository: AppRepository) {
        TODO("Not yet implemented")
    }

    override fun onCloseConnection(repository: AppRepository) {
        TODO("Not yet implemented")
    }

}