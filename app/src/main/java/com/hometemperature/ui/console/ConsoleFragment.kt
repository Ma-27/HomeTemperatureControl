package com.hometemperature.ui.console

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hometemperature.databinding.FragmentConsoleBinding


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
        //温度控制系统打开按钮监听
        binding.switchTemperature.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                //打开后从这里处理
                consoleViewModel.setNetWorkStatus("温度控制系统已开启")
            } else {
                consoleViewModel.setNetWorkStatus("温度控制系统已关闭")
            }
        }

        //
        binding.sbTemperature.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            var temperature: Float = 15.0f
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Called when the progress value is changed
                temperature = 15.0f + (progress / 100.0f) * 15.0f

                consoleViewModel.setTargetTemperature(temperature)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Called when the user starts touching the seekbar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Called when the user stops touching the seekbar
                //向主机发送温度数据


            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}