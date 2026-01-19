package com.qytech.tidal.data.model

data class PlaylistResource(
    val id: String,
    val type: String,
    val attributes: PlaylistAttributes,
    val relationships: PlaylistRelationship,
) : IncludedItem