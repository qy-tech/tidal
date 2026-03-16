package com.qytech.tidal.base

import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException

sealed class RepoError {

    /** 网络不可用，例如无网络、DNS失败 */
    data object NetworkUnavailable : RepoError()

    /** 请求超时 */
    data object Timeout : RepoError()

    /** HTTP错误，例如 404 / 500 */
    data class HttpError(
        val code: Int,
        val message: String? = null
    ) : RepoError()

    /** 服务端业务错误 */
    data class ServerError(
        val code: Int,
        val message: String
    ) : RepoError()

    /** 数据解析错误 */
    data class ParseError(
        val message: String? = null
    ) : RepoError()

    /** 本地数据库错误 */
    data class DatabaseError(
        val message: String? = null
    ) : RepoError()

    /** 数据为空（例如接口返回空） */
    data object EmptyData : RepoError()

    /** 权限错误，例如 401 */
    data object Unauthorized : RepoError()

    /** 未知错误 */
    data class Unknown(
        val throwable: Throwable? = null
    ) : RepoError()
}

fun Throwable.toRepoError(): RepoError {

    return when (this) {

        is java.net.UnknownHostException ->
            RepoError.NetworkUnavailable

        is java.net.SocketTimeoutException ->
            RepoError.Timeout

        is java.io.IOException ->
            RepoError.NetworkUnavailable

        is retrofit2.HttpException -> {
            when (code()) {
                401 -> RepoError.Unauthorized
                else -> RepoError.HttpError(code(), message())
            }
        }

        is JsonSyntaxException,
        is JsonParseException,
        is kotlinx.serialization.SerializationException ->
            RepoError.ParseError(message)

        is android.database.SQLException ->
            RepoError.DatabaseError(message)

        else ->
            RepoError.Unknown(this)
    }
}