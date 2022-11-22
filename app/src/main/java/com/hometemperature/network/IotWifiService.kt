package com.hometemperature.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.PatternMatcher
import com.hometemperature.bean.ApplicationViewModel
import com.hometemperature.bean.WifiItem
import com.hometemperature.util.DataTransferObject
import timber.log.Timber

class IotWifiService(private val context: Context) : NetService {
    //初始化读取网络状态
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    //响应刷新命令
    override fun checkNetworkState(viewModel: ApplicationViewModel) {
        //注册broadcast receiver并接受返回的wifi列表
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    //提示刷新成功
                    viewModel.setNetWorkStatus("刷新成功")
                    //解析刷新结果
                    val resultsList = wifiManager.scanResults
                    //在view model中存入数据
                    viewModel.setWifiList(DataTransferObject.asWifiItem(resultsList))
                }
            }
        }

        //添加broadcast receiver响应刷新命令
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)

        //开始扫描网络
        val success = wifiManager.startScan()

        // 扫描失败后报错
        if (!success) {
            viewModel.setNetWorkStatus("刷新wifi列表失败，请检查网络状态")
            Timber.d("刷新wifi列表失败，请检查网络状态")
        }

        //log记录按钮按下，已经刷新
        Timber.d("刷新wifi按钮已按下")
    }

    override fun connectToSpecifiedWifi(viewModel: ApplicationViewModel, item: WifiItem) {
        //网络过滤器，筛选能连接的网络和设备
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsidPattern(PatternMatcher(item.wifiName, PatternMatcher.PATTERN_PREFIX))
            .setBssidPattern(
                MacAddress.fromString(item.apMacAddress),
                MacAddress.fromString(item.deviceMacAddress)
            )
            .build()

        //构建网络请求和连接方式
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                //数据传递

                //提示已经成功连接
                viewModel.setNetWorkStatus("设备已经成功连接该WLAN")
            }

            override fun onUnavailable() {
                super.onUnavailable()
                //提示连接失败
                viewModel.setNetWorkStatus("连接该WLAN失败，请重试")
            }
        }
        //连接到网络
        connectivityManager.requestNetwork(request, networkCallback)
    }

    override fun disConnectWifi(viewModel: ApplicationViewModel) {
        wifiManager.disconnect()
        viewModel.setNetWorkStatus("已断开WLAN连接")
    }
}