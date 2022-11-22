package com.hometemperature.network

import com.hometemperature.bean.ApplicationViewModel
import com.hometemperature.bean.WifiItem

//通过url访问互联网
class InternetApiService : NetService {
    //retrofit
    override fun checkNetworkState(viewModel: ApplicationViewModel) {
        TODO("Not yet implemented")
    }

    override fun connectToSpecifiedWifi(viewModel: ApplicationViewModel, item: WifiItem) {
        TODO("Not yet implemented")
    }

    override fun disConnectWifi(viewModel: ApplicationViewModel) {
        TODO("Not yet implemented")
    }


}