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
        setListener()

        return binding.root
    }

    //创建各个控件的点击响应监听和变量状态改变监听
    private fun setListener() {
        //监视发送缓存，发送数据就是把待发数据存入发送缓存中，一旦缓存变更就发送
        dataCenterViewModel.dataSendCache.observe(viewLifecycleOwner, Observer {
            //防止空列表
            val dataItem =
                if (dataCenterViewModel.dataList.value?.size != 0) dataCenterViewModel.dataList.value?.get(
                    0
                ) else null

            //如果是创建发送缓存之初变化，则不发送数据，否则发送数据;
            // TODO 为了防止重复发送，如果在on start中live data收到更新，则不应该发送。这里检查时间戳。如果该数据要发送而尚未发送，该数据必然在排在最前面，使用get获取最前面的元素判断是否已发送。
            if (dataItem != null) {
                if (it != TestFlag.SEND &&
                    (dataItem.timestamp != dataCenterViewModel.latestItemTimestamp.value || dataCenterViewModel.latestItemTimestamp.value == 0L)
                ) {
                    //正式调用网络模块发送数据
                    dataCenterViewModel.sendDataToHost(dataCenterViewModel.repository)
                    //重置timestamp标志，用来做前一个的发送标志
                    dataItem.timestamp.let { it1 ->
                        dataCenterViewModel.modifyLatestTimestamp(
                            it1
                        )
                    }
                }
            }
            //通知列表刷新数据
            listAdapter.notifyDataSetChanged()
        })

        //监视接收缓存，一旦收到数据就将数据存入数据列表中
        dataCenterViewModel.dataReceiveCache.observe(viewLifecycleOwner, Observer {
            //初始字符串就是 TestFlag.RECEIVE 如果相同说明未更新数据
            if (it != TestFlag.RECEIVE) {
                //新建一个data item
                val item = DataItemBuilder.buildDataItem(it)
                item.messageType = MessageType.MESSAGE_RECEIVE
                item.messageStatus = TransmissionStatus.SUCCESS
                //将item添加到显示列表中去
                dataCenterViewModel.addDataToDataList(dataCenterViewModel.repository, item)
            }
            //通知列表刷新数据
            listAdapter.notifyDataSetChanged()
        })

        //监视列表，如果发现列表改变，查看是不是要发送数据（变更有两种可能，发送数据和新数据收到）,如果是发送数据，则存入数据到发送缓存中。
        dataCenterViewModel.dataList.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                //按照时间戳降序排列，这样第一个就是最新的一个
                it.sortByDescending { dataItem -> dataItem.timestamp }
                //选取最新更新的一个item，判断是不是更新的发出的数据
                val dataItem = it[0]
                //如果是要发出的数据，修改view model中发送缓存
                if (dataItem.messageType == MessageType.MESSAGE_SEND &&
                    (dataItem.timestamp != dataCenterViewModel.latestItemTimestamp.value || dataCenterViewModel.latestItemTimestamp.value == 0L)
                ) {
                    dataCenterViewModel.modifySendCache(dataItem.data)
                    dataItem.messageStatus = TransmissionStatus.ON_SENDING
                }

                //如果是接收到的新数据，对新数据进行处理，但首先要防止重复添加数据。
                else if (dataItem.messageType == MessageType.MESSAGE_RECEIVE &&
                    (dataItem.timestamp != dataCenterViewModel.latestItemTimestamp.value || dataCenterViewModel.latestItemTimestamp.value == 0L)
                ) {
                    //修改时间为两者一样，防止下次重复添加，重复执行命令
                    dataCenterViewModel.modifyLatestTimestamp(dataItem.timestamp)
                    Timber.d("接收到新数据")
                    //TODO 解码，翻译命令

                }

                //通知列表刷新数据
                listAdapter.notifyDataSetChanged()
            }
        })

        //重置刷新
        binding.swDatacenter.setOnRefreshListener {
            binding.swDatacenter.isRefreshing = false
            //通知列表刷新数据
            listAdapter.notifyDataSetChanged()
        }
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