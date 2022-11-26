package com.hometemperature.util.itembuild

import com.hometemperature.bean.flag.NetWorkDefaultConfiguration
import com.hometemperature.bean.item.WifiItem

class WifiItemBuilder {
    companion object {
        fun buildWifiItem(): WifiItem {
            return WifiItem(
                "ERROR",
                "no",
                "1",
                1,
                "no",
                "no",
                NetWorkDefaultConfiguration.DEFAULT_MAC_ADDRESS,
                NetWorkDefaultConfiguration.DEFAULT_IP_ADDRESS,
                "",
                NetWorkDefaultConfiguration.DEFAULT_PORT_NUMBER,
                1,
                true
            )
        }
    }
}