package com.qytech.tidal.data.model

data class AlbumAttributes(
    val title: String,
    val barcodeId: String,
    val numberOfVolumes: Int,
    val numberOfItems: Int,
    val duration: String,
    val explicit: Boolean,
    val releaseDate: String,
    val copyright: Copyright,
    val popularity: Double,
)