package com.hometemperature.ui.datacenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hometemperature.R
import com.hometemperature.bean.flag.MessageType
import com.hometemperature.bean.flag.TestFlag
import com.hometemperature.bean.flag.TransmissionStatus
import com.hometemperature.databinding.FragmentDatacenterBinding
import com.hometemperature.util.itembuild.DataItemBuilder
import timber.log.Timber

class DataCenterFragment : Fragment() {

    private var _binding: FragmentDatacenterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //本fragment的view model
    private lateinit var dataCenterViewModel: DataCenterViewModel

    private lateinit var listAdapter: DataCenterListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_datacenter, container, false)
        dataCenterViewModel =
            ViewModelProvider(this, DataCenterViewModelFactory(requireActivity().application))
                .get(DataCenterViewModel::class.java)
        binding.viewmodel = dataCenterViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupAdapter()
        setListener(container)

        return binding.root
    }

    //创建各个控件的点击响应监听和变量状态改变监听
    private fun setListener(container: ViewGroup?) {
        //监视发送缓存，发送数据就把数据存入发送缓存中，一旦缓存变更就发送数据
        binding.viewmodel!!.dataSendCache.observe(viewLifecycleOwner, Observer {
            //如果是创建发送缓存之初变化，则不发送数据，否则发送数据。
            if (it != TestFlag.SEND) {
                dataCenterViewModel.sendDataToHost(dataCenterViewModel.repository)
            }
        })

        //监视接收缓存，一旦收到数据就将数据存入数据列表中
        binding.viewmodel!!.dataReceiveCache.observe(viewLifecycleOwner, Observer {
            if (it != TestFlag.RECEIVE) {
                //新建一个data item
                val item = DataItemBuilder.buildDataItem(it)
                item.messageType = MessageType.MESSAGE_RECEIVE
                item.messageStatus = TransmissionStatus.SUCCESS
                dataCenterViewModel.addDataToDataList(dataCenterViewModel.repository, item)
            }
        })


    }

    private fun setupAdapter() {
        val viewModel = binding.viewmodel
        if (viewModel != null) {
            //TODO 初始化recycler view adapter也在这里设置点击监听
            listAdapter = DataCenterListAdapter(DataCenterClickListener { it ->

            })
            binding.rvDataList.adapter = listAdapter
        } else {
            Timber.e("试图设置adapter时ViewModel尚未初始化。")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}