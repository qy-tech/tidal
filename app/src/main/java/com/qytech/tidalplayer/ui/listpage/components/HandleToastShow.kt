package com.qytech.tidalplayer.ui.listpage.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.qytech.tidalplayer.utils.ToastUtils

@Composable
fun HandleToastShow() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        ToastUtils.toastFlow.collect { message ->
//            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            ToastUtils.show(message)
        }
    }
}