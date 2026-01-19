package com.qytech.tidal.data.model

import com.qytech.tidal.data.Track


data class TrackResource(
    val id: String,
    val type: String,
    val attributes: TrackAttributes,
    val relationships: TrackRelationship,
) : IncludedItem

fun TrackResource.toTrack() = Track(
    id = this.id,
    title = this.attributes.title,
    duration = this.attributes.duration
)

fun List<TrackResource>.toTrackList() = this.map { it -> it.toTrack() }