package com.qytech.tidal.data.model

data class AlbumResource(
    val id: String,
    val type: String,
    val attributes: AlbumAttributes,
    val relationships: AlbumRelationship,
) : IncludedItem