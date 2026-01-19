package com.qytech.tidal.network

import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.qytech.tidal.api.AuthInterceptor
import com.qytech.tidal.api.TidalApi
import com.qytech.tidal.data.model.IncludedItem
import com.qytech.tidal.utils.IncludedItemDeserializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIDAL_BASE_URL = "https://openapi.tidal.com/v2/"

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class TidalNetwork

    @Provides
    @Singleton
    @TidalNetwork
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @TidalNetwork
    fun provideRetrofit(
        @TidalNetwork okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TIDAL_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().registerTypeAdapter(
                        IncludedItem::class.java,
                        IncludedItemDeserializer()
                    ).setStrictness(Strictness.LENIENT).create()
                )
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideTidalApi(@TidalNetwork retrofit: Retrofit): TidalApi {
        return retrofit.create(TidalApi::class.java)
    }
}