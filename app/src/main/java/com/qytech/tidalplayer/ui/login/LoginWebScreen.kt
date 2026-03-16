package com.qytech.tidalplayer.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.qytech.tidal.login.LoginHandleState
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.TidalRoute
import com.qytech.tidalplayer.vm.TidalViewModel
import com.qytech.tidalplayer.ui.login.webview.ComposeWebView

@Composable
fun LoginWebScreen(navController: NavController) {
    val viewModel: TidalViewModel = hiltViewModel()
    val loginUiState by viewModel.getTidalLogin().loginUiState.collectAsState()

    var isLoading by remember { mutableStateOf(true) }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val (quit, content, loading) = createRefs()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(content) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            ComposeWebView(
                viewModel = viewModel,
                onPageFinish = {
                    isLoading = false
                }
            )
        }

        if (isLoading || loginUiState.handleState == LoginHandleState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.constrainAs(loading) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )
        }

        IconButton(
            onClick = {
                navController.navigateUp()
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.icon_quit),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(35.dp)
                    .constrainAs(quit) {
                        top.linkTo(parent.top, margin = 10.dp)
                        start.linkTo(parent.start, margin = 10.dp)
                    }
            )
        }

        if (loginUiState.handleState is LoginHandleState.Success) {
            navController.navigate(TidalRoute.SONG_LIST_START)
        }
    }
}