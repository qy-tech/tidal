package com.qytech.tidalplayer.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qytech.tidalplayer.utils.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    private val _showLoading = MutableStateFlow(false)
    val showLoading = _showLoading.asStateFlow()

    protected fun showToast(msg: String) {
        viewModelScope.launch {
            ToastUtils.showForFlow(msg)
        }
    }

    protected fun launchWithLoading(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _showLoading.value = true
                block()
            } finally {
                _showLoading.value = false
            }
        }
    }
}