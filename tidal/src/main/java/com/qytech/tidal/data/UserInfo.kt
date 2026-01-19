package com.qytech.tidal.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfo(
    val id: String,
    val token: String? = null,
    val userName: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
) : Parcelable