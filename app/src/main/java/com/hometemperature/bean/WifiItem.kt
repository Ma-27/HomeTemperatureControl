package com.hometemperature.bean

data class WifiItem(
    val wifiName: String,
    val apMacAddress: String,
    val signalStrength: String,
    val frequency: Int,
    val isConnected: String,
    var password: String,
    val deviceMacAddress: String = "ff:ff:ff:00:00:00",
    val deviceIpAddress: String,
    val apIpAddress: String,
    var portNumber: Int,
    val destinationPortNumber: Int,
    var isAutoPortNumber: Boolean
) {
    //接入点name为wifi名称，为ssid
    //接入点mac地址，变量名apMacAddress，为bssid
    //FIXME ，这里的设备mac地址写死了，实际情况下待wifi进行分配
}
