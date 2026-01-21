package com.qytech.tidal.data.model

data class TrackAttributes(
    val title: String,
    val version: String?,
    val isrc: String,
    val duration: String,
    val copyright: Copyright,
    val explicit: Boolean,
    val popularity: Double,
    val accessType: String,
    val spotlighted: Boolean,
)