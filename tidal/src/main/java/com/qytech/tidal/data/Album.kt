package com.qytech.tidal.data

data class Album(
    val id: String,
    val title: String,
    val barcodeId: String,
    val numberOfItems: Int,
    val duration: String,
    val releaseDate: String?,
    val coverArts: List<CoverArt>,
    val artists: List<Artist>,
)