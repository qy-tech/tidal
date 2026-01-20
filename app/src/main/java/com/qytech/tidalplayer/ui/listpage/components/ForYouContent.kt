package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.TidalRoute
import com.qytech.tidalplayer.ui.listpage.ListPageViewModel
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.ItemInfo
import com.qytech.tidalplayer.ui.listpage.model.ItemType
import com.qytech.tidalplayer.ui.listpage.model.PageItem
import com.qytech.tidalplayer.ui.listpage.model.SongList
import com.tidal.sdk.player.playbackengine.model.PlaybackState
import kotlinx.coroutines.flow.emptyFlow
import timber.log.Timber

@Composable
fun ForYouContent(
    item: PageItem,
    navController: NavController
) {
    val viewModel: ListPageViewModel = hiltViewModel()
    val controllerUiState by viewModel.controllerUiState.collectAsState()
    val playingListId by viewModel.currentListId.collectAsState()
    val isPlaying by remember {
        derivedStateOf {
            controllerUiState.playbackState == PlaybackState.PLAYING
        }
    }


    val playlistData = viewModel.playlistPagingData.collectAsLazyPagingItems()
    val albumData = viewModel.albumPagingData.collectAsLazyPagingItems()
    val emptyData = emptyFlow<PagingData<SongList>>().collectAsLazyPagingItems()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(item.subItems) { subItem ->
                when (subItem.itemType) {
                    ItemType.SONG_LIST -> {
                        ForYouSongListItem(
                            title = subItem.title,
                            playingListId = playingListId,
                            isPlaying = isPlaying,
                            dataList = when(subItem.dataType) {
                                DataType.PLAY_LIST -> playlistData
                                DataType.ALBUM -> albumData
                                else -> emptyData
                            },
                            onClick = { item ->
                                // 跳转到单曲界面
                                val route = TidalRoute.getItemTrackListRoute(item.id, item.dataType)
                                navController.navigate(route)
                            }
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun <T : ItemInfo> ForYouSongListItem(
    title: String = "Mixes for you",
    playingListId: String = "",
    isPlaying: Boolean = false,
    dataList: LazyPagingItems<T>,
    onClick: (ItemInfo) -> Unit = {}
) {
    Column(
        modifier = Modifier.background(
            color = Color.Black
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                fontWeight = FontWeight(700),
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = "See all",
                color = Color(0xff00E5FF),
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                fontSize = 14.sp
            )
        }
        // 列表
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                count = dataList.itemCount,
                key = dataList.itemKey { it.id }
            ) { index ->
                val item = dataList[index]
                if (item != null) {
                    SongListItem(
                        item = item,
                        isCurrentList = playingListId == item.id,
                        isPlaying = isPlaying,
                        onClick = onClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SongListItem(
    item: ItemInfo,
    isCurrentList: Boolean = true,
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (ItemInfo) -> Unit = {},
) {
    Column(
        modifier = modifier
            .clickable(
                onClick = { onClick.invoke(item) }
            )
    ) {
        Spacer(modifier = Modifier.size(5.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = item.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .blur(if (isCurrentList) 2.dp else 0.dp)
            )

            if (isCurrentList) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0xffffffff),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(if (isPlaying) R.drawable.icon_pause_solid_full else R.drawable.icon_play_solid_full),
                        contentDescription = null,
                        tint = Color(0xff080808),
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
//        Image(
//            painter = painterResource(R.drawable.img),
//            contentDescription = null,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier.size(120.dp)
//                .clip(RoundedCornerShape(8.dp))
//        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = item.title,
            color = if (isCurrentList) Color(0xff00E5FF) else Color(0xffA0A0A0),
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            fontSize = 16.sp,
            modifier = Modifier.width(120.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

//        description?.apply {
//            Text(
//                text = this,
//                color = Color(0xffA0A0A0),
//                style = TextStyle(
//                    platformStyle = PlatformTextStyle(
//                        includeFontPadding = false
//                    )
//                ),
//                fontSize = 14.sp
//            )
//        }
    }
}