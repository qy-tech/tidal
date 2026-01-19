package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun <T> TabRowPill(
    tabs: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val centerOffset = remember(configuration, density) {
        with(density) {
            val screenCenter = (configuration.screenWidthDp.dp / 2).toPx()
            val halfTabWidth = (configuration.screenWidthDp.dp / 10).toPx()

            -(screenCenter - halfTabWidth).toInt()
        }
    }
    LaunchedEffect(selected, tabs) {
        val index = tabs.indexOf(selected)
        if (index >= 0) {
            listState.scrollToItem(index, scrollOffset = centerOffset)
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.CenterEnd) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .background(Color(0xff1a1a1a), CircleShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(tabs) { tab ->
                val isActive = tab == selected
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onSelect(tab) }
                        .background(if (isActive) Color(0xff333333) else Color(0xff1a1a1a))
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = labelProvider(tab),
                        color = if (isActive) Color.White else Color(0xffA0A0A0),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}