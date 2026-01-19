package com.qytech.tidal.data.model

data class ArtworkResource(
    val id: String,
    val type: String,
    val attributes: ArtworkAttributes,
) : IncludedItem