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
import com.hometemperature.databinding.FragmentDatacenterBinding
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
        //监视发送缓存，在fragment中监视只需要通知数据发送即可
        dataCenterViewModel.dataSendCache.observe(viewLifecycleOwner, Observer {
            //通知列表刷新数据
            listAdapter.notifyDataSetChanged()
        })

        //监视接收缓存，一旦收到数据就将数据存入数据列表中
        dataCenterViewModel.dataReceiveCache.observe(viewLifecycleOwner, Observer {
            //通知列表刷新数据
            listAdapter.notifyDataSetChanged()
        })

        //监视列表，如果发现列表改变，查看是不是要发送数据（变更有两种可能，发送数据和新数据收到）,如果是发送数据，则存入数据到发送缓存中。
        dataCenterViewModel.dataList.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
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
            //XXX 初始化recycler view adapter也在这里设置点击监听
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