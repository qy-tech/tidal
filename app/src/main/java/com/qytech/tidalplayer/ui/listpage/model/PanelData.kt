package com.qytech.tidalplayer.ui.listpage.model

import androidx.paging.compose.LazyPagingItems

data class PanelData(
    val showPanel: Boolean = false,
    val dataType: DataType? = null,
    val lazyList: LazyPagingItems<SingleSong>? = null,
    val songList: SongList? = null,
    val singleSong: SingleSong? = null
)