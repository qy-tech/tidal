package com.qytech.tidal.base

import kotlinx.coroutines.delay
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> apiCall(
    maxRetry: Int = 3,
    execute: suspend () -> T
): RepoResult<T> {
    repeat(maxRetry) { attempt ->
        try {
            val data = execute()
            return RepoResult.Success(data)
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() != 429) {
                return RepoResult.Error(e.toRepoError())
            }
            if (attempt == maxRetry - 1) {
                return RepoResult.Error(RepoError.HttpError(429, "重试次数已达到上限"))
            }

            val seconds = e.response()
                ?.headers()
                ?.get("retry-after")
                ?.toLongOrNull()
                ?: 2L

            delay(seconds * 1000)
        } catch (e: Throwable) {
            return RepoResult.Error(e.toRepoError())
        }
    }

    error("unreachable")
}