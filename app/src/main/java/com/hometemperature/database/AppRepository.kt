package com.hometemperature.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hometemperature.bean.flag.TestFlag
import com.hometemperature.bean.item.DataItem
import com.hometemperature.bean.item.WifiItem
import com.hometemperature.network.iot.IotConnection
import com.hometemperature.network.iot.IotTransmission
import com.hometemperature.util.itembuild.WifiItemBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.Socket

/*
app repository是整个app的数据中心，所有app的全局数据都暂存在这里。
同时，该类负责处理多线程问题，比如数据库存取和网络请求。同时也是多线程任务的管理中心
 */
//物联网模块的连接处理对象
//物联网模块的数据发送处理对象
class AppRepository(
    private val iotConnection: IotConnection,
    private val iotTransmission: IotTransmission
) {

    companion object {
        //volatile注释不允许缓存该变量
        @Volatile
        private var INSTANCE: AppRepository? = null

        //App repository的实例化方法，采用单例模式，整个app只允许存在一个app repository实例（懒汉饿汉、线程安全）
        fun getInstance(
            iotConnection: IotConnection,
            iotTransmission: IotTransmission
        ): AppRepository {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = AppRepository(iotConnection, iotTransmission)
                    INSTANCE = instance
                }

                // 返回一个Repository实例
                return instance
            }
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

    //TODO SnackBar提示字符串变量
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

    //TODO 数据中心中显示的数据列表
    //为了应对set value 导致的list同步问题，如果需要更改该list，需要同步更改mdataList和dataList
    private val mdataList: MutableList<DataItem> = ArrayList()
    private val _dataList = MutableLiveData<MutableList<DataItem>>().apply {
        value = mdataList
    }
    var dataList: LiveData<MutableList<DataItem>> = _dataList

    //TODO 要接收的数据缓存（相比起数据列表更容易access）
    private val _dataReceiveCache = MutableLiveData<String>().apply {
        value = TestFlag.RECEIVE
    }
    val dataReceiveCache: LiveData<String>
        get() = _dataReceiveCache

    //TODO 要发送的数据缓存（相比起数据列表更容易access）
    private val _dataSendCache = MutableLiveData<String>().apply {
        value = TestFlag.SEND
    }
    val dataSendCache: LiveData<String>
        get() = _dataSendCache

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

    //连接到wifi之后，设置端口
    fun setSocket(value: Socket) {
        _socket.postValue(value)
    }

    //更改接收缓存，提示收到数据了
    fun setDataReceiveCache(data: String) {
        _dataReceiveCache.postValue(data)
    }

    //在数据中心的数据列表中添加数据
    // 为了应对线程不同步问题，先从mdataList中添加元素，再将修改后的整个list传递给datalist
    fun addDataItemToList(dataItem: DataItem) {
        Timber.d("add data item to list")
        mdataList.add(dataItem)
        _dataList.postValue(mdataList)
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
    }

    //断开与现在的wifi的连接
    fun disConnectWifi(repository: AppRepository) {
        iotConnection.disConnectWifi(repository)
    }

    //提醒repository，设备已经接收到wifi发来的数据了
    suspend fun notifyDataReceiving(repository: AppRepository) {
        withContext(Dispatchers.IO) {
            iotTransmission.onReceiveData(repository)
        }
    }

    //提醒repository socket已经改变
    suspend fun notifySocketChanged(repository: AppRepository) {
        //在本app中，socket是连接的最后一步，如果成功生成或者更改了socket，则可以监听数据发送，这里需要提示刷新数据
        notifyDataReceiving(repository)
    }

    //向目标主机发送数据
    suspend fun sendData(repository: AppRepository) {
        withContext(Dispatchers.IO) {
            iotTransmission.onSendData(repository)
            //发送数据方法：必须将数据存入repository send cache中
        }
    }
}