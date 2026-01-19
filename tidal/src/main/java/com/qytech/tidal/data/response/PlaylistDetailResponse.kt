package com.qytech.tidal.data.response

import com.qytech.tidal.data.model.IncludedItem
import com.qytech.tidal.data.model.PlaylistResource

data class PlaylistDetailResponse(
    val data: PlaylistResource,
    val included: List<IncludedItem>,
)