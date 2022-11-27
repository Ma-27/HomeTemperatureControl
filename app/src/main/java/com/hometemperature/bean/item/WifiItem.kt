package com.hometemperature.bean.item

import com.hometemperature.bean.flag.NetWorkDefaultConfiguration

data class WifiItem(
    val wifiName: String,
    val apMacAddress: String,
    val signalStrength: String,
    val frequency: Int,
    var isConnected: String,
    var password: String,
    val deviceMacAddress: String = NetWorkDefaultConfiguration.DEFAULT_MAC_ADDRESS,
    var deviceIpAddress: String,
    val apIpAddress: String,
    var portNumber: Int,
) {
    //接入点name为wifi名称，为ssid
    //接入点mac地址，变量名apMacAddress，为bssid
    //FIXME ，这里的设备mac地址写死了，实际情况下待wifi进行分配
}
