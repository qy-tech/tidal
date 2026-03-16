package com.qytech.tidal.base

sealed class RepoResult<out T> {
    data class Success<T>(
        val data: T
    ) : RepoResult<T>()

    data class Error(
        val error: RepoError
    ) : RepoResult<Nothing>()
}

fun <T> RepoResult<T>.getOrNull(): T? = (this as? RepoResult.Success)?.data

inline fun <T> RepoResult<T>.onSuccess(
    block: (T) -> Unit
): RepoResult<T> {
    if (this is RepoResult.Success) {
        block(data)
    }
    return this
}

inline fun <T> RepoResult<T>.onError(
    block: (RepoError) -> Unit
): RepoResult<T> {
    if (this is RepoResult.Error) {
        block(error)
    }
    return this
}