package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import kotlinx.coroutines.delay
import retrofit2.HttpException

@Composable
fun <T : Any> HandlePagingError(pagingData: LazyPagingItems<T>) {
    LaunchedEffect(pagingData.loadState) {
        suspend fun handleError(e: HttpException?) {
            if (e?.code() == 429) {
                val seconds = e.response()
                    ?.headers()
                    ?.get("retry-after")
                    ?.toLongOrNull() ?: 2
                delay(seconds * 1000L) // 至少等一个窗口
                pagingData.retry()
            }
        }

        val refresh = pagingData.loadState.refresh
        val append = pagingData.loadState.append
        if (refresh is LoadState.Error) {
            handleError(refresh.error as? HttpException)
        } else if (append is LoadState.Error) {
            handleError(append.error as? HttpException)
        }
    }
}
