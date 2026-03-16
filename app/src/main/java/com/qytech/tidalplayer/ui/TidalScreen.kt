package com.qytech.tidalplayer.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qytech.tidalplayer.ui.listpage.ListStartScreen
import com.qytech.tidalplayer.ui.listpage.components.HandleToastShow
import com.qytech.tidalplayer.ui.login.LoginStartScreen
import com.qytech.tidalplayer.ui.login.LoginWebScreen
import com.qytech.tidalplayer.vm.TidalViewModel

@Composable
fun TidalScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val viewModel: TidalViewModel = hiltViewModel()
    val activity = LocalActivity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xff080808))
    ) {

        NavHost(
            navController = navController,
            startDestination = TidalRoute.LOGIN_START,
            modifier = Modifier
        ) {
            composable(TidalRoute.LOGIN_START) {
                LoginStartScreen(viewModel = viewModel, navController = navController)
            }
            composable(TidalRoute.LOGIN_WEB) {
                LoginWebScreen(navController)
            }
            composable(TidalRoute.SONG_LIST_START) {
                ListStartScreen(navController) {
                    activity?.moveTaskToBack(true)
                }
            }
        }
    }
}