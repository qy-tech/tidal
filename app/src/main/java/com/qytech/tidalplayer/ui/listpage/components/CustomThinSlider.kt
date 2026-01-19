package com.qytech.tidalplayer.ui.listpage.components

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThinSlider(
    modifier: Modifier = Modifier,
    enabled: Boolean = false,
    currentValue: Float = 0f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit = {},
    onValueChangeFinished: () -> Unit = {}
) {

    val myActiveColor = Color(0xFF00E5FF)   // 激活部分的颜色（比如亮青色）
    val myInactiveColor = Color(0xFF333333) // 未激活部分的颜色（深灰色）
    val myThumbColor = Color.White          // 圆点的颜色

    val trackHeight = 2.dp  // 线条的高度（细线）
    val thumbSize = 6.dp   // 圆点的大小

    Slider(
        enabled = enabled,
        value = currentValue,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        thumb = {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center // 2. 强制内部元素居中
            ) {
                Box(
                    modifier = Modifier
                        .size(thumbSize) // 10.dp
                        .shadow(4.dp, CircleShape)
                        .background(myThumbColor, CircleShape)
                )
            }
        },
        track = { sliderState ->
            SliderDefaults.Track(
                colors = SliderDefaults.colors(
                    activeTrackColor = myActiveColor,
                    inactiveTrackColor = myInactiveColor,
                ),
                sliderState = sliderState,
                modifier = Modifier.height(trackHeight),
                thumbTrackGapSize = 0.dp
            )
        },
        modifier = modifier
    )
}