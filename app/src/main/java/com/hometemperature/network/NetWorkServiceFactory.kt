package com.hometemperature.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.hometemperature.bean.flag.NetWorkStatic.Companion.IOT_WLAN_CONNECTION_SERVICE
import com.hometemperature.bean.flag.NetWorkStatic.Companion.IOT_WLAN_TRANSMISSION_SERVICE
import com.hometemperature.network.iot.IotConnectionImpl
import com.hometemperature.network.iot.IotTransmissionImpl

//工厂方法，通过选择不同的网络类型返回不同的网络服务实例，屏蔽掉context和网络接口的具体实现
class NetWorkServiceFactory {
    companion object {
        //volatile注释不允许缓存该变量
        @Volatile
        private var INSTANCE_CONNECTION: IotConnectionImpl? = null

        @Volatile
        private var INSTANCE_TRANSMISSION: IotTransmissionImpl? = null

        fun build(type: Int, context: Context): NetWorkService {
            return when (type) {
                IOT_WLAN_CONNECTION_SERVICE -> {
                    //单例模式，确保只有一个服务类在运行
                    return buildIotConnectionService(context)
                }
                IOT_WLAN_TRANSMISSION_SERVICE -> {
                    //单例模式，确保只有一个服务类在运行
                    return buildIotTransmissionService()
                }

                else -> buildIotConnectionService(context)
            }
        }

        fun buildIotConnectionService(context: Context): IotConnectionImpl {
            //初始化读取网络状态
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (INSTANCE_CONNECTION == null) {
                synchronized(this) {
                    if (INSTANCE_CONNECTION == null) {
                        INSTANCE_CONNECTION = IotConnectionImpl(wifiManager, connectivityManager)
                    }
                }
            }
            // 返回一个实例
            return INSTANCE_CONNECTION!!
        }

        fun buildIotTransmissionService(): IotTransmissionImpl {
            if (INSTANCE_TRANSMISSION == null) {
                synchronized(this) {
                    if (INSTANCE_TRANSMISSION == null) {
                        INSTANCE_TRANSMISSION = IotTransmissionImpl()
                    }
                }
            }
            // 返回一个实例
            return INSTANCE_TRANSMISSION!!
        }
    }
}