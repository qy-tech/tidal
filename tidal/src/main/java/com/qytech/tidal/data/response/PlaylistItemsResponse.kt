package com.qytech.tidal.data.response

import com.qytech.tidal.data.model.Links
import com.qytech.tidal.data.model.PlaylistItem

data class PlaylistItemsResponse(
    val data: List<PlaylistItem>,
    val links: Links,
)