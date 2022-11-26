package com.hometemperature.network

import android.content.Context
import com.hometemperature.bean.flag.NetWorkStatic.Companion.IOT_WLAN_CONNECTION_SERVICE
import com.hometemperature.bean.flag.NetWorkStatic.Companion.IOT_WLAN_TRANSMISSION_SERVICE

//工厂方法，通过选择不同的网络类型返回不同的网络服务实例，屏蔽掉context和网络接口的具体实现
class NetWorkServiceFactory {
    private val INSTANCE_CONNECTION: IotConnectionImpl? = null
    private val INSTANCE_TRANSMISSION: IotTransmissionImpl? = null

    fun build(type: Int, context: Context): NetWorkService {
        return when (type) {
            IOT_WLAN_CONNECTION_SERVICE -> {
                //单例模式，确保只有一个服务类在运行
                return buildIotConnectionService(context)
            }
            IOT_WLAN_TRANSMISSION_SERVICE -> {
                //单例模式，确保只有一个服务类在运行
                return buildIotTransmissionService(context)
            }

            else -> IotConnectionImpl(context)
        }
    }

    fun buildIotConnectionService(context: Context): IotConnectionImpl {
        synchronized(this) {
            var instance = INSTANCE_CONNECTION

            if (instance == null) {
                instance = IotConnectionImpl(context)
            }

            // 返回一个实例
            return instance
        }
    }

    fun buildIotTransmissionService(context: Context): IotTransmissionImpl {
        synchronized(this) {
            var instance = INSTANCE_TRANSMISSION

            if (instance == null) {
                instance = IotTransmissionImpl(context)
            }

            // 返回一个实例
            return instance
        }
    }
}