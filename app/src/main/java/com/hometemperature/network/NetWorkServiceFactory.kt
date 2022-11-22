package com.hometemperature.network

import android.content.Context
import com.hometemperature.bean.NetWorkStatic.Companion.INTERNET_API_SERVICE
import com.hometemperature.bean.NetWorkStatic.Companion.IOT_WLAN_COMMUNICATION_SERVICE

//工厂方法，通过选择不同的网络类型返回不同的网络服务实例，屏蔽掉context和网络接口的具体实现
class NetWorkServiceFactory {
    fun build(type: Int, context: Context): NetService {
        return when (type) {
            IOT_WLAN_COMMUNICATION_SERVICE -> {
                return IotWifiService(context)
            }
            INTERNET_API_SERVICE -> {
                InternetApiService()
            }
            else -> InternetApiService()
        }
    }
}