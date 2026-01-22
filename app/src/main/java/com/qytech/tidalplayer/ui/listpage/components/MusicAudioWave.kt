package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MusicAudioWave(
    color: Color = Color(0xFF00E5FF), // 对应你 CSS 中的 accent-color
    barWidth: Dp = 3.dp,
    maxHeight: Dp = 14.dp, // 对应 CSS 中最大的高度
    minHeight: Dp = 4.dp   // 对应 CSS 中最小的高度
) {
    // 1. 创建无限循环的 Transition
    val infiniteTransition = rememberInfiniteTransition(label = "MusicWave")

    // 2. 定义动画的一帧时长
    val durationMillis = 500 // CSS 是 1s 完成一次完整波动(0->50%->100%)，这里用 reverse 模式，单程 500ms

    // 3. 创建三个独立的高比例动画 (0f ~ 1f)，设置不同的起始偏移量 (StartOffset) 来模拟错落感
    
    // 第一根柱子 (延迟 0ms)
    val anim1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(0) 
        ),
        label = "Bar1"
    )

    // 第二根柱子 (延迟 200ms，模拟 CSS 的 0.2s)
    val anim2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(200)
        ),
        label = "Bar2"
    )

    // 第三根柱子 (延迟 400ms，模拟 CSS 的 0.4s - 为了更明显的错落感可以稍微调大)
    val anim3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(100) // 这里故意设个不同的步调，让它看起来不像单纯的流水
        ),
        label = "Bar3"
    )

    // 4. 布局：横向排列，底部对齐
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp), // 柱子之间的间距
        modifier = Modifier.height(maxHeight) // 容器高度固定为最大高度
    ) {
        // 渲染三个柱子
        AudioBar(anim1, color, barWidth, minHeight, maxHeight)
        AudioBar(anim2, color, barWidth, minHeight, maxHeight)
        AudioBar(anim3, color, barWidth, minHeight, maxHeight)
    }
}

@Composable
private fun AudioBar(
    fraction: Float,
    color: Color,
    width: Dp,
    minHeight: Dp,
    maxHeight: Dp
) {
    // 根据动画比例计算当前高度: min + (max - min) * fraction
    val currentHeight = minHeight + (maxHeight - minHeight) * fraction
    
    Box(
        modifier = Modifier
            .width(width)
            .height(currentHeight)
            .clip(RoundedCornerShape(50)) // 圆角
            .background(color)
    )
}