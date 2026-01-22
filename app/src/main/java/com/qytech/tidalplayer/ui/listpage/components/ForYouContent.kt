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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.qytech.tidal.data.toDisplayDuration
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.TidalRoute
import com.qytech.tidalplayer.ui.listpage.ListPageViewModel
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.ItemInfo
import com.qytech.tidalplayer.ui.listpage.model.ItemType
import com.qytech.tidalplayer.ui.listpage.model.PageItem
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
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
    val playingArtistId by viewModel.currentArtistId.collectAsState()
    val playingListId by viewModel.currentListId.collectAsState()
    val playingSongId by viewModel.currentSongId.collectAsState()
    val isPlaying by remember {
        derivedStateOf {
            controllerUiState.playbackState == PlaybackState.PLAYING || controllerUiState.playbackState == PlaybackState.STALLED
        }
    }

    val artistData = viewModel.collectionArtistsPagingData.collectAsLazyPagingItems()
    val playlistData = viewModel.collectionPlaylistPagingData.collectAsLazyPagingItems()
    val albumData = viewModel.collectionAlbumPagingData.collectAsLazyPagingItems()
    val trackData = viewModel.collectionTracksPagingData.collectAsLazyPagingItems()
    val emptySongList = emptyFlow<PagingData<SongList>>().collectAsLazyPagingItems()

    LaunchedEffect(trackData.itemCount, playingListId) {
        if (trackData.loadState.refresh is LoadState.NotLoading) {
            viewModel.setCurrentSongList(playingListId, trackData.itemSnapshotList.items)
            if (!trackData.loadState.append.endOfPaginationReached) {
                if (trackData.itemCount > 0) trackData[trackData.itemCount - 1] // 让他默认全部加载完
            }
        }
    }

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
                        ForYouSongList(
                            title = subItem.title,
                            playingListId = playingListId,
                            isPlaying = isPlaying,
                            dataList = when (subItem.dataType) {
                                DataType.PLAY_LIST -> playlistData
                                DataType.ALBUM -> albumData
                                else -> emptySongList
                            },
                            onClick = { item ->
                                // 跳转到单曲界面
                                val route = TidalRoute.getItemTrackListRoute(
                                    listId = item.id,
                                    dataType = item.dataType,
                                    coverUrl = item.coverUrl,
                                    title = item.title,
                                    description = item.description
                                )
                                navController.navigate(route)
                            },
                            onRefresh = {
                                viewModel.refreshAllCollection(subItem.dataType)
                            }
                        )
                    }

                    ItemType.TRACK -> {
                        val listId = "for you tracks"
                        ForYouTracks(
                            playingTrackId = playingSongId,
                            isPlaying = isPlaying,
                            isFavourite = viewModel::checkFavouriteTrack,
                            dataList = trackData,
                            onClick = { index, item ->
                                if (playingSongId == item.id && isPlaying) {
                                    viewModel.pauseSong()
                                    return@ForYouTracks
                                }
                                viewModel.setCurrentListId(listId)
                                viewModel.setCurrentSongList(
                                    listId,
                                    trackData.itemSnapshotList.items.subList(
                                        (index - 5).coerceAtLeast(0),
                                        (index + 6).coerceAtMost(trackData.itemCount)
                                    )
                                )
                                viewModel.loadAndPlaySong(index, item)
                                viewModel.setControllerShow(true)
                            },
                            onRefresh = { viewModel.refreshAllCollection(subItem.dataType) }
                        )
                    }

                    ItemType.ARTIST -> {
                        ForYouArtists(
                            playingArtistId = playingArtistId,
                            isPlaying = isPlaying,
                            dataList = artistData,
                            onClick = {

                            },
                            onRefresh = { viewModel.refreshAllCollection(subItem.dataType) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForYouArtists(
    title: String = "Artists",
    playingArtistId: String = "",
    isPlaying: Boolean = false,
    dataList: LazyPagingItems<SongList>,
    onClick: (SongList) -> Unit,
    onRefresh: () -> Unit
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
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
        // item
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                count = dataList.itemCount,
                key = dataList.itemKey { it.id }
            ) { index ->
                val item = dataList[index]
                item?.apply {
                    ArtistsItem(
                        item = item,
                        isCurrentArtist = playingArtistId == item.id,
                        isPlaying = isPlaying,
                        onClick = onClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistsItem(
    item: SongList,
    isCurrentArtist: Boolean = false,
    isPlaying: Boolean = false,
    onClick: (SongList) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .clickable(
                onClick = { onClick.invoke(item) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = item.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .blur(if (isCurrentArtist) 2.dp else 0.dp)
            )

            if (isCurrentArtist) {
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
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = item.title,
            color = if (isCurrentArtist) Color(0xff00E5FF) else Color(0xffA0A0A0),
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
}

@Composable
private fun ForYouTracks(
    title: String = "Tracks",
    playingTrackId: String = "",
    isPlaying: Boolean = false,
    isFavourite: (String) -> Boolean = {false},
    dataList: LazyPagingItems<SingleSong>,
    onClick: (Int, SingleSong) -> Unit,
    onRefresh: () -> Unit = {}
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
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
        // item
        if (dataList.itemCount == 0) return
        LazyHorizontalGrid(
            rows = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth()
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
                        onClick = { onClick.invoke(index, item) },
                        onFavourite = {

                        },
                        onOtherOption = {

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
    onFavourite: (SingleSong) -> Unit = {},
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
                onClick = { onFavourite.invoke(item) }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_heart_solid_full),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = if (isFavourite) Color(0xff00E5FF) else Color.White
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

@Composable
private fun <T : ItemInfo> ForYouSongList(
    title: String = "Mixes for you",
    playingListId: String = "",
    isPlaying: Boolean = false,
    dataList: LazyPagingItems<T>,
    onClick: (ItemInfo) -> Unit = {},
    onRefresh: () -> Unit = {}
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
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
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
    modifier: Modifier = Modifier,
    item: ItemInfo,
    isCurrentList: Boolean = true,
    isPlaying: Boolean = false,
    onClick: (ItemInfo) -> Unit = {},
) {
    Column(
        modifier = modifier
            .clickable(
                onClick = { onClick.invoke(item) }
            )
    ) {
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