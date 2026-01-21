package com.qytech.tidal.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfo(
    val id: String,
    val token: String? = null,
    val userName: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
) : Parcelable