package com.hometemperature.network.iot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.PatternMatcher
import com.hometemperature.R
import com.hometemperature.bean.item.WifiItem
import com.hometemperature.database.AppRepository
import com.hometemperature.util.DataTransferObject
import timber.log.Timber
import java.net.SocketException

class IotConnectionImpl(private val context: Context) : IotConnection {
    //初始化读取网络状态
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    //后台刷新wifi列表
    override fun checkNetworkState(repository: AppRepository) {
        //注册broadcast receiver并接受返回的wifi列表
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    //提示刷新成功
                    repository.setNetWorkStatus("刷新成功")
                    //解析刷新结果
                    val resultsList = wifiManager.scanResults
                    //在repository 的live data中存入数据
                    repository.setWifiList(DataTransferObject.asWifiItem(resultsList))
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
            repository.setNetWorkStatus("刷新wifi列表失败，请检查网络状态")
            Timber.w("刷新wifi列表失败，请检查网络状态")
        }

    }

    override fun connectToSpecifiedWifi(item: WifiItem, repository: AppRepository) {
        repository.setWifiItem(item)
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
                //绑定该app的网络服务到该网络，自此该网络的流量由此WLAN收发
                connectivityManager.bindProcessToNetwork(network)
                //初始化套接字
                initializeServerSocket(network, repository)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                //提示连接失败
                repository.setNetWorkStatus("连接该WLAN失败，请重试")
            }
        }
        //连接到网络
        connectivityManager.requestNetwork(request, networkCallback)
    }

    //FIXME 从android10以上，disconnect被禁用 该方法不适用于该版本之上
    override fun disConnectWifi(repository: AppRepository) {
        wifiManager.disconnect()
        repository.setNetWorkStatus("已断开WLAN连接")
    }

    //初始化端口和套接字
    fun initializeServerSocket(network: Network, repository: AppRepository) {
        //使用try catch防止用户输入错误的ip地址程序崩溃
        try {
            //创建socket并且连接。创建套接字并将其连接到指定地址的指定端口号。这个套接字是使用为这个工厂建立的套接字选项配置的。
            val socket = network.socketFactory.createSocket(
                repository.wifiItem.value!!.deviceIpAddress,
                repository.wifiItem.value!!.portNumber
            )

            socket.let {
                //修改保存的网络信息为已连接，此行必须在set之前
                repository.wifiItem.value!!.isConnected = "已连接"
                //保存socket
                repository.setSocket(socket)
                //提示连接成功
                repository.setNetWorkStatus(context.getString(R.string.connection_succeed))
                Timber.d("成功连接指定wifi")
            }
        } catch (e: SocketException) {
            repository.setNetWorkStatus("连接被拒绝,目标主机可能未准备好，请等待目标主机或者忘记网络后重试")
            Timber.e("连接被拒绝,目标主机可能未准备好，请等待目标主机或者忘记网络后重试")
            Timber.e(repository.wifiItem.value!!.deviceIpAddress)
            Timber.e(repository.wifiItem.value!!.portNumber.toString())
            e.printStackTrace()
            Timber.e(e.message)
        } catch (exception: Exception) {
            repository.setNetWorkStatus("在初始化端口时遇到了未知的错误")
            Timber.e("在初始化端口时遇到了未知的错误")
            exception.printStackTrace()
            Timber.e(exception.message)
        } finally {
            //一般都不会出现这个exception
            //出现这个就说明当前设备需要忘记网络，不能重复连接
            // failed to connect to /192.168.4.1 (port 8080) from /:: (port 57752): connect failed: ECONNABORTED (Software caused connection abort)
        }
    }
}