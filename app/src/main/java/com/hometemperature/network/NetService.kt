package com.hometemperature.network

import com.hometemperature.bean.ApplicationViewModel
import com.hometemperature.bean.WifiItem

interface NetService {
    //检查wifi状态
    fun checkNetworkState(viewModel: ApplicationViewModel)

    //根据指定的item信息，连接到的wifi
    fun connectToSpecifiedWifi(viewModel: ApplicationViewModel, item: WifiItem)

    //断开现在的wifi连接
    fun disConnectWifi(viewModel: ApplicationViewModel)
}
