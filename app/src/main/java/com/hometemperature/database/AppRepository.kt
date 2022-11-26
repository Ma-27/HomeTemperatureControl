package com.hometemperature.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hometemperature.bean.item.WifiItem
import com.hometemperature.network.IotConnection
import com.hometemperature.network.IotTransmission
import com.hometemperature.network.NetWorkServiceFactory
import com.hometemperature.util.itembuild.WifiItemBuilder
import timber.log.Timber
import java.net.Socket

/*
app repository是整个app的数据中心，所有app的全局数据都暂存在这里。
同时，该类负责处理多线程问题，比如数据库存取和网络请求。同时也是多线程任务的管理中心
 */
class AppRepository(context: Context) {
    //物联网模块的连接处理对象
    private var iotConnection: IotConnection

    //物联网模块的数据发送处理对象
    private var iotTransmission: IotTransmission

    companion object {
        //volatile注释不允许缓存该变量
        @Volatile
        private var INSTANCE: AppRepository? = null

        //App repository的实例化方法，采用单例模式，整个app只允许存在一个app repository实例（懒汉饿汉、线程安全）
        fun getInstance(context: Context): AppRepository {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = AppRepository(context)
                }

                // 返回一个Repository实例
                return instance
            }
        }
    }

    //TODO repository类初始化，即配置网络服务
    init {
        //创建网络连接服务
        iotConnection =
            context.let {
                NetWorkServiceFactory().buildIotConnectionService(it)
            }
        //创建网络数据传输服务
        iotTransmission =
            context.let {
                NetWorkServiceFactory().buildIotTransmissionService(it)
            }

    }

    //TODO 菜单中刷新按钮是否按下
    private val _refreshIsChecked = MutableLiveData<Boolean>().apply {
        value = false
    }
    val refreshIsChecked: LiveData<Boolean>
        get() = _refreshIsChecked

    //TODO 菜单中显示的wifi列表
    private val _wifiList = MutableLiveData<List<WifiItem>>().apply {
        value = emptyList<WifiItem>()
    }
    var wifiList: LiveData<List<WifiItem>> = _wifiList

    //TODO 检查网络状态，暂存网络状态
    private val _networkStatus = MutableLiveData<String>().apply {
        value = "向下滑动来刷新WLAN列表"
    }
    val networkStatus: LiveData<String>
        get() = _networkStatus

    //TODO 特定wifi的属性集合
    private val _wifiItem = MutableLiveData<WifiItem>().apply {
        value = WifiItemBuilder.buildWifiItem()
    }
    val wifiItem: LiveData<WifiItem>
        get() = _wifiItem

    //TODO socket设置
    private val _socket = MutableLiveData<Socket>().apply {
        value = Socket()
    }
    val socket: LiveData<Socket>
        get() = _socket


    /**
     * 各自字段的setter方法
     */
    fun setNetWorkStatus(value: String) {
        _networkStatus.postValue(value)
    }

    fun setWifiList(list: List<WifiItem>) {
        _wifiList.postValue(list)
    }

    fun setWifiItem(item: WifiItem) {
        _wifiItem.postValue(item)
    }

    //按下refresh按钮之后
    fun setRefreshChecked() {
        _refreshIsChecked.value = true
    }

    //停止refresh按钮之后
    fun clearRefreshChecked() {
        _refreshIsChecked.value = false
    }

    fun setSocket(value: Socket) {
        _socket.postValue(value)
    }


    /***
     * 预留其他类access该类并修改数据的接口，通常从view model类中access此类
     */
    //检查网络状态
    fun checkNetWorkState(repository: AppRepository) {
        //调用网络模块进行刷新
        iotConnection.checkNetworkState(repository)
    }

    //连接到指定wifi，wifiItem中含有wifi参数
    fun connectToSpecifiedWifi(item: WifiItem, repository: AppRepository) {
        iotConnection.connectToSpecifiedWifi(item, repository)
        if (socket.value != null && networkStatus.value == "连接WLAN成功") {
            //接收连接成功后刚发过来的数据
            iotTransmission.onStartConnection(this)
        } else {
            Timber.w("repository中连接到指定wifi后，socket为空")
        }
    }

    //断开与现在的wifi的连接
    fun disConnectWifi(repository: AppRepository) {
        iotConnection.disConnectWifi(repository)
    }
}