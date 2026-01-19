package com.qytech.tidal.data.model

data class UserAttributes(
    val username: String,
    val country: String,
    val email: String,
    val emailVerified: Boolean,
    val firstName: String,
    val lastName: String,
)
