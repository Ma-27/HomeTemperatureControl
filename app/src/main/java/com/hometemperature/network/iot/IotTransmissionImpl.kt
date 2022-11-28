package com.hometemperature.network.iot

import com.hometemperature.bean.flag.TransmissionStatus
import com.hometemperature.database.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

class IotTransmissionImpl : IotTransmission {
    lateinit var dataIn: String
    lateinit var dataOut: String

    //当接收到数据时，在此处理。FIXME 此处可能有socket空异常
    override suspend fun onReceiveData(repository: AppRepository) {
        val socket = repository.socket.value
        // 获取输入流.
        val inputStream: InputStream =
            withContext(Dispatchers.IO) {
                socket!!.getInputStream()
            }
        // 创建缓冲区
        val reader = BufferedReader(InputStreamReader(inputStream))
        val builder = StringBuilder()
        var line: String? = ""

        //一行行读取输入的数据并且构建字符串
        //while ((line = reader.readLine()) != null) 简化写法
        while (builder.isEmpty()) {
            withContext(Dispatchers.IO) {
                line = reader.readLine()
                builder.append(line)
                //添加空行，让debug很容易
                builder.append("\n")
            }
            break
        }

        dataIn = builder.toString()
        Timber.d("onStartConnection成功接收到的数据如下：$dataIn")
        repository.setDataReceiveCache(dataIn)
    }

    //当发送数据时，在此处理。FIXME 此处可能有socket空异常
    override suspend fun onSendData(repository: AppRepository): Int {
        //检查连接状态，如果没有连接就发送数据，则退出函数
        if (repository.wifiItem.value!!.isConnected != "已连接") {
            Timber.e("未连接到wifi就尝试发送数据")
            return TransmissionStatus.FAIL
        }

        dataOut = repository.dataSendCache.toString()
        val socket = repository.socket.value
        // 获取输出流.
        val outputStream: OutputStream = withContext(Dispatchers.IO) {
            socket!!.getOutputStream()
        }
        //向外写入字节数组（字节流）
        withContext(Dispatchers.IO) {
            outputStream.write(dataOut.toByteArray())
            //清除缓存区
            outputStream.flush()
            //关闭连接
            outputStream.close()
        }
        Timber.d("onStartConnection成功发送的数据如下：${dataOut.toByteArray()}")

        //确认传输成功
        //返回传输结果
        return TransmissionStatus.SUCCESS
    }

    //获取以秒做单位的时间戳
    private fun getTime(): Int {
        val timeStamp = System.currentTimeMillis()
        val timeStampSec = (timeStamp / 1000).toInt()
        Timber.d("时间戳$timeStampSec")
        return timeStampSec
    }
}