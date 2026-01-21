package com.qytech.tidalplayer.ui.listpage.model

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
    override val description: String? = null,
    override val itemList: List<ItemInfo> = arrayListOf(SingleSong(), SingleSong()),
    override val coverUrl: String? = null,
    override val dataType: DataType = DataType.PLAY_LIST,
) : ItemInfo

data class SingleSong(
    override val id: String = "",
    override val title: String = "single song",
    override val description: String? = null,
    override val itemList: List<ItemInfo> = emptyList(),
    override val coverUrl: String? = null,
    override val dataType: DataType = DataType.TRACK,
    val duration: String = "3M38S",
    val version: String = ""
) : ItemInfo

data class Artist(
    override val id: String = "",
    override val title: String = "",
    override val description: String? = null,
    override val coverUrl: String? = null,
    override val itemList: List<ItemInfo> = emptyList(),
    override val dataType: DataType = DataType.ARTIST
): ItemInfo