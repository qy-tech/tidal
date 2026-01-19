package com.qytech.tidal.api

import com.qytech.tidal.cache.TidalCacheManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tidalCacheManager: TidalCacheManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()

        val token = tidalCacheManager.getUserInfo()?.token ?: ""

        // 创建新的请求，添加所有必要的头部
        val newRequest = originalRequest.newBuilder()
            .header("Charset", "UTF-8")
            .header("Authorization", "Bearer $token")
            .build()

        // 执行请求
        val response = chain.proceed(newRequest)

        return response
    }

}