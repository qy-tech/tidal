package com.qytech.tidal.data.paging

import com.qytech.tidal.data.ArtistDetail
import com.qytech.tidal.data.paging.Pagination

data class ArtistList(
    val artists: List<ArtistDetail>,
    val pagination: Pagination,
)