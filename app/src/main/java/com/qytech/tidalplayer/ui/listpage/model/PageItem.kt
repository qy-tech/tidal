package com.qytech.tidalplayer.ui.listpage.model

import androidx.compose.ui.graphics.Color

data class PageItem(
    val title: String,
    val color: Color,
    val subItems: List<SubItem>,
)

data class SubItem(
    val title: String,
    val itemType: ItemType,
    val dataType: DataType,
    val itemList: List<ItemInfo> = arrayListOf(SongList(), SongList())
)

enum class ItemType{
    SONG_LIST, TRACK, ARTIST
}

enum class DataType{
    PLAY_LIST, ALBUM, TRACK, ARTIST
}