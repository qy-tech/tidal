package com.qytech.tidal.data.response

import com.qytech.tidal.data.model.AlbumsItem
import com.qytech.tidal.data.model.Links

data class AlbumsItemsResponse(
    val data: List<AlbumsItem>,
    val links: Links,
)