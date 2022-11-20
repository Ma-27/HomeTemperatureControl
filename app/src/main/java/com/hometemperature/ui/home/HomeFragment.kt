package com.hometemperature.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.hometemperature.R
import com.hometemperature.bean.DataTransferObject
import com.hometemperature.databinding.FragmentHomeBinding
import timber.log.Timber

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var listAdapter: WifiListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.viewmodel = activity?.let { ViewModelProvider(it).get(HomeViewModel::class.java) }
        binding.lifecycleOwner = viewLifecycleOwner

        setupAdapter()
        setListener(container)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListener(container: ViewGroup?) {
        //上拉刷新控件的响应
        binding.swContainer.setOnRefreshListener {
            checkWifiState(container)
        }
        //状态栏菜单中刷新
        binding.viewmodel!!.refreshIsChecked.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                checkWifiState(container)
                //重置live data状态
                binding.viewmodel!!.clearRefreshChecked()
            }
        })
    }

    private fun setupAdapter() {
        val viewModel = binding.viewmodel
        if (viewModel != null) {
            //TODO 初始化recycler view adapter并且在这里添加点击响应回调
            listAdapter = WifiListAdapter()
            binding.rvWifiList.adapter = listAdapter
        } else {
            Timber.e("试图设置adapter时ViewModel尚未初始化。")
        }
    }

    //响应刷新命令
    private fun checkWifiState(container: ViewGroup?) {
        //初始化读取网络状态
        val wifiManager = context!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
        //注册broadcast receiver并接受返回的wifi列表
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    //提示刷新成功
                    if (container != null) {
                        Snackbar.make(container, "刷新wifi列表成功", Snackbar.LENGTH_SHORT).show()
                    }
                    //解析刷新结果
                    val resultsList = wifiManager.scanResults
                    //在view model中存入数据
                    binding.viewmodel!!.setWifiList(DataTransferObject.asWifiItem(resultsList))
                }
            }
        }

        //添加broadcast receiver响应刷新命令
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context!!.registerReceiver(wifiScanReceiver, intentFilter)

        //开始扫描网络
        val success = wifiManager.startScan()

        // 扫描失败后报错
        if (!success) {
            //提示刷新成功
            if (container != null) {
                Snackbar.make(container, "刷新wifi列表失败，请检查网络状态", Snackbar.LENGTH_SHORT).show()
            }
        }

        //log记录按钮按下，已经刷新
        Timber.d("刷新wifi按钮已按下")
        //停止swipe refresh刷新
        if (binding.swContainer.isRefreshing) {
            binding.swContainer.isRefreshing = false
        }
    }
}