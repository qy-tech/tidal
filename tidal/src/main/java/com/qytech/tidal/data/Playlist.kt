package com.qytech.tidal.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: String,
    val lastModifiedAt: String,
    val coverArts: List<CoverArt>,
) : Parcelable