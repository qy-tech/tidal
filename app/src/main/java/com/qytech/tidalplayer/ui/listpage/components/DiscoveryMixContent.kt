package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.qytech.tidalplayer.ui.TidalRoute
import com.qytech.tidalplayer.ui.listpage.ListPageViewModel
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.ui.listpage.model.SongList
import com.tidal.sdk.player.playbackengine.model.PlaybackState

@Composable
fun DiscoveryMixContent(
    navController: NavController
) {
    val viewModel: ListPageViewModel = hiltViewModel()
    val discoveryMixesPagingData = viewModel.discoveryMixesPagingData.collectAsLazyPagingItems()
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

    HandlePagingError(discoveryMixesPagingData)

    DiscoveryMixList(
        viewModel,
        dataList = discoveryMixesPagingData,
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
                return@DiscoveryMixList
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
private fun DiscoveryMixList(
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
    var loadingCount by remember { mutableStateOf(0) }

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
                    MixTracks(
                        subItem = item,
                        itemTracks = viewModel::getDiscoveryMixTracksPagingData,
                        playingTrackId = currentTrackId,
                        isPlaying = isPlaying,
                        isFavourite = isFavourite,
                        onClick = { index, song, pagingData ->
                            onClick.invoke(index, song, item.id, pagingData)
                        },
                        onRefresh = onRefresh,
                        onFavourite = onFavourite,
                        onSeeAll = { onSeeAll.invoke(item) },
                        isLoading = { state ->
                            if (state) loadingCount++
                            else loadingCount--
                        }
                    )
                }
            }
        }

        if (loadingCount > 0 || dataList.loadState.refresh is LoadState.Loading) {
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


