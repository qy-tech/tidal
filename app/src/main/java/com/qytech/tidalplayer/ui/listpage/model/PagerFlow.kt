package com.qytech.tidalplayer.ui.listpage.model

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.qytech.tidal.data.paging.Pagination
import com.qytech.tidalplayer.ui.TidalPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retryWhen

object PagerFlow {

    fun <RESPONSE, UI_MODEL : Any> build(
        config: PagingConfig = PagingConfig(20),
        fetchData: suspend (cursor: String?) -> Pair<List<RESPONSE>, Pagination>,
        toUiModel: (RESPONSE) -> UI_MODEL
    ): Flow<PagingData<UI_MODEL>> {
        return Pager(
            config = config,
            pagingSourceFactory = {
                TidalPagingSource(
                    fetchData = fetchData,
                    toUiModel = toUiModel
                )
            }
        ).flow.retryWhen { e, attempt ->
            e.printStackTrace()
            false
        }
    }
}