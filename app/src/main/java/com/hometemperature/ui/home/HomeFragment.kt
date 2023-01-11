package com.hometemperature.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hometemperature.R
import com.hometemperature.bean.flag.NetWorkDefaultConfiguration
import com.hometemperature.bean.item.WifiItem
import com.hometemperature.databinding.DialogWifiDetailBinding
import com.hometemperature.databinding.FragmentHomeBinding
import timber.log.Timber


@Suppress("NAME_SHADOWING")
class HomeFragment : Fragment() {

    //该fragment的视图绑定
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //显示wifi信息并连接的dialog的的视图绑定
    private lateinit var wifiDetailBinding: DialogWifiDetailBinding

    private lateinit var listAdapter: WifiListAdapter

    //本fragment的view model
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        homeViewModel = ViewModelProvider(this, HomeViewModelFactory(requireActivity().application))
            .get(HomeViewModel::class.java)
        binding.viewmodel = homeViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupAdapter()
        setListener(container)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        //断开连接
        homeViewModel.disConnectWifi(homeViewModel.repository)
    }

    //创建各个控件的点击响应监听和变量状态改变监听
    private fun setListener(container: ViewGroup?) {
        //上拉刷新控件的响应
        binding.swContainer.setOnRefreshListener {
            checkWifiState()
            //重置刷新开关状态
            binding.viewmodel!!.clearRefreshChecked()
        }

        //状态栏菜单中刷新
        binding.viewmodel!!.refreshIsChecked.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                checkWifiState()
                //重置刷新开关状态
                binding.viewmodel!!.clearRefreshChecked()
            }
        })


        //socket发生改变时通知repository刷新socket
        //XXX 粗浅的修补了bug：切换时防止为空集合
        homeViewModel.socket.observe(viewLifecycleOwner, Observer {
            if (homeViewModel.usageCount != 0 && homeViewModel.repository.wifiItem.value!!.isConnected == "已连接") {
                homeViewModel.notifySocketChanged(homeViewModel.repository)
            }
            homeViewModel.usageCount++
            if (homeViewModel.usageCount > 9) {
                homeViewModel.usageCount = 1
            }
        })
    }

    //创建recycler view的适配器和recycler view的点击响应
    private fun setupAdapter() {
        val viewModel = binding.viewmodel
        if (viewModel != null) {
            //XXX 初始化recycler view adapter也在这里设置点击监听，点击即弹出对话框配置网络参数
            listAdapter = WifiListAdapter(WifiListClickListener { item ->
                showWifiItemDetail(item)
                Timber.d("成功点击$item")
            })

            binding.rvWifiList.adapter = listAdapter
        } else {
            Timber.e("试图设置adapter时ViewModel尚未初始化。")
        }
    }

    //弹出询问wifi连接方式的dialog
    private fun showWifiItemDetail(item: WifiItem) {
        Timber.d("接入点名称：" + item.wifiName)
        Timber.d("接入点主机地址：" + item.apMacAddress)

        context?.let {
            //对话框视图绑定
            wifiDetailBinding = DialogWifiDetailBinding.inflate(layoutInflater)
            wifiDetailBinding.tvWifiDescription.text =
                "接入点Mac地址：${item.apMacAddress}\n" +
                        "设备mac地址:${item.deviceMacAddress}\n" +
                        "设备的IP地址:${item.deviceIpAddress}\n" +
                        "请输入大于等于8位的wifi密码："
            wifiDetailBinding.etWifiCode.hint = "无密码请忽略此项"
            wifiDetailBinding.cbIsDefault.text = "是否自动获取端口号?"
            wifiDetailBinding.cbIsDefault.isChecked = true
            wifiDetailBinding.etWifiPort.hint = "如果选择自动生成则忽略此项"

            //构建对话框
            MaterialAlertDialogBuilder(it)
                .setTitle(item.wifiName)
                //取消连接wifi的意图
                .setNegativeButton("取消") { dialog, which -> }
                //断开与当前wifi的连接
                .setNeutralButton("断开连接") { dialog, which ->
                    homeViewModel.disConnectWifi(homeViewModel.repository)
                }
                //连接到这个wifi
                .setPositiveButton("连接") { dialog, which ->
                    //填充在dialog中选择的参数
                    item.password = wifiDetailBinding.etWifiCode.text.toString()
                    //端口号自动填充为默认值8080
                    item.portNumber =
                        if (wifiDetailBinding.etWifiPort.text.toString() != "") Integer.valueOf(
                            wifiDetailBinding.etWifiPort.text.toString()
                        ) else NetWorkDefaultConfiguration.DEFAULT_PORT_NUMBER
                    //ip地址自动填充为默认值192.168.4.1
                    item.deviceIpAddress = if (wifiDetailBinding.etWifiIp.text.toString() != "")
                        wifiDetailBinding.etWifiIp.text.toString()
                    else NetWorkDefaultConfiguration.DEFAULT_IP_ADDRESS

                    homeViewModel.setWifiItem(item)
                    //连接到特定的wifi
                    homeViewModel.connectToSpecifiedWifi(item, homeViewModel.repository)
                }
                .setView(wifiDetailBinding.root)
                .show()
        }
    }

    //响应刷新网络列表命令
    private fun checkWifiState() {
        //调用网络模块进行刷新
        homeViewModel.checkNetworkState(homeViewModel.repository)

        //停止swipe refresh layout刷新
        if (binding.swContainer.isRefreshing) {
            binding.swContainer.isRefreshing = false
        }
    }
}