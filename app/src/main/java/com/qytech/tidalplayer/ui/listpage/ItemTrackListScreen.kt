package com.qytech.tidalplayer.ui.listpage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.qytech.tidalplayer.ui.TidalRoute
import com.qytech.tidalplayer.ui.listpage.components.SongListView
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.PanelData
import com.qytech.tidalplayer.ui.listpage.model.SongList
import com.qytech.tidalplayer.utils.popBackSafely
import com.qytech.tidalplayer.vm.ListPageViewModel
import com.tidal.sdk.player.playbackengine.model.PlaybackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun ItemTrackListScreen(
    navController: NavController,
    listId: String,
    dataType: Int,
    coverUrl: String?,
    title: String,
    description: String?
) {
    val viewModel: ListPageViewModel = hiltViewModel()
    val controllerUiState by viewModel.controllerUiState.collectAsState()
    val currentArtistId by viewModel.currentArtistId.collectAsState()
    val currentListId by viewModel.currentListId.collectAsState()
    val currentSongId by viewModel.currentSongId.collectAsState()
    val isPlaying by remember {
        derivedStateOf {
            controllerUiState.playbackState == PlaybackState.PLAYING || controllerUiState.playbackState == PlaybackState.STALLED
        }
    }
    val isPause by remember {
        derivedStateOf {
            controllerUiState.playbackState == PlaybackState.NOT_PLAYING
        }
    }
    val pagingItem = remember {
        when (dataType) {
            DataType.PLAY_LIST.ordinal -> viewModel.getPlaylistItemPagingData(listId)
            DataType.ALBUM.ordinal -> viewModel.getAlbumItemPagingData(listId)
            DataType.TRACK.ordinal -> viewModel.collectionTracksPagingData
            else -> emptyFlow()
        }
    }.collectAsLazyPagingItems()
    val favouriteTracks by viewModel.collectionTrackIds.collectAsState()

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

    LaunchedEffect(pagingItem.itemCount, currentListId) {
        val refresh = pagingItem.loadState.refresh
        if (refresh is LoadState.NotLoading) {
            viewModel.setCurrentSongList(listId, pagingItem.itemSnapshotList.items)
            if (!pagingItem.loadState.append.endOfPaginationReached) {
                if (pagingItem.itemCount > 0) {
                    pagingItem[pagingItem.itemCount - 1] // 让他默认全部加载完
                }
            }
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
        SongListView(
            coverUrl = coverUrl,
            title = title,
            description = description,
            daraList = pagingItem,
            isCurrentListIdPlaying = currentListId == listId && isPlaying,
            currentSongId = currentSongId,
            isFavourite = { id -> favouriteTracks.contains(id) },
            onItemClick = { index, currentSong ->
                if (currentSongId == currentSong.id) {
                    when {
                        isPlaying -> {
                            viewModel.pauseSong()
                            return@SongListView
                        }
                        isPause -> {
                            viewModel.playSong()
                            return@SongListView
                        }
                        else -> {}
                    }
                }
                viewModel.setCurrentListId(listId)
                // 将所有歌曲id添加到控制器中
                viewModel.setCurrentSongList(
                    listId,
                    pagingItem.itemSnapshotList.items.subList(
                        (index - 5).coerceAtLeast(0),
                        (index + 6).coerceAtMost(pagingItem.itemCount)
                    )
                )
                viewModel.loadAndPlaySong(index, currentSong)
                viewModel.setControllerShow(true)

            },
            onBack = {
                navController.popBackSafely()
            },
            onFavourite = { id, isFavourite ->
                if (isFavourite) {
                    viewModel.removeTrackToCollection(id)
                } else {
                    viewModel.addTrackToCollection(id)
                }
            },
            onPlaySequentially = {
                val list = pagingItem.itemSnapshotList.items
                if (list.isEmpty()) return@SongListView
                if (currentListId == listId) {
                    when {
                        isPlaying -> {
                            viewModel.pauseSong()
                            return@SongListView
                        }
                        isPause -> {
                            viewModel.playSong()
                            return@SongListView
                        }
                        else -> {}
                    }
                }
                viewModel.setCurrentListId(listId)
                viewModel.setCurrentSongList(
                    listId,
                    list.subList(
                        0,
                        10.coerceAtMost(pagingItem.itemCount)
                    )
                )
                viewModel.loadAndPlaySong(0, list[0])
                viewModel.setControllerShow(true)
            },
            onOtherOption = {
                val type = DataType.entries.toTypedArray()[dataType]
                if (type == DataType.TRACK) {
                    viewModel.openPanel(
                        dataType = DataType.PLAY_LIST,
                        songList = SongList(
                            id = listId,
                            coverUrl = TidalRoute.TRACK_LIST_ID,
                            title = title,
                            description = description
                        ),
                        lazyList = pagingItem
                    )
                } else {
                    viewModel.openPanel(
                        dataType = type,
                        songList = SongList(
                            id = listId,
                            coverUrl = coverUrl,
                            title = title,
                            description = description
                        ),
                        lazyList = pagingItem
                    )
                }
            },
            onItemOtherOption = { item ->
                viewModel.openPanel(
                    dataType = DataType.TRACK,
                    singleSong = item,
                    songList = SongList(
                        id = listId,
                        coverUrl = coverUrl,
                        title = title,
                        description = description
                    ),
                )
            }
        )
    }
}