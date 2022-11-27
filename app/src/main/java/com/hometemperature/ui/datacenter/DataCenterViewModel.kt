package com.hometemperature.ui.datacenter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DataCenterViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dataCenter Fragment"
    }
    val text: LiveData<String> = _text
}