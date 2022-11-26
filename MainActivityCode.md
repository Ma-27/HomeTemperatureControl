package com.hometemperature.ui

import android.Manifest import android.content.pm.PackageManager import android.os.Bundle import
android.view.Menu import android.view.MenuItem import android.widget.Toast import
androidx.appcompat.app.AppCompatActivity import androidx.core.app.ActivityCompat import
androidx.core.content.ContextCompat import androidx.drawerlayout.widget.DrawerLayout import
androidx.lifecycle.ViewModelProvider import androidx.navigation.findNavController import
androidx.navigation.ui.AppBarConfiguration import androidx.navigation.ui.navigateUp import
androidx.navigation.ui.setupActionBarWithNavController import
androidx.navigation.ui.setupWithNavController import
com.google.android.material.navigation.NavigationView import
com.google.android.material.snackbar.Snackbar import com.hometemperature.R import
com.hometemperature.database.AppRepository import
com.hometemperature.databinding.ActivityMainBinding import com.hometemperature.ui.home.HomeViewModel
import com.hometemperature.ui.home.HomeViewModelFactory import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var appRepository : AppRepository

    private val REQUEST_ACCESS_NETWORK_STATE = 0
    private val REQUEST_INTERNET_STATE = 1
    private val REQUEST_ACCESS_WIFI_STATE = 2
    private val REQUEST_ACCESS_FINE_LOCATION = 3
    private val REQUEST_READ_PHONE_STATE = 4
    private val REQUEST_CHANGE_WIFI_STATE = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //请求权限，处理权限问题
        permissionRequest()
        //初始化repository
        appRepository = AppRepository.getInstance(this)

        //TODO 初始化视图布局
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

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
                appRepository.setRefreshChecked()
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

        //索要网络权限,wifi权限
        if (permissionNetworkState  != PackageManager.PERMISSION_GRANTED &&
            permissionWifiAccess  != PackageManager.PERMISSION_GRANTED &&
            permissionFineLocation  != PackageManager.PERMISSION_GRANTED &&
            permissionReadPhoneState  != PackageManager.PERMISSION_GRANTED &&
            permissionChangeWifiAccess  != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                ),
                REQUEST_INTERNET_STATE
            )
        } else {
            Timber.d("已授予互联网权限")
        }
    }

    //权限请求和授予
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode ==  REQUEST_INTERNET_STATE ){
            for (i in permissions){
                Timber.d("权限"+i)
            }
        }else{
            Timber.w("授予权限的访问对象出错")
        }
    }

}