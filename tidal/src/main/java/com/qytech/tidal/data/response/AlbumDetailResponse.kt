package com.qytech.tidal.data.response

import com.qytech.tidal.data.model.AlbumResource
import com.qytech.tidal.data.model.IncludedItem

data class AlbumDetailResponse(
    val data: AlbumResource,
    val included: List<IncludedItem>,
)