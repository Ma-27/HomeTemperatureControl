package com.hometemperature.network.iot

import com.hometemperature.bean.item.WifiItem
import com.hometemperature.database.AppRepository
import com.hometemperature.network.NetWorkService

//网络请求中连接wifi的接口
interface IotConnection : NetWorkService {
    //检查wifi状态
    fun checkNetworkState(repository: AppRepository)

    //根据指定的item信息，连接到的wifi
    fun connectToSpecifiedWifi(item: WifiItem, repository: AppRepository)

    //断开现在的wifi连接
    fun disConnectWifi(repository: AppRepository)
}
