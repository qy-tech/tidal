package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MyRightSidePanel(
    showPanel: Boolean = false,
    startPositionPx: Float = 0f,
    endPositionPx: Float = 0f,
    onClickScrim: () -> Unit = {},
    mainContent: @Composable () -> Unit,
    slideBarContent: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        mainContent.invoke()

        AnimatedVisibility(
            visible = showPanel,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    // 这里处理点击空白处关闭，indication = null 去掉点击波纹
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClickScrim
                    )
            )
        }

        SlideInBox(
            visible = showPanel,
            modifier = Modifier.align(Alignment.CenterEnd),
            startPositionPx = startPositionPx,
            endPositionPx = endPositionPx,
            content = slideBarContent
        )

    }
}