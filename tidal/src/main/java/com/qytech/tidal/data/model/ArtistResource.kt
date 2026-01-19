package com.qytech.tidal.data.model

import com.qytech.tidal.data.Artist


data class ArtistResource(
    val id: String,
    val type: String,
    val attributes: ArtistAttributes,
    val relationships: ArtistRelationship,
) : IncludedItem

fun List<ArtistResource>.toArtistList() = this.map {
    Artist(
        id = it.id,
        name = it.attributes.name
    )
}