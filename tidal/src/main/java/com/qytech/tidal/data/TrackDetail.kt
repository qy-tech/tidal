package com.qytech.tidal.data

data class TrackDetail(
    val trackInfo: Track,
    val artists: List<Artist>,
    val album: Album,
    val itemId: String? = null,
)