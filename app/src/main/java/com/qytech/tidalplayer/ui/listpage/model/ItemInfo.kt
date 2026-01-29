package com.qytech.tidalplayer.ui.listpage.model

import com.qytech.tidal.data.Album
import com.qytech.tidal.data.Artist

interface ItemInfo {
    val id: String
    val title: String
    val description: String?
    val coverUrl: String?
    val itemList: List<ItemInfo>
    val dataType: DataType
}

data class SongList(
    override val id: String = "",
    override val title: String = "song list",
    override val description: String? = "",
    override val itemList: List<ItemInfo> = arrayListOf(SingleSong(), SingleSong()),
    override val coverUrl: String? = "https://resources.tidal.com/images/a3dc8eb8/5f0c/47e8/b5d8/07ed4e78d5f1/160x160.jpg",
    override val dataType: DataType = DataType.PLAY_LIST,
) : ItemInfo

data class SingleSong(
    override val id: String = "",
    override val title: String = "single song",
    override val description: String? = "",
    override val itemList: List<ItemInfo> = emptyList(),
    override val coverUrl: String? = "https://resources.tidal.com/images/a3dc8eb8/5f0c/47e8/b5d8/07ed4e78d5f1/160x160.jpg",
    override val dataType: DataType = DataType.TRACK,
    val duration: String = "3M38S",
    val version: String = "",
    val artists: List<Artist> = emptyList(),
    val album: Album? = null
) : ItemInfo {
    fun getDetailTitle(): String {
        return "${title}${if (version.isNotBlank()) "（${version}）" else ""}"
    }
}

data class Artist(
    override val id: String = "",
    override val title: String = "",
    override val description: String? = "",
    override val coverUrl: String? = null,
    override val itemList: List<ItemInfo> = emptyList(),
    override val dataType: DataType = DataType.ARTIST
): ItemInfo