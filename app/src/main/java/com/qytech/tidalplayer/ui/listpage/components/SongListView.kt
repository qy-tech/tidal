package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.listpage.model.SingleSong

@Composable
fun SongListView(
    coverUrl: String? = null,
    title: String = "",
    description: String? = null,
    daraList: LazyPagingItems<SingleSong>,
    currentSongId: String = "",
    glowColor: Color = Color(0xFF821AAB),
    isFavourite: (String) -> Boolean = { false },
    onItemClick: (Int, SingleSong) -> Unit = {_, _ -> },
    onPlaySequentially: () -> Unit = {},
    onBack: () -> Unit = {},
    onFavourite: (String, Boolean) -> Unit = {_, _ ->},
    onOtherOption: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff080808)) // 黑色背景
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.5f), // 霓虹色 (中心)
                        Color.Transparent // 透明 (边缘)
                    ),
                    center = Offset(x = 400f, y = 100f), // 光晕中心坐标 (左上角)
                    radius = 500f // 光晕半径
                )
            )
    ) {
        Column(
            modifier = Modifier
                .width(340.dp)
                .fillMaxHeight()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(
                        topStart = 15.dp
                    )
                )
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = strokeWidth
                    )
                }
                .padding(horizontal = 30.dp)
        ) {
            Spacer(modifier = Modifier.size(20.dp))
            // 退出按钮
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_chevron_left_solid_full),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = Color.White
                )
            }
            // 图片
            Spacer(modifier = Modifier.size(15.dp))
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                if (coverUrl.isNullOrBlank()) {
                    TidalTracksCover(
                        size = 250.dp
                    )
                } else {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(250.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop

                    )
                }
                Spacer(modifier = Modifier.size(15.dp))
                Text(
                    text = title,
                    color = Color.White,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontSize = 30.sp,
                    fontWeight = FontWeight(700),
                    modifier = Modifier
                        .width(250.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // "No description"
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    text = description ?: "No description.",
                    color = Color(0xffA0A0A0),
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontSize = 20.sp,
                    modifier = Modifier
                        .width(250.dp),
                )
                // 播放全部
                Spacer(modifier = Modifier.size(15.dp))
                Box(
                    modifier = Modifier
                        .width(250.dp)
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .width(120.dp)
                            .height(40.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(40.dp)
                            )
                            .clickable(onClick = onPlaySequentially),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.icon_play_solid_full),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(5.dp))
                        Text(
                            text = "顺序播放",
                            fontSize = 15.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight(800),
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                ),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.size(15.dp))
            }
        }
        // 右边列表
        RightSongList(
            modifier = Modifier.weight(1f),
            dataList = daraList,
            currentSongId = currentSongId,
            isFavourite = isFavourite,
            onItemClick = onItemClick,
            onFavourite = onFavourite,
            onOtherOption = onOtherOption
        )
    }
}

@Composable
fun RightSongList(
    modifier: Modifier = Modifier,
    dataList: LazyPagingItems<SingleSong>,
    currentSongId: String = "",
    isFavourite: (String) -> Boolean,
    onItemClick: (Int, SingleSong) -> Unit,
    onFavourite: (String, Boolean) -> Unit = {_, _ ->},
    onOtherOption: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.03f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 25.dp),
    ) {
        Spacer(modifier = Modifier.size(25.dp))
        Row(
            Modifier
                .height(40.dp)
                .fillMaxWidth()
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = strokeWidth
                    )
                }
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val textColor = Color(0xffA0A0A0)
            val spec = 15.dp
            Text(
                text = "#",
                color = textColor,
                modifier = Modifier
                    .width(30.dp),
                textAlign = TextAlign.Center
            )
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .width(spec)
            )
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .width(40.dp)
            )
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .width(spec)
            )
            Text(
                text = "TITLE",
                color = textColor,
                modifier = Modifier
                    .weight(1.3f)
            )
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .width(spec)
            )
            Text(
                text = "ALBUM",
                color = textColor,
                modifier = Modifier
                    .weight(1f)
            )
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .width(spec)
            )
            Text(
                text = "LIKE",
                color = textColor,
                modifier = Modifier
                    .width(35.dp),
                textAlign = TextAlign.Center
            )
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .width(spec)
            )
            Box(
                modifier = Modifier.width(65.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_clock_regular_full),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .width(spec)
            )
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .width(30.dp)
            )
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (dataList.loadState.refresh is LoadState.NotLoading) {
                items(
                    count = dataList.itemCount,
                    key = dataList.itemKey { it.id }
                ) { index ->
                    val item = dataList[index]
                    item?.apply {
                        SongItem(
                            index = index,
                            item = item,
                            isCurrentItem = currentSongId == item.id,
                            isFavourite = isFavourite(item.id),
                            onClick = onItemClick,
                            onFavourite = onFavourite,
                            onOtherOption = onOtherOption
                        )
                    }
                }
            }
        }

        when (dataList.loadState.refresh) {
            is LoadState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is LoadState.NotLoading -> {
                if (dataList.itemCount == 0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无数据",
                            fontSize = 25.sp,
                            color = Color.White,
                            letterSpacing = 10.sp
                        )
                    }
                }
            }
            is LoadState.Error -> {

            }
        }
    }
}

@Composable
fun SongItem(
    index: Int = 0,
    item: SingleSong = SingleSong(),
    isCurrentItem: Boolean = false,
    isFavourite: Boolean = false,
    onClick: (Int, SingleSong) -> Unit = { _, _ -> },
    onFavourite: (String, Boolean) -> Unit = {_, _ ->},
    onOtherOption: () -> Unit = {}
) {
    val textColor = Color(0xffA0A0A0)
    val spec = 15.dp
    Row(
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .background(
                color = if (isCurrentItem) Color(0x0d00e5ff) else Color.Transparent,
                shape = if (isCurrentItem) RoundedCornerShape(10.dp) else RectangleShape
            )
            .border(
                width = 1.dp,
                color = if (isCurrentItem) Color(0x2600e5ff) else Color.Transparent,
                shape = if (isCurrentItem) RoundedCornerShape(10.dp) else RectangleShape
            )
            .padding(horizontal = 15.dp)
            .clickable(
                onClick = { onClick.invoke(index, item) }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isCurrentItem) {
            // 序号
            Text(
                text = (index + 1).toString(),
                color = textColor,
                modifier = Modifier
                    .width(30.dp),
                textAlign = TextAlign.Center
            )
        } else {
            // 动画
            Box(
                modifier = Modifier.width(30.dp),
                contentAlignment = Alignment.Center
            ) {
                MusicAudioWave()
            }
        }
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .width(spec)
        )
        AsyncImage(
            model = item.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(
                    shape = RoundedCornerShape(5.dp)
                ),
            contentScale = ContentScale.Crop
        )
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .width(spec)
        )
        Column(
            modifier = Modifier
                .weight(1.3f)
        ) {
            Text(
                text = item.getDetailTitle(),
                color = if (isCurrentItem) Color(0xff00e5ff) else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight(1000),
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.size(3.dp))
            Text(
                text = item.description ?: "",
                color = textColor,
                fontSize = 13.sp,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .width(spec)
        )
        Text(
            text = item.album?.title ?: "",
            color = textColor,
            fontSize = 15.sp,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .width(spec)
        )
        Box(
            modifier = Modifier.width(35.dp)
                .clickable(
                    onClick = { onFavourite.invoke(item.id, isFavourite) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(if (isFavourite) R.drawable.icon_heart_solid_full else R.drawable.icon_heart_regular_full),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = if (isFavourite) Color(0xff00e5ff) else textColor
            )
        }
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .width(spec)
        )
        Text(
            text = item.duration,
            color = textColor,
            fontSize = 15.sp,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            modifier = Modifier.width(65.dp),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .width(spec)
        )
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.icon_ellipsis_solid_full),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
                .clickable(
                    onClick = onOtherOption
                ),
            tint = textColor
        )
    }
}