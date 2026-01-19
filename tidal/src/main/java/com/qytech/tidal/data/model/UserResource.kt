package com.qytech.tidal.data.model

data class UserResource(
    val id: String,
    val type: String,
    val attributes: UserAttributes,
)