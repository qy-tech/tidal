package com.qytech.tidalplayer.ui.listpage.model

sealed interface ChannelType {
    val result: Boolean

    // 检查权限
    data class CheckAuth(override val result: Boolean): ChannelType
    // 操作歌单
    data class CreatePlaylist(
        override val result: Boolean,
        val data: SongList
    ) : ChannelType
    data class DeletePlaylist(override val result: Boolean): ChannelType
}

