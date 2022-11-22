package com.hometemperature.bean

//字段类，通过字段常量选择网络模式，选择IOT模式(2)为物联网模式，wifi接入局域网和传感器网络；当选择(1)时接入互联网
class NetWorkStatic {
    companion object {
        val INTERNET_API_SERVICE = 1

        val IOT_WLAN_COMMUNICATION_SERVICE = 2
    }
}