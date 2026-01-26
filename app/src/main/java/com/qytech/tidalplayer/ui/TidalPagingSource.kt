package com.qytech.tidalplayer.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.qytech.tidal.data.paging.Pagination

class TidalPagingSource<RESPONSE, UI_MODEL : Any>(
    private val fetchData: suspend (cursor: String?) -> Pair<List<RESPONSE>, Pagination>,
    val toUiModel: (RESPONSE) -> UI_MODEL,
) : PagingSource<String, UI_MODEL>() {
    override fun getRefreshKey(state: PagingState<String, UI_MODEL>): String? {
        return null
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, UI_MODEL> {
        val cursor = params.key

        return try {
            val response = fetchData.invoke(cursor)
            val items = response.first
            val nextCursor = response.second.nextCursor
            val nextKey = if (nextCursor.isNullOrBlank() || items.isEmpty()) {
                null
            } else {
                nextCursor
            }

            val uiModelList = arrayListOf<UI_MODEL>()
            for (item in items) {
                uiModelList.add(
                    toUiModel.invoke(item)
                )
            }

            LoadResult.Page(
                data = uiModelList,
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }
}