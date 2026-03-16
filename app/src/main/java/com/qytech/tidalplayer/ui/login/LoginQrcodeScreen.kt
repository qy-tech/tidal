package com.qytech.tidalplayer.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.qytech.tidalplayer.vm.TidalViewModel

private sealed class State {
    data object Init : State()

    data object Link : State()

    data object Done : State()
}

@Composable
fun LoginQrcodeScreen() {
    val viewModel: TidalViewModel = hiltViewModel()
    val state = remember { mutableStateOf<State>(State.Init) }
    val loginUiState by viewModel.getTidalLogin().loginUiState.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        when (state.value) {
            is State.Init -> {
                InitUI {
                    viewModel.getTidalLogin().useQrCodeLogin()
                }
            }

            is State.Link -> {
                LinkUI(loginUiState.qrCode ?: "", loginUiState.userCode ?: "")
            }

            is State.Done -> {}
        }
    }
}

@Composable
fun InitUI(onClick: () -> Unit) {
    Column { Button(onClick = onClick) { Text(text = "Start Device Login") } }
}

@Composable
fun LinkUI(uri: String, code: String) {
    Column {
        Text(text = "$code", color = Color.White)
        Text(text = "$uri", color = Color.White)
    }
}