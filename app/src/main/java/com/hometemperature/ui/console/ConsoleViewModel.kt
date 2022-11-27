package com.hometemperature.ui.console

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConsoleViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is console Fragment"
    }
    val text: LiveData<String> = _text
}