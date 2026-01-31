package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun SlideInBox(
    visible: Boolean,
    modifier: Modifier = Modifier,
    startPositionPx: Float = 0f,
    endPositionPx: Float = 0f,
    content: @Composable BoxScope.() -> Unit
) {
    val offsetX = remember { Animatable(startPositionPx) }

    LaunchedEffect(visible) {
        if (visible) {
            offsetX.animateTo(
                targetValue = endPositionPx,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )
        } else {
            offsetX.animateTo(
                targetValue = startPositionPx,
                animationSpec = tween(300)
            )
        }
    }

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = offsetX.value.toInt(),
                    y = 0
                )
            },
    ) {
        content()
    }
}