package com.qytech.tidal.data.response

import com.qytech.tidal.data.model.IncludedItem
import com.qytech.tidal.data.model.Links
import com.qytech.tidal.data.model.PlaylistResource

data class PlaylistsResponse(
    val data: List<PlaylistResource>,
    val links: Links,
    val included: List<IncludedItem>,
)