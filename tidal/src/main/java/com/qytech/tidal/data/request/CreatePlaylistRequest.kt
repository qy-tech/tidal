package com.qytech.tidal.data.request


data class CreatePlaylistRequest(
    val data: CreatePlaylistData,
)

data class CreatePlaylistData(
    val attributes: CreatePlaylistAttributes,
    val type: String = "playlists",
)

data class CreatePlaylistAttributes(
    val accessType: String = "PUBLIC",
    val description: String,
    val name: String,
)