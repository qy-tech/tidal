package com.qytech.tidal.data.model

data class PlaylistAttributes(
    val name: String,
    val description: String?,
    val bounded: Boolean,
    val createdAt: String,
    val lastModifiedAt: String,
    val accessType: String,
    val playlistType: String,
)