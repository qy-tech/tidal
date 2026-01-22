package com.qytech.tidalplayer.ui.listpage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.qytech.tidalplayer.ui.listpage.components.SongListView
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.utils.popBackSafely
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun ItemTrackListScreen(
    navController: NavController,
    listId: String,
    dataType: Int,
    coverUrl: String?,
    title: String,
    description: String?,
) {
    val viewModel: ListPageViewModel = hiltViewModel()
    val currentListId by viewModel.currentListId.collectAsState()
    val currentSongId by viewModel.currentSongId.collectAsState()
    val pagingItem = remember {
        when (dataType) {
            DataType.PLAY_LIST.ordinal -> viewModel.getPlaylistItemPagingData(listId, dataType)
            DataType.ALBUM.ordinal -> viewModel.getAlbumItemPagingData(listId, dataType)
            else -> emptyFlow()
        }
    }.collectAsLazyPagingItems()

    LaunchedEffect(pagingItem.itemCount, currentListId) {
        if (pagingItem.loadState.refresh is LoadState.NotLoading) {
            viewModel.setCurrentSongList(listId, pagingItem.itemSnapshotList.items)
            if (!pagingItem.loadState.append.endOfPaginationReached) {
                if (pagingItem.itemCount > 0) pagingItem[pagingItem.itemCount - 1] // 让他默认全部加载完
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
            currentSongId = currentSongId,
            isFavourite = viewModel::checkFavouriteTrack,
            onItemClick = { index, currentSong ->
                // 将所有歌曲id添加到控制器中
                viewModel.setCurrentListId(listId)
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
            }
        )
    }
}