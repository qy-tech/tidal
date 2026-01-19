package com.qytech.tidal.data.paging

import com.qytech.tidal.data.Album
import com.qytech.tidal.data.paging.Pagination

data class AlbumList(
    val albums: List<Album>,
    val pagination: Pagination,
)