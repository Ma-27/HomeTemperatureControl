package com.hometemperature.bean

data class WifiItem(
    val name: String,
    val address: String,
    val signalStrength: String,
    val frequency: Int,
    val isConnected: String
) {
    //接入点名称address，为bssid
}
