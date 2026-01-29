package com.qytech.tidalplayer.ui.listpage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.TidalRoute
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.SongList
import com.qytech.tidalplayer.utils.popBackSafely
import com.tidal.sdk.player.playbackengine.model.PlaybackState
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun PlaylistAlbumScreen(
    navController: NavController,
    artistId: String,
    title: String,
    dataType: Int
) {
    val viewModel: ListPageViewModel = hiltViewModel()
    val pagingItem = when (dataType) {
        DataType.PLAY_LIST.ordinal -> viewModel.collectionPlaylistPagingData
        DataType.ALBUM.ordinal -> {
            if (artistId.isEmpty()) {
                viewModel.collectionAlbumPagingData
            } else {
                remember { viewModel.getArtistAlbumsPagingData(artistId) }
            }
        }
        else -> emptyFlow()
    }.collectAsLazyPagingItems()
    val controllerUiState by viewModel.controllerUiState.collectAsState()
    val currentListId by viewModel.currentListId.collectAsState()
    val isPlaying by remember {
        derivedStateOf {
            controllerUiState.playbackState == PlaybackState.PLAYING || controllerUiState.playbackState == PlaybackState.STALLED
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color(0xff080808)
            ),
        contentAlignment = Alignment.Center
    ) {
        PlaylistAlbumListContent(
            title = title,
            dataList = pagingItem,
            currentListId = currentListId,
            isPlaying = isPlaying,
            onClick = { songList ->
                // 跳转到单曲界面
                val route = TidalRoute.getItemTrackListRoute(
                    listId = songList.id,
                    dataType = songList.dataType,
                    coverUrl = songList.coverUrl,
                    title = songList.title,
                    description = songList.description
                )
                navController.navigate(route)
            },
            onBack = {
                navController.popBackSafely()
            }
        )
    }
}

@Composable
private fun PlaylistAlbumListContent(
    title: String = "Playlist",
    glowColor: Color = Color.Transparent,
    dataList: LazyPagingItems<SongList>,
    currentListId: String = "",
    isPlaying: Boolean = false,
    onClick: (SongList) -> Unit = {},
    onBack: () -> Unit
) {
    Column(
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
            .padding(horizontal = 30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
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
//                .drawBehind {
//                    val strokeWidth = 1.dp.toPx()
//                    drawLine(
//                        color = Color.White.copy(alpha = 0.05f),
//                        start = Offset(0f, size.height),
//                        end = Offset(size.width, size.height),
//                        strokeWidth = strokeWidth
//                    )
//                }

        ) {
            Spacer(modifier = Modifier.size(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = title,
                    color = Color.White,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontSize = 30.sp,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        // 主内容
        Spacer(modifier = Modifier.size(15.dp))
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
                if (dataList.itemCount == 0) return
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    items(
                        count = dataList.itemCount,
                        key = { index ->
                            dataList[index]?.id ?: "_${index}_"
                        }
                    ) { index ->
                        val item = dataList[index]
                        item?.apply {
                            PlaylistAlbumItem(
                                item = item,
                                isPlaying = isPlaying,
                                isCurrentList = currentListId == item.id,
                                onClick = onClick
                            )
                        }
                    }
                }
            }

            is LoadState.Error -> {}
        }
    }
}

@Composable
private fun PlaylistAlbumItem(
    item: SongList,
    isCurrentList: Boolean = false,
    isPlaying: Boolean = false,
    onClick: (SongList) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(185.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    onClick = { onClick.invoke(item) }
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = item.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(185.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .blur(if (isCurrentList) 2.dp else 0.dp),
                contentScale = ContentScale.Crop
            )

            if (isCurrentList) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
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
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(5.dp))
        Box(
            modifier = Modifier
                .width(185.dp)
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.title,
                color = if (isCurrentList) Color(0xff00E5FF) else Color(0xffA0A0A0),
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}