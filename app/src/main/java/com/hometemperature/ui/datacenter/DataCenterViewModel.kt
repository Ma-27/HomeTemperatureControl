package com.hometemperature.ui.datacenter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hometemperature.bean.ApplicationViewModel

class DataCenterViewModel : ApplicationViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dataCenter Fragment"
    }
    val text: LiveData<String> = _text
}