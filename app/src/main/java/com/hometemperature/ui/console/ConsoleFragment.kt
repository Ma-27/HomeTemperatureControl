package com.hometemperature.ui.console

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hometemperature.R
import com.hometemperature.databinding.FragmentConsoleBinding
import com.hometemperature.ui.datacenter.DataCenterViewModel
import com.hometemperature.ui.datacenter.DataCenterViewModelFactory

class ConsoleFragment : Fragment() {

    private var _binding: FragmentConsoleBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //本fragment的view model
    private lateinit var consoleViewModel: ConsoleViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsoleBinding.inflate(inflater, container, false)

        consoleViewModel =
            ViewModelProvider(this, ConsoleViewModelFactory(requireActivity().application))
                .get(ConsoleViewModel::class.java)

        binding.viewmodel = consoleViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setListener()

        return binding.root
    }

    //创建各个控件的点击响应监听和变量状态改变监听
    private fun setListener() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}