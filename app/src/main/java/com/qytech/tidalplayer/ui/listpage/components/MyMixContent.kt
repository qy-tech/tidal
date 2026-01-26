package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.TidalRoute
import com.qytech.tidalplayer.ui.listpage.ListPageViewModel
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.ui.listpage.model.SongList
import com.tidal.sdk.player.playbackengine.model.PlaybackState

@Composable
fun MyMixContent(
    navController: NavController
) {
    val viewModel: ListPageViewModel = hiltViewModel()
    val myMixesPagingData = viewModel.myMixesPagingData.collectAsLazyPagingItems()
    val controllerUiState by viewModel.controllerUiState.collectAsState()
    val currentTrackId by viewModel.currentSongId.collectAsState()
    val isPlaying by remember {
        derivedStateOf {
            controllerUiState.playbackState == PlaybackState.PLAYING || controllerUiState.playbackState == PlaybackState.STALLED
        }
    }
    val favouriteTracks by viewModel.collectionTrackIds.collectAsState()


    LaunchedEffect(controllerUiState.playbackState) {
        if (controllerUiState.playbackState == PlaybackState.IDLE) {
            // 清空id
            viewModel.clearIds()
        }
    }


    HandlePagingError(myMixesPagingData)

    MyMixList(
        viewModel,
        dataList = myMixesPagingData,
        currentTrackId = currentTrackId,
        isPlaying = isPlaying,
        isFavourite = { id -> favouriteTracks.contains(id) },
        onClick = { index, item, listId, pagingData ->
            if (currentTrackId == item.id) {
                if (isPlaying) {
                    viewModel.pauseSong()
                } else {
                    viewModel.playSong()
                }
                return@MyMixList
            }
            viewModel.setCurrentListId(listId)
            viewModel.setCurrentSongList(
                listId,
                pagingData.subList(
                    (index - 5).coerceAtLeast(0),
                    (index + 6).coerceAtMost(pagingData.size)
                )
            )
            viewModel.loadAndPlaySong(index, item)
            viewModel.setControllerShow(true)
        },
        onRefresh = {
            // todo
        },
        onFavourite = { id, isFavourite ->
            if (isFavourite) {
                viewModel.removeTrackToCollection(id)
            } else {
                viewModel.addTrackToCollection(id)
            }
        },
        onSeeAll = { subItem ->
            val route = TidalRoute.getItemTrackListRoute(
                listId = subItem.id,
                dataType = DataType.PLAY_LIST,
                coverUrl = subItem.coverUrl,
                title = subItem.title,
                description = subItem.description ?: ""
            )
            navController.navigate(route)
        }
    )
}

@Composable
private fun MyMixList(
    viewModel: ListPageViewModel,
    dataList: LazyPagingItems<SongList>,
    currentTrackId: String = "",
    isPlaying: Boolean = false,
    isFavourite: (String) -> Boolean = { false },
    onClick: (Int, SingleSong, String, pagingData: List<SingleSong>) -> Unit,
    onRefresh: () -> Unit = {},
    onFavourite: (String, Boolean) -> Unit,
    onSeeAll: (SongList) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            for (index in 0..<dataList.itemCount) {
                val item = dataList[index]
                item?.apply {
                    MyMixTracks(
                        viewModel = viewModel,
                        subItem = item,
                        playingTrackId = currentTrackId,
                        isPlaying = isPlaying,
                        isFavourite = isFavourite,
                        onClick = { index, song, pagingData ->
                            onClick.invoke(index, song, item.id, pagingData)
                        },
                        onRefresh = onRefresh,
                        onFavourite = onFavourite,
                        onSeeAll = { onSeeAll.invoke(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MyMixTracks(
    viewModel: ListPageViewModel,
    subItem: SongList = SongList(),
    playingTrackId: String = "",
    isPlaying: Boolean = false,
    isFavourite: (String) -> Boolean = { false },
    onClick: (Int, SingleSong, pagingData: List<SingleSong>) -> Unit = { _, _, _ -> },
    onRefresh: () -> Unit = {},
    onFavourite: (String, Boolean) -> Unit = { _, _ -> },
    onSeeAll: () -> Unit = {}
) {
    val dataList = remember {
        viewModel.getMyMixTracksPagingData(subItem.id)
    }.collectAsLazyPagingItems()

    HandlePagingError(dataList)

    LaunchedEffect(dataList.itemCount, subItem.id) {
        if (dataList.loadState.refresh is LoadState.NotLoading) {
            viewModel.setCurrentSongList(subItem.id, dataList.itemSnapshotList.items)
            if (!dataList.loadState.append.endOfPaginationReached) {
                if (dataList.itemCount > 0) {
                    // 让他默认全部加载完
                    dataList[dataList.itemCount - 1]
                }
            }
        }
    }

    Column(
        modifier = Modifier.background(
            color = Color.Black
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subItem.title,
                color = Color.White,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                fontWeight = FontWeight(700),
                fontSize = 20.sp,
                modifier = Modifier.clickable(
                    onClick = onRefresh
                )
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
                fontSize = 14.sp,
                modifier = Modifier.clickable(
                    onClick = onSeeAll
                )
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
        // item
        if (dataList.itemCount == 0) return
        LazyHorizontalGrid(
            rows = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                count = dataList.itemCount,
                key = dataList.itemKey { it.id }
            ) { index ->
                val item = dataList[index]
                item?.apply {
                    TracksItem(
                        item = item,
                        isCurrentTrack = playingTrackId == item.id,
                        isPlaying = isPlaying,
                        isFavourite = isFavourite.invoke(item.id),
                        onClick = { onClick.invoke(index, item, dataList.itemSnapshotList.items) },
                        onFavourite = onFavourite,
                        onOtherOption = {
                            // todo
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TracksItem(
    item: SingleSong = SingleSong(),
    isCurrentTrack: Boolean = false,
    isPlaying: Boolean = false,
    isFavourite: Boolean = false,
    onClick: (SingleSong) -> Unit = {},
    onFavourite: (String, Boolean) -> Unit = { _, _ -> },
    onOtherOption: (SingleSong) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .width(400.dp)
            .height(80.dp)
            .background(
                color = if (isCurrentTrack) Color(0x1400e5ff) else Color(0x10ffffff),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = if (isCurrentTrack) Color(0x4d00e5ff) else Color(0x12ffffff),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(vertical = 10.dp)
            .padding(start = 10.dp, end = 0.dp)
            .clickable(onClick = { onClick.invoke(item) }),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
        ) {
            Box {
                AsyncImage(
                    model = item.coverUrl ?: "",
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(
                            shape = RoundedCornerShape(10.dp)
                        )
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(10.dp),
                            clip = false
                        )
                )

                if (isCurrentTrack) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                color = Color(0x99000000),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) {
                                ImageVector.vectorResource(R.drawable.icon_pause_solid_full)
                            } else {
                                ImageVector.vectorResource(R.drawable.icon_play_solid_full)
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .weight(1f)
            ) {
                Text(
                    text = item.getDetailTitle(),
                    color = if (isCurrentTrack) Color(0xff00E5FF) else Color.White,
                    fontWeight = FontWeight(600),
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    text = item.description ?: "No description",
                    color = Color(0xffA0A0A0),
                    fontWeight = FontWeight(500),
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }


            IconButton(
                onClick = { onFavourite.invoke(item.id, isFavourite) }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(if (isFavourite) R.drawable.icon_heart_solid_full else R.drawable.icon_heart_regular_full),
                    contentDescription = null,
                    tint = if (isFavourite) Color(0xff00E5FF) else Color(0xffA0A0A0),
                    modifier = Modifier.size(25.dp)
                )
            }

            Text(
                text = item.duration,
                color = Color(0xffA0A0A0),
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                fontSize = 12.sp
            )

            IconButton(
                onClick = { onOtherOption.invoke(item) }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_ellipsis_solid_full),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = Color.White
                )
            }
        }
    }
}