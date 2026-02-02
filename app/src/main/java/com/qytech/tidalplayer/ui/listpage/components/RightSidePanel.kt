package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.TidalRoute
import com.qytech.tidalplayer.ui.listpage.ListPageViewModel
import com.qytech.tidalplayer.ui.listpage.model.ItemInfo
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.ui.listpage.model.SongList

@Composable
fun RightSidePanel(
    showPanel: Boolean = false,
    onClickScrim: () -> Unit = {},
    mainContent: @Composable () -> Unit,
    slideBarContent: @Composable () -> Unit
) {
//    var showPanel by remember { mutableStateOf(false) }
//
//    BackHandler(enabled = showPanel) {
//        showPanel = false
//    }

    Box(modifier = Modifier.fillMaxSize()) {

//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text("主界面内容")
//            Spacer(modifier = Modifier.height(20.dp))
//            Button(onClick = { showPanel = true }) {
//                Text("从右侧打开侧边栏")
//            }
//        }
        // 主内容
        mainContent.invoke()


        // --- 3. 遮罩层 (Scrim) ---
        // 当面板显示时，显示半透明背景，点击背景关闭面板
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

        // --- 4. 滑动面板 (核心逻辑) ---
        AnimatedVisibility(
            visible = showPanel,
            modifier = Modifier.align(Alignment.CenterEnd), // 关键：让动画组件靠右对齐
            // 入场动画：水平滑入，初始位置偏移量为自身的宽度（即完全在屏幕右侧外）
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth }, // fullWidth 是组件自身宽度
                animationSpec = tween(durationMillis = 300)
            ),
            // 出场动画：水平滑出，目标位置偏移量为自身的宽度
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            ),

            ) {
            // --- 侧边栏实际内容 ---
//            Column(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .fillMaxWidth(0.5f) // 占据屏幕宽度的 50%
//                    .background(MaterialTheme.colorScheme.surface) // 背景色
//                    .clickable(enabled = false) {} // 拦截点击事件，防止穿透到遮罩层
//                    .padding(16.dp)
//            ) {
//                Text(
//                    text = "侧边设置",
//                    style = MaterialTheme.typography.headlineSmall
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Text("这里是侧边栏的内容区域...")
//                Spacer(modifier = Modifier.weight(1f))
//                Button(onClick = { showPanel = false }) {
//                    Text("关闭")
//                }
//            }

            // 侧边栏内容
            slideBarContent.invoke()
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(200.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 200.dp)
                .background(
                    color = Color.White
                )
        ) {}
    }
}

@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 640
)
@Composable
fun MyPlaylistSlideBarContent(
    data: ItemInfo? = null,
    createPlaylist: List<SongList> = emptyList(),
    onCreateNew: () -> Unit = {},
    onItemClick: (ItemInfo?, SongList) -> Unit = { _, _ -> }
) {

    val viewModel: ListPageViewModel = hiltViewModel()
    val myPlaylistPagingData = viewModel.myPlaylistPagingData.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.4f)
            .background(
                color = Color(0xff121212)
            ) // 背景色
            .drawBehind {
                drawLine(
                    color = Color.White.copy(0.2f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .clickable(enabled = false) {} // 拦截点击事件，防止穿透到遮罩层
    ) {

        // 头部
        Column(
            modifier = Modifier.drawBehind {
                drawLine(
                    color = Color.White.copy(0.2f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        ) {
            Spacer(modifier = Modifier.size(15.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 15.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add To My Playlist",
                    color = Color.White,
                    fontWeight = FontWeight(600),
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(35.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable(
                            onClick = onCreateNew
                        )
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.icon_plus_solid_full),
                        contentDescription = null,
                        tint = Color(0xff00e5ff),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Text(
                        text = "New Playlist",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight(500)
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                }
            }

            Spacer(modifier = Modifier.size(15.dp))
        }

        // 歌单
        LazyColumn(
            modifier = Modifier.padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            item {}

            // 主要内容
            items(
                count = createPlaylist.size,
                key = { index -> createPlaylist[index].id }
            ) { index ->
                val item = createPlaylist[index]
                item.apply {
                    val item = createPlaylist[index]
                    item.apply {
                        PlaylistItem(
                            list = item,
                            onItemClick = { list ->
                                onItemClick.invoke(data, list)
                            }
                        )
                    }
                }

            }

            items(
                count = myPlaylistPagingData.itemCount,
                key = { index -> myPlaylistPagingData[index]?.id ?: "_${index}_" }
            ) { index ->
                val item = myPlaylistPagingData[index]
                item?.apply {
                    PlaylistItem(
                        list = item,
                        onItemClick = { list ->
                            onItemClick.invoke(data, list)
                        }
                    )
                }
            }

            item {}
        }

    }
}

@Composable
private fun PlaylistItem(
    list: SongList,
    onItemClick: (SongList) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(
                onClick = { onItemClick.invoke(list) }
            ),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (list.coverUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.default_playlist_cover_160x160)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentScale = ContentScale.Crop
            )
        } else {
            AsyncImage(
                model = list.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(shape = RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = list.title,
                fontWeight = FontWeight(500),
                color = Color.White,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.size(5.dp))
            Text(
                text = list.description ?: "",
                fontWeight = FontWeight(500),
                color = Color(0xffa0a0a0),
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun OptionSlideBarContent(
    isTrack: Boolean = true,
    data: ItemInfo? = null,
    isFavourite: (String) -> Boolean = { false },
    onPlayNow: (ItemInfo?) -> Unit = {},
    onPlayNext: (ItemInfo?) -> Unit = {},
    onAddToQueue: (ItemInfo?) -> Unit = {},
    onAddOrRemoveToMyCollection: (ItemInfo?, Boolean) -> Unit = { _, _ -> },
    onAddToMyPlaylist: (ItemInfo?) -> Unit = {},
    onViewArtist: (ItemInfo?) -> Unit = {},
    onViewAlbum: (ItemInfo?) -> Unit = {}
) {

    val singleSong = if (data != null && isTrack) data as SingleSong else null
    val songList = if (data != null && !isTrack) data as SongList else null

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.4f)
            .background(
                color = Color(0xff121212)
            ) // 背景色
            .drawBehind {
                drawLine(
                    color = Color.White.copy(0.2f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .clickable(enabled = false) {} // 拦截点击事件，防止穿透到遮罩层

    ) {

        SlideBarHeader(
            coverUrl = if (isTrack) singleSong?.coverUrl else songList?.coverUrl,
            title = if (isTrack) singleSong?.title ?: "" else songList?.title ?: "",
            description = if (isTrack) singleSong?.description ?: "" else songList?.title ?: ""
        )

        Spacer(modifier = Modifier.size(10.dp))

        val itemInfo = if (isTrack) singleSong else songList

        Column() {
            SlideBarOption(
                iconId = R.drawable.icon_circle_play_solid_full,
                optionName = if (isTrack) "Play Now" else "Play All Now",
                onClick = { onPlayNow.invoke(itemInfo) }
            )
            SlideBarOption(
                iconId = R.drawable.icon_indent_solid_full,
                optionName = "Add To Next",
                onClick = { onPlayNext.invoke(itemInfo) }
            )
            SlideBarOption(
                iconId = R.drawable.icon_list_solid_full,
                optionName = "Add to Queue",
                onClick = { onAddToQueue.invoke(itemInfo) }
            )
            SlideBarOption(
                iconId = if (isFavourite.invoke(
                        if (isTrack) singleSong?.id ?: "" else songList?.id ?: ""
                    )
                ) R.drawable.icon_heart_solid_full else R.drawable.icon_heart_regular_full,
                optionName = if (isFavourite.invoke(
                        if (isTrack) singleSong?.id ?: "" else songList?.id ?: ""
                    )
                ) "Remove from Collection" else "Add to My Collection",
                onClick = {
                    onAddOrRemoveToMyCollection.invoke(
                        itemInfo,
                        isFavourite.invoke(
                            if (isTrack) singleSong?.id ?: "" else songList?.id ?: ""
                        )
                    )
                }
            )
            if (isTrack) {
                SlideBarOption(
                    iconId = R.drawable.icon_plus_solid_full,
                    optionName = "Add To My Playlist",
                    onClick = { onAddToMyPlaylist.invoke(itemInfo) }
                )
                SlideBarOption(
                    iconId = R.drawable.icon_circle_user_solid_full,
                    optionName = "View Artist",
                    onClick = { onViewArtist.invoke(itemInfo) }
                )
                SlideBarOption(
                    iconId = R.drawable.icon_compact_disc_solid_full,
                    optionName = "View Album",
                    onClick = { onViewAlbum.invoke(itemInfo) }
                )
            }
        }
    }
}

@Composable
fun SlideBarOption(
    iconId: Int = R.drawable.icon_circle_play_solid_full,
    optionName: String = "Play All Now",
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(start = 25.dp, end = 15.dp)
            .clickable(
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconId),
            contentDescription = null,
            tint = Color(0xff00e5ff),
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.size(20.dp))
        Text(
            text = optionName,
            color = Color(0xffa0a0a0),
            fontSize = 22.sp,
            fontWeight = FontWeight(600)
        )
    }
}

@Composable
fun SlideBarHeader(
    coverUrl: String? = null,
    title: String = "Acoustic Favorites",
    description: String = "Playlist"
) {
    Column(
        modifier = Modifier
            .drawBehind {
                drawLine(
                    color = Color.White.copy(0.2f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .padding(start = 25.dp, end = 15.dp)
    ) {
        Spacer(modifier = Modifier.size(30.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
        ) {
            if (coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.default_playlist_cover_160x160)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(65.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (coverUrl == TidalRoute.TRACK_LIST_ID) {
                TidalTracksCover(
                    size = 65.dp
                )
            } else {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(65.dp)
                        .clip(shape = RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.size(20.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight(800)
                )
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    text = description,
                    color = Color(0xffa0a0a0),
                    fontSize = 20.sp,
                    fontWeight = FontWeight(500)
                )
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        )
    }
}