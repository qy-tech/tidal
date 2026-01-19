package com.qytech.tidal.data.model

data class Links(
    val self: String,
    val next: String?,
    val meta: LinksMeta?,
)

data class LinksMeta(
    val nextCursor: String,
)