package com.qytech.tidal.data.request

import com.qytech.tidal.data.model.PlaylistItemMeta

data class RemoveTracksFromPlaylistRequest(
    val data: List<RemoveTracksFromPlaylistData>,
)

data class RemoveTracksFromPlaylistData(
    val id: String,
    val type: String,
    val meta: PlaylistItemMeta,
)