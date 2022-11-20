package com.hometemperature.bean

import android.net.wifi.ScanResult

//data class转换类，负责将一个data class转换为另一个，或者提取数据并映射
class DataTransferObject {
    companion object {
        //使用map映射将每个对象转化为另一个对象
        fun asWifiItem(scanResult: List<ScanResult>): List<WifiItem> {
            return scanResult.map {
                WifiItem(
                    name = it.SSID,
                    address = it.BSSID,
                    signalStrength = it.capabilities,
                    frequency = it.frequency,
                    isConnected = "未连接"
                )
            }
        }
    }
}