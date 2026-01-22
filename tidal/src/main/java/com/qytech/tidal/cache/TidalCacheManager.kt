package com.qytech.tidal.cache

import com.qytech.tidal.data.PageArguments
import com.qytech.tidal.data.Playlist
import com.qytech.tidal.data.UserInfo
import com.tencent.mmkv.MMKV
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TidalCacheManager @Inject constructor(
    private val mmkv: MMKV
) {

    fun savePageArguments(pageArguments: PageArguments) {
        mmkv.encode(CacheKey.PAGE_ARGUMENTS, pageArguments)
    }

    fun getPageArguments(): PageArguments? {
        val res = mmkv.decodeParcelable(CacheKey.PAGE_ARGUMENTS, PageArguments::class.java)
        clearPageArguments()
        return res
    }

    private fun clearPageArguments() {
        mmkv.remove(CacheKey.PAGE_ARGUMENTS)
    }

    fun saveCurrentPlaylist(playlist: Playlist) {
        mmkv.encode(CacheKey.CURRENT_PLAYLIST, playlist)
    }

    fun getCurrentPlaylist(): Playlist? {
        val res = mmkv.decodeParcelable(CacheKey.CURRENT_PLAYLIST, Playlist::class.java)
        clearCurrentPlaylist()
        return res
    }

    private fun clearCurrentPlaylist() {
        mmkv.remove(CacheKey.CURRENT_PLAYLIST)
    }

    fun saveUserInfo(userInfo: UserInfo) {
        mmkv.encode(CacheKey.USER_INFO, userInfo)
    }

    fun getUserInfo(): UserInfo? {
        return mmkv.decodeParcelable(CacheKey.USER_INFO, UserInfo::class.java)
    }

    fun clearUserInfo() {
        mmkv.remove(CacheKey.USER_INFO)
        Timber.d("isLoggedIn cacheLoggedIn clear")
    }

}