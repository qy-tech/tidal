package com.qytech.tidal.data.request

data class AddTracksToPlaylistRequest(
    val data: List<AddTracksToPlaylistData>,
)

data class AddTracksToPlaylistData(
    val type: String,
    val id: String,
)