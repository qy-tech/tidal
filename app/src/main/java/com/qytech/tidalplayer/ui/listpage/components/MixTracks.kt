package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.ui.listpage.model.SongList
import com.qytech.tidalplayer.vm.ListPageViewModel
import kotlinx.coroutines.flow.Flow


@Composable
fun MixTracks(
    subItem: SongList = SongList(),
    itemTracks: (String) -> Flow<PagingData<SingleSong>>,
    playingTrackId: String = "",
    isPlaying: Boolean = false,
    isFavourite: (String) -> Boolean = { false },
    onClick: (Int, SingleSong, pagingData: List<SingleSong>) -> Unit = { _, _, _ -> },
    onRefresh: () -> Unit = {},
    onFavourite: (String, Boolean) -> Unit = { _, _ -> },
    onSeeAll: () -> Unit = {},
    isLoading: (Boolean) -> Unit = {},
    onItemOtherOption: (SingleSong) -> Unit = {}
) {
    val viewModel: ListPageViewModel = hiltViewModel()
    val dataList = remember {
        itemTracks.invoke(subItem.id)
    }.collectAsLazyPagingItems()

    isLoading.invoke(dataList.loadState.refresh is LoadState.Loading)

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
                        onOtherOption = onItemOtherOption
                    )
                }
            }
        }
    }
}