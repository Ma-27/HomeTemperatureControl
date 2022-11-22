package com.hometemperature.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hometemperature.bean.ApplicationViewModel

class HomeViewModel : ApplicationViewModel() {

    //菜单中刷新按钮是否按下
    private val _refreshIsChecked = MutableLiveData<Boolean>().apply {
        value = false
    }
    var refreshIsChecked: LiveData<Boolean> = _refreshIsChecked
        get() = _refreshIsChecked

    //按下refresh按钮之后
    fun setRefreshChecked() {
        _refreshIsChecked.value = true
    }

    //停止refresh按钮之后
    fun clearRefreshChecked() {
        _refreshIsChecked.value = false
    }

    init {

    }
}