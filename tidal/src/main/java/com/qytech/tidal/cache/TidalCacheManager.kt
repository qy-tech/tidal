package com.qytech.tidal.cache

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.qytech.tidal.data.PageArguments
import com.qytech.tidal.data.Playlist
import com.qytech.tidal.data.UserInfo
import com.tencent.mmkv.MMKV
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
    }

    fun saveSearchHistory(searchText: String): List<String> {
        val searchHistory = getSearchHistory().toMutableList().apply {
            if (indexOf(searchText) != -1) {
                remove(searchText)
            }
            add(searchText)
        }
        mmkv.encode(CacheKey.SEARCH_HISTORY, Gson().toJson(searchHistory))
        return searchHistory
    }

    fun removeSearchHistory(searchText: String): List<String> {
        val searchHistory = getSearchHistory().toMutableList().apply {
            if (indexOf(searchText) != -1) {
                remove(searchText)
            }
        }
        mmkv.encode(CacheKey.SEARCH_HISTORY, Gson().toJson(searchHistory))
        return searchHistory
    }

    fun getSearchHistory(): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        val emptyListStr = Gson().toJson(emptyList<String>())
        return Gson().fromJson(mmkv.decodeString(CacheKey.SEARCH_HISTORY) ?: emptyListStr, type) ?: emptyList()
    }

    fun clearSearchHistory() {
        mmkv.encode(CacheKey.SEARCH_HISTORY, Gson().toJson(emptyList<String>()))
    }

}