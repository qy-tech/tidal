package com.qytech.tidalplayer.ui.listpage.model

import androidx.compose.ui.graphics.Color

data class PageItem(
    val title: String,
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
    PLAY_LIST, // 别人的歌单 + 自己的歌单
    MY_PLAY_LIST, // 自己的歌单
    ALBUM,
    TRACK,
    ARTIST,
    MY_MIX,
    DISCOVERY_MIX,
    NEW_ARRIVAL
}