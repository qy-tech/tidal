package com.qytech.tidal.base

import com.qytech.tidal.data.paging.Pagination

data class PagingResponse<T>(
    val data: List<T> = emptyList(),
    val pagination: Pagination = Pagination()
) {
}