package com.qytech.tidal.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PageArguments(
    val title: String,
    val id: String,
    val type: String? = null,
) : Parcelable