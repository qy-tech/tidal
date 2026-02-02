package com.qytech.tidalplayer.ui.listpage.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import com.qytech.tidalplayer.ui.listpage.ListPageViewModel
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.ui.listpage.model.SongList
import com.qytech.tidalplayer.utils.popBackSafely
import com.tidal.sdk.player.playbackengine.model.PlaybackState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

private enum class TabTitle {
    Playlists, Tracks, Albums, Artists
}

@Preview(
    showBackground = true,
    widthDp = 1080,
    heightDp = 640
)
@Composable
fun SearchResultPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color.Black
            )
            .padding(50.dp)
    ) {
        SearchResult()
    }
}

@Composable
fun SearchResult(
    navController: NavController? = null,
    searchText: String = ""
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { TabTitle.entries.toTypedArray().size })
    Column() {
        // 头部
        SearchResultHeader(
            selected = TabTitle.entries.toTypedArray()[pagerState.currentPage],
            onSelect = { tab ->
                scope.launch {
                    pagerState.scrollToPage(tab.ordinal)
                }
            }
        )
        Spacer(modifier = Modifier.size(20.dp))
        // 结果
        HorizontalPager(
            state = pagerState,
            pageSpacing = 24.dp,
            beyondViewportPageCount = 1,
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxSize()
        ) { page ->
            val item = TabTitle.entries.toTypedArray()[page]
            when (item) {
                TabTitle.Playlists -> {
                    PlaylistAlbumResult(
                        navController = navController,
                        searchText = searchText,
                        dataType = DataType.PLAY_LIST
                    )
                }

                TabTitle.Tracks -> {
                    TrackResult(
                        searchText = searchText,
                        listId = "${System.currentTimeMillis() * 1000L + Random.nextInt(0,1000)}"
                    )
                }

                TabTitle.Albums -> {
                    PlaylistAlbumResult(
                        navController = navController,
                        searchText = searchText,
                        dataType = DataType.ALBUM
                    )
                }

                TabTitle.Artists -> {
                    ArtistResult(
                        navController = navController,
                        searchText = searchText
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackResult(
    searchText: String,
    listId: String,
) {
    val viewModel: ListPageViewModel = hiltViewModel()
    val dataList = remember {
        viewModel.searchTrack(searchText)
    }.collectAsLazyPagingItems()
    val controllerUiState by viewModel.controllerUiState.collectAsState()
    val currentTrackId by viewModel.currentSongId.collectAsState()
    val isPlaying by remember {
        derivedStateOf {
            controllerUiState.playbackState == PlaybackState.PLAYING || controllerUiState.playbackState == PlaybackState.STALLED
        }
    }
    val favouriteTracks by viewModel.collectionTrackIds.collectAsState()


    HandlePagingError(dataList)

    LaunchedEffect(dataList.itemCount, listId) {
        if (dataList.loadState.refresh is LoadState.NotLoading) {
            viewModel.setCurrentSongList(listId, dataList.itemSnapshotList.items)
            if (!dataList.loadState.append.endOfPaginationReached) {
                if (dataList.itemCount in 1..1000) {
                    // 让他默认全部加载完
                    dataList[dataList.itemCount - 1]
                }
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize(),
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
                    isCurrentTrack = currentTrackId == item.id,
                    isPlaying = isPlaying,
                    isFavourite = favouriteTracks.contains(item.id),
                    onClick = { item ->
                        val totalItemList = dataList.itemSnapshotList.items
                        if (currentTrackId == item.id) {
                            if (isPlaying) {
                                viewModel.pauseSong()
                            } else {
                                viewModel.playSong()
                            }
                            return@TracksItem
                        }
                        viewModel.setCurrentListId(listId)
                        viewModel.setCurrentSongList(
                            listId,
                            totalItemList.subList(
                                (index - 5).coerceAtLeast(0),
                                (index + 6).coerceAtMost(totalItemList.size)
                            )
                        )
                        viewModel.loadAndPlaySong(index, item)
                        viewModel.setControllerShow(true)
                    },
                    onFavourite = { id, isFavourite ->
                        if (isFavourite) {
                            viewModel.removeTrackToCollection(id)
                        } else {
                            viewModel.addTrackToCollection(id)
                        }
                    },
                    onOtherOption = { singleSong ->
                        viewModel.openPanel(
                            dataType = DataType.TRACK,
                            singleSong = singleSong
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ArtistResult(
    navController: NavController? = null,
    searchText: String
) {
    val viewModel: ListPageViewModel = hiltViewModel()
    val pagingItem = remember(searchText) {
        viewModel.searchArtist(searchText)
    }.collectAsLazyPagingItems()
    val controllerUiState by viewModel.controllerUiState.collectAsState()
    val currentArtistId by viewModel.currentArtistId.collectAsState()
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
        ArtistResultContent(
            dataList = pagingItem,
            currentArtistId = currentArtistId,
            isPlaying = isPlaying,
            onClick = { songList ->
                // 跳转到单曲界面
                val route = TidalRoute.getPlaylistAlbumListRoute(
                    title = songList.title,
                    artistId = songList.id,
                    dataType = DataType.ALBUM
                )
                navController?.navigate(route)
            }
        )
    }
}

@Composable
private fun ArtistResultContent(
    glowColor: Color = Color.Transparent,
    dataList: LazyPagingItems<SongList>,
    currentArtistId: String = "",
    isPlaying: Boolean = false,
    onClick: (SongList) -> Unit = {}
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
                        key = dataList.itemKey { it.id }
                    ) { index ->
                        val item = dataList[index]
                        item?.apply {
                            ArtistItem(
                                item = item,
                                isPlaying = isPlaying,
                                isCurrentArtist = currentArtistId == item.id,
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
private fun ArtistItem(
    item: SongList,
    isCurrentArtist: Boolean = false,
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
                .clip(CircleShape)
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
                    .clip(CircleShape)
                    .blur(if (isCurrentArtist) 2.dp else 0.dp),
                contentScale = ContentScale.Crop
            )

            if (isCurrentArtist) {
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
                color = if (isCurrentArtist) Color(0xff00E5FF) else Color(0xffA0A0A0),
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

// PlaylistAlbumResult
@Composable
private fun PlaylistAlbumResult(
    navController: NavController? = null,
    searchText: String = "",
    dataType: DataType
) {
    val viewModel: ListPageViewModel = hiltViewModel()
    val pagingItem = remember(dataType,searchText) {
        when(dataType) {
            DataType.PLAY_LIST -> {
                viewModel.searchPlaylist(searchText)
            }
            DataType.ALBUM -> {
                viewModel.searchAlbum(searchText)
            }
            else -> emptyFlow()
        }
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
        SearchPlaylistAlbumContent(
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
                navController?.navigate(route)
            },
        )
    }
}

@Composable
private fun SearchPlaylistAlbumContent(
    glowColor: Color = Color.Transparent,
    dataList: LazyPagingItems<SongList>,
    currentListId: String = "",
    isPlaying: Boolean = false,
    onClick: (SongList) -> Unit = {},
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
            .padding(horizontal = 20.dp)
    ) {
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
                            SearchPlaylistAlbumItem(
                                item = item,
                                isPlaying = isPlaying,
                                isCurrentList = currentListId == item.id,
                                showDescription = item.dataType == DataType.ALBUM,
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
private fun SearchPlaylistAlbumItem(
    item: SongList,
    isCurrentList: Boolean = false,
    isPlaying: Boolean = false,
    showDescription: Boolean = false,
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
                color = if (isCurrentList) Color(0xff00E5FF) else Color.White,
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
        if (showDescription) {
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
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
private fun SearchResultHeader(
    selected: TabTitle = TabTitle.Playlists,
    onSelect: (TabTitle) -> Unit = {}
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 4.dp.toPx()
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            },
        horizontalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        items(
            items = TabTitle.entries.toTypedArray(),
        ) { item ->
            val isActive = item == selected
            TabItem(
                item = item,
                isActive = isActive,
                onSelect = onSelect
            )
        }
    }
}

@Composable
private fun TabItem(
    item: TabTitle,
    isActive: Boolean = false,
    onSelect: (TabTitle) -> Unit
) {
    Column(
        modifier = Modifier
            .drawBehind {
                val strokeWidth = 4.dp.toPx()
                drawLine(
                    color = if (isActive) Color(0xff00e5ff) else Color.Transparent,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            }
            .clickable(
                onClick = { onSelect.invoke(item) }
            )
    ) {
        Text(
            text = item.name,
            color = if (isActive) Color(0xff00e5ff) else Color(0xffa0a0a0),
            fontSize = 24.sp,
            fontWeight = FontWeight(600)
        )
        Spacer(modifier = Modifier.size(20.dp))
    }
}