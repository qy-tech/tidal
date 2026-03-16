package com.qytech.tidalplayer.ui.listpage.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.TidalRoute
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.ItemInfo
import com.qytech.tidalplayer.ui.listpage.model.ItemType
import com.qytech.tidalplayer.ui.listpage.model.PageItem
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.ui.listpage.model.SongList
import com.qytech.tidalplayer.vm.ListPageViewModel
import com.tidal.sdk.player.playbackengine.model.PlaybackState
import kotlinx.coroutines.flow.emptyFlow

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
    val favouriteTracks by viewModel.collectionTrackIds.collectAsState()

    val artistData = viewModel.collectionArtistsPagingData.collectAsLazyPagingItems()
    val playlistData = viewModel.collectionPlaylistPagingData.collectAsLazyPagingItems()
    val albumData = viewModel.collectionAlbumPagingData.collectAsLazyPagingItems()
    val trackData = viewModel.collectionTracksPagingData.collectAsLazyPagingItems()
    val emptySongList = emptyFlow<PagingData<SongList>>().collectAsLazyPagingItems()

    // 滑动侧边栏
    val panelState by viewModel.panelState.collectAsState()

    BackHandler(enabled = panelState.showPanel) {
        viewModel.closePanel()
    }

    LaunchedEffect(controllerUiState.playbackState) {
        if (controllerUiState.playbackState == PlaybackState.IDLE) {
            // 清空id
            viewModel.clearIds()
        }
    }

    LaunchedEffect(trackData.itemCount, playingListId) {
        if (trackData.loadState.refresh is LoadState.NotLoading) {
            viewModel.setCurrentSongList(TidalRoute.TRACK_LIST_ID, trackData.itemSnapshotList.items)
            if (!trackData.loadState.append.endOfPaginationReached) {
                if (trackData.itemCount > 0) {
                    // 让他默认全部加载完
                    trackData[trackData.itemCount - 1]
                }
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
                                viewModel.refreshData(subItem.dataType)
                            },
                            onSeeAll = {
                                when (val type = subItem.dataType) {
                                    DataType.PLAY_LIST -> {
                                        navController.navigate(
                                            route = TidalRoute.getPlaylistAlbumListRoute(
                                                title = "Collection Playlists",
                                                dataType = type
                                            )
                                        )
                                    }

                                    DataType.ALBUM -> {
                                        navController.navigate(
                                            route = TidalRoute.getPlaylistAlbumListRoute(
                                                title = "Collection Albums",
                                                dataType = type
                                            )
                                        )
                                    }

                                    else -> {}
                                }
                            }
                        )
                    }

                    ItemType.TRACK -> {
                        val listId = TidalRoute.TRACK_LIST_ID
                        ForYouTracks(
                            playingTrackId = playingSongId,
                            isPlaying = isPlaying,
                            isFavourite = { id -> favouriteTracks.contains(id) },
                            dataList = trackData,
                            onClick = { index, item ->
                                if (playingSongId == item.id) {
                                    if (isPlaying) {
                                        viewModel.pauseSong()
                                    } else {
                                        viewModel.playSong()
                                    }
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
                            onRefresh = { viewModel.refreshData(subItem.dataType) },
                            onFavourite = { id, isFavourite ->
                                if (isFavourite) {
                                    viewModel.removeTrackToCollection(id)
                                } else {
                                    viewModel.addTrackToCollection(id)
                                }
                            },
                            onSeeAll = {
                                val route = TidalRoute.getItemTrackListRoute(
                                    listId = listId,
                                    dataType = DataType.TRACK,
                                    coverUrl = listId,
                                    title = "Collection Tracks",
                                    description = ""
                                )
                                navController.navigate(route)
                            },
                            onItemOtherOption = { singleSong ->
                                viewModel.openPanel(
                                    dataType = DataType.TRACK,
                                    singleSong = singleSong
                                )
                            }
                        )
                    }

                    ItemType.ARTIST -> {
                        ForYouArtists(
                            playingArtistId = playingArtistId,
                            isPlaying = isPlaying,
                            dataList = artistData,
                            onClick = { songList ->
                                // 跳转到歌单界面
                                val route = TidalRoute.getPlaylistAlbumListRoute(
                                    title = songList.title,
                                    artistId = songList.id,
                                    dataType = DataType.ALBUM
                                )
                                navController.navigate(route)
                            },
                            onRefresh = { viewModel.refreshData(subItem.dataType) },
                            onSeeAll = {
                                navController.navigate(
                                    route = TidalRoute.getArtistListRoute(
                                        title = "Collection Artists"
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }

        if (artistData.loadState.refresh is LoadState.Loading
            || trackData.loadState.refresh is LoadState.Loading
            || albumData.loadState.refresh is LoadState.Loading
            || playlistData.loadState.refresh is LoadState.Loading
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
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
    onRefresh: () -> Unit,
    onSeeAll: () -> Unit
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
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable(
                        onClick = onSeeAll
                    )
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
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(100.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ForYouTracks(
    title: String = "Tracks",
    playingTrackId: String = "",
    isPlaying: Boolean = false,
    isFavourite: (String) -> Boolean = { false },
    dataList: LazyPagingItems<SingleSong>,
    onClick: (Int, SingleSong) -> Unit,
    onRefresh: () -> Unit = {},
    onFavourite: (String, Boolean) -> Unit,
    onSeeAll: () -> Unit = {},
    onItemOtherOption: (SingleSong) -> Unit = {}
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
                        onClick = { onClick.invoke(index, item) },
                        onFavourite = onFavourite,
                        onOtherOption = onItemOtherOption
                    )
                }
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
    onRefresh: () -> Unit = {},
    onSeeAll: () -> Unit = {}
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
                fontSize = 14.sp,
                modifier = Modifier.clickable(
                    onClick = onSeeAll
                )
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
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