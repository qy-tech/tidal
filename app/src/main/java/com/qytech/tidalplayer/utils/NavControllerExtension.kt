package com.qytech.tidalplayer.utils

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController

fun NavController.popBackSafely() {
    if (previousBackStackEntry != null && currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    }
}