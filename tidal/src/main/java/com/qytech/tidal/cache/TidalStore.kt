package com.qytech.tidal.cache

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TidalStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tidal_store")

    data class TidalStoreData(
        val searchHistory: List<String> = emptyList(),
        val collectionTrackIds: List<String> = emptyList(),
        val collectionAlbumIds: List<String> = emptyList(),
        val collectArtistIds: List<String> = emptyList(),
        val collectionPlaylistIds: List<String> = emptyList()
    )

    object StoreKeys {
        val SEARCH_HISTORY = stringPreferencesKey("search_history")
        val COLLECTION_TRACK_IDS = stringPreferencesKey("collection_track_ids")
        val COLLECTION_ALBUM_IDS = stringPreferencesKey("collection_album_ids")
        val COLLECTION_ARTIST_IDS = stringPreferencesKey("collection_artist_ids")
        val COLLECTION_PLAYLIST_IDS = stringPreferencesKey("collection_playlist_ids")
    }

    val tidalStoreData: Flow<TidalStoreData> = context.dataStore.data.map { storeData ->
        val listType = object : TypeToken<List<String>>() {}.type

        fun getStringList(storeKey: Preferences.Key<String>): List<String> {
            return storeData[storeKey]?.let {
                Gson().fromJson(it, listType)
            } ?: emptyList()
        }

        TidalStoreData(
            searchHistory = getStringList(StoreKeys.SEARCH_HISTORY),
            collectionTrackIds = getStringList(StoreKeys.COLLECTION_TRACK_IDS),
            collectionAlbumIds = getStringList(StoreKeys.COLLECTION_ALBUM_IDS),
            collectArtistIds = getStringList(StoreKeys.COLLECTION_ARTIST_IDS),
            collectionPlaylistIds = getStringList(StoreKeys.COLLECTION_PLAYLIST_IDS)
        )
    }

    suspend fun updateByKey(storeKey: Preferences.Key<String>, dataList: List<String>) {
        context.dataStore.edit { storeData ->
            if (dataList.isNotEmpty()) {
                storeData[storeKey] = Gson().toJson(dataList)
            }
        }
    }

    suspend fun removeByKey(storeKey: Preferences.Key<String>) {
        context.dataStore.edit { storeData ->
            storeData.remove(storeKey)
        }
    }

}