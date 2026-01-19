package com.qytech.tidal.network

import com.tencent.mmkv.MMKV
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TidalModule {

    @Provides
    @Singleton
    fun provideMMKV(): MMKV {
        return MMKV.defaultMMKV()
    }
}