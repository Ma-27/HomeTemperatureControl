package com.hometemperature.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.hometemperature.R
import com.hometemperature.bean.flag.MessageType
import com.hometemperature.bean.flag.TestFlag
import com.hometemperature.bean.flag.TransmissionStatus
import com.hometemperature.databinding.ActivityMainBinding
import com.hometemperature.databinding.DialogDataSendBinding
import com.hometemperature.util.itembuild.DataItemBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    //部分live data变量更新次数统计，初始为0，每次使用+1(防止bug)
    var usageCount: Int = 0
    var usageCount1: Int = 0

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var dataSendBinding: DialogDataSendBinding
    private val viewModel: MainActivityViewModel by viewModels()

    private val REQUEST_ACCESS_NETWORK_STATE = 0
    private val REQUEST_INTERNET_STATE = 1
    private val REQUEST_ACCESS_WIFI_STATE = 2
    private val REQUEST_ACCESS_FINE_LOCATION = 3
    private val REQUEST_READ_PHONE_STATE = 4
    private val REQUEST_CHANGE_WIFI_STATE = 5

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //请求权限，处理权限问题
        permissionRequest()

        //初始化视图布局
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        setListener()


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_console, R.id.nav_datacenter
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    //设置点击事件和live data的监听
    @OptIn(DelicateCoroutinesApi::class)
    private fun setListener() {
        //主界面fab按钮，fab按钮按下后，app发送数据到指定主机
        binding.appBarMain.fab.setOnClickListener { view ->
            showDataSendingDetail()
        }

        //监视接收缓存，一旦收到数据就将数据存入数据列表中
        viewModel.dataReceiveCache.observe(this, Observer {
            //初始字符串就是 TestFlag.RECEIVE 如果相同说明未更新数据
            if (it != TestFlag.RECEIVE) {
                //新建一个data item
                val item = DataItemBuilder.buildDataItem(it)
                item.messageType = MessageType.MESSAGE_RECEIVE
                item.messageStatus = TransmissionStatus.SUCCESS
                //提示收到数据
                item.event = DataItemBuilder.determineEventString("1", MessageType.MESSAGE_RECEIVE)
                //将item添加到显示列表中去
                viewModel.addDataToDataList(item)
            }

            //一旦收到数据，再启动一个接收函数，负责下一次的数据接收。
            if (usageCount1 != 0 && viewModel.repository.wifiItem.value!!.isConnected == "已连接") {
                GlobalScope.launch {
                    viewModel.repository.notifyDataReceiving(viewModel.repository)
                }
            }
            usageCount1++
            if (usageCount1 > 9) {
                usageCount1 = 1
            }
        })


        //监视发送缓存，发送数据就是把待发数据存入发送缓存中，一旦缓存变更就发送
        viewModel.dataSendCache.observe(this, Observer {
            //防止空列表
            val dataItem =
                if (viewModel.dataList.value?.size != 0) viewModel.dataList.value?.get(
                    0
                ) else null

            //如果是创建发送缓存之初变化，则不发送数据，否则发送数据;
            //为了防止重复发送，如果在on start中live data收到更新，则不应该发送。这里检查时间戳。如果该数据要发送而尚未发送，该数据必然在排在最前面，使用get获取最前面的元素判断是否已发送。
            if (dataItem != null) {
                if (it != TestFlag.SEND &&
                    (dataItem.timestamp != viewModel.latestItemTimestamp.value || viewModel.latestItemTimestamp.value == 0L)
                ) {
                    //正式调用网络模块发送数据
                    viewModel.sendDataToHost()
                    //重置timestamp标志，用来做前一个的发送标志
                    dataItem.timestamp.let { it1 ->
                        viewModel.modifyLatestTimestamp(
                            it1
                        )
                    }
                }
            }
        })


        //监视列表，如果发现列表改变，查看是不是要发送数据（变更有两种可能，发送数据和新数据收到）,如果是发送数据，则存入数据到发送缓存中。
        viewModel.dataList.observe(this, Observer {
            if (it.isNotEmpty()) {
                //按照时间戳降序排列，这样第一个就是最新的一个
                it.sortByDescending { dataItem -> dataItem.timestamp }
                //选取最新更新的一个item，判断是不是更新的发出的数据
                val dataItem = it[0]
                //如果是要发出的数据，修改view model中发送缓存
                if (dataItem.messageType == MessageType.MESSAGE_SEND &&
                    (dataItem.timestamp != viewModel.latestItemTimestamp.value || viewModel.latestItemTimestamp.value == 0L)
                ) {
                    viewModel.modifySendCache(dataItem.data)
                    dataItem.messageStatus = TransmissionStatus.ON_SENDING
                }

                //如果是接收到的新数据，对新数据进行处理，但首先要防止重复添加数据。
                else if (dataItem.messageType == MessageType.MESSAGE_RECEIVE &&
                    (dataItem.timestamp != viewModel.latestItemTimestamp.value || viewModel.latestItemTimestamp.value == 0L)
                ) {
                    //修改时间为两者一样，防止下次重复添加，重复执行命令
                    viewModel.modifyLatestTimestamp(dataItem.timestamp)
                    Timber.d("接收到新数据")
                    //解码，翻译命令
                    //XXX 由于接收到的即为温度，则直接将内容提取出来并且转换就行了
                    viewModel.modifyCurrentTemperature(DataItemBuilder.formatTemperature(dataItem.data))
                }
            }
        })


        //刷新状态更新的snackBar提示
        viewModel.repository.networkStatus.observe(this, Observer {
            viewModel.repository.networkStatus.value?.let { it ->
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        })


    }

    //点击+号fab，发送数据的界面
    private fun showDataSendingDetail() {
        this.let {
            //对话框视图绑定
            dataSendBinding = DialogDataSendBinding.inflate(layoutInflater)
            //构建对话框
            MaterialAlertDialogBuilder(it)
                .setTitle("发送数据到主机：")
                //连接到这个wifi
                .setPositiveButton("发送") { dialog, which ->
                    //读取内容
                    val data = dataSendBinding.etWifiData.text.toString()
                    if (data.isNotEmpty()) {
                        //向主机发送数据
                        viewModel.sendData(data)
                    } else {
                        Timber.w("发送内容中无内容输入")
                        Toast.makeText(this, "无内容输入", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("取消") { dialog, which ->
                    //取消
                }
                .setView(dataSendBinding.root)
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    //手动检查更新wifi列表，通知view model已经刷新
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 如果被menu被刷新
            R.id.menu_refresh -> {
                Timber.d("刷新菜单被按下")
                // 通知live data改变刷新
                viewModel.repository.setRefreshChecked()
                return true
            }
        }

        // User didn't trigger a refresh, let the superclass handle this action
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private fun permissionRequest() {
        //申请网络状态权限
        val permissionNetworkState =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
        //获取wifi状态
        val permissionWifiAccess =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
        //获取精确定位权限，这样才能获取到附近的wifi
        val permissionFineLocation =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        //读取手机状态
        val permissionReadPhoneState =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        //修改wifi权限，允许app让设备连接到wifi
        val permissionChangeWifiAccess =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)

        //索要网络权限
        if (permissionNetworkState != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_NETWORK_STATE),
                REQUEST_INTERNET_STATE
            )
        } else {
            Timber.d("已授予互联网权限")
        }

        //索要wifi权限
        if (permissionWifiAccess != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_WIFI_STATE),
                REQUEST_ACCESS_WIFI_STATE
            )
        } else {
            Timber.d("已授予wifi权限")
        }

        //索要精度定位权限
        if (permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_ACCESS_FINE_LOCATION
            )
        } else {
            Timber.d("已授予高精度定位权限")
        }

        //索要读取手机状态权限
        if (permissionReadPhoneState != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_READ_PHONE_STATE
            )
        } else {
            Timber.d("已授予读取手机状态权限")
        }

        //索要修改wifi连接权限
        if (permissionChangeWifiAccess != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CHANGE_WIFI_STATE),
                REQUEST_CHANGE_WIFI_STATE
            )
        } else {
            Timber.d("已授予修改wifi连接权限")
        }
    }

    //权限请求和授予
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_INTERNET_STATE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.d("网络权限已经正常授予")
            }
            REQUEST_ACCESS_WIFI_STATE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.d("wifi权限已经正常授予")
            }
            REQUEST_ACCESS_FINE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.d("精确定位权限已经正常授予")
            }
            REQUEST_READ_PHONE_STATE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.d("读取手机状态权限已经正常授予")
            }
            REQUEST_CHANGE_WIFI_STATE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.d("连接特定wifi权限已经正常授予")
            }
            else -> {
                Toast.makeText(this, "请授权软件网络权限和位置信息权限", Toast.LENGTH_SHORT).show()
            }
        }
    }
}