package com.qytech.tidalplayer.ui.listpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.qytech.tidal.cache.TidalCacheManager
import com.qytech.tidal.data.Album
import com.qytech.tidal.data.Artist
import com.qytech.tidal.data.CoverArt
import com.qytech.tidal.data.Playlist
import com.qytech.tidal.data.TrackDetail
import com.qytech.tidal.data.paging.Pagination
import com.qytech.tidal.data.toDisplayDuration
import com.qytech.tidal.repository.TidalRepository
import com.qytech.tidalplayer.ui.TidalPagingSource
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.ui.listpage.model.SongList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListPageViewModel @Inject constructor(
    private val controllerManager: ControllerManager,
    private val tidalCacheManager: TidalCacheManager,
    private val tidalRepository: TidalRepository
) : ViewModel() {

    val controllerUiState = controllerManager.controllerUiState
    val currentListId = controllerManager.currentListId

    val playlistPagingData = Pager(
        config = PagingConfig(20),
        pagingSourceFactory = {
            TidalPagingSource(
                fetchData = { cursor ->
                    val userId = tidalCacheManager.getUserInfo()?.id
                    if (userId.isNullOrBlank()) {
                        emptyList<Playlist>() to Pagination()
                    } else {
                        val response = tidalRepository.getCollectionPlaylists(userId, cursor)
                        response.playlists to response.pagination
                    }
                },
                toUiModel = { res ->
                    SongList(
                        id = res.id,
                        title = res.name,
                        description = res.description,
                        coverUrl = getCoverUrl(res.coverArts),
                        dataType = DataType.PLAY_LIST,
//                        duration =
                    )
                }
            )
        }
    )
        .flow
        .flowOn(Dispatchers.IO)
        .cachedIn(viewModelScope)

    val albumPagingData = Pager(
        config = PagingConfig(20),
        pagingSourceFactory = {
            TidalPagingSource(
                fetchData = { cursor ->
                    val userId = tidalCacheManager.getUserInfo()?.id
                    if (userId.isNullOrBlank()) {
                        emptyList<Album>() to Pagination()
                    } else {
                        val response = tidalRepository.getCollectionAlbums(userId, cursor)
                        response.albums to response.pagination
                    }
                },
                toUiModel = { res ->
                    SongList(
                        id = res.id,
                        title = res.title,
                        description = getArtistStr(res.artists),
                        coverUrl = getCoverUrl(res.coverArts),
                        dataType = DataType.ALBUM
                    )
                }
            )
        }
    ).flow
        .flowOn(Dispatchers.IO)
        .cachedIn(viewModelScope)

    fun getPlaylistItemPagingData(listId: String, dataType: Int): Flow<PagingData<SingleSong>> {
        Timber.d("getPlaylistItemPagingData listId: $listId, dataType: $dataType")
        return Pager(
            config = PagingConfig(20),
            pagingSourceFactory = {
                TidalPagingSource(
                    fetchData = { cursor ->
                        if (dataType != DataType.PLAY_LIST.ordinal) {
                            emptyList<TrackDetail>() to Pagination()
                        } else {
                            val response =
                                tidalRepository.getPlaylistTracks(listId, cursor)
                            response.tracks to response.pagination
                        }
                    },
                    toUiModel = { res ->
                        SingleSong(
                            id = res.trackInfo.id,
                            title = res.trackInfo.title,
                            duration = res.trackInfo.duration.toDisplayDuration(),
                            coverUrl = getCoverUrl(res.album.coverArts),
                            description = getArtistStr(res.artists),
                            dataType = DataType.TRACK
                        )
                    }
                )
            }
        ).flow
            .flowOn(Dispatchers.IO)
            .cachedIn(viewModelScope)
    }

    fun getAlbumItemPagingData(listId: String, dataType: Int): Flow<PagingData<SingleSong>> {
        Timber.d("getAlbumItemPagingData listId: $listId, dataType: $dataType")
        return Pager(
            config = PagingConfig(20),
            pagingSourceFactory = {
                TidalPagingSource(
                    fetchData = { cursor ->
                        if (dataType != DataType.ALBUM.ordinal) {
                            emptyList<TrackDetail>() to Pagination()
                        } else {
                            val response =
                                tidalRepository.getAlbumsTracks(listId, cursor)
                            response.tracks to response.pagination
                        }
                    },
                    toUiModel = { res ->
                        SingleSong(
                            id = res.trackInfo.id,
                            title = res.trackInfo.title,
                            duration = res.trackInfo.duration.toDisplayDuration(),
                            coverUrl = getCoverUrl(res.album.coverArts),
                            description = getArtistStr(res.artists),
                            dataType = DataType.TRACK
                        )
                    }
                )
            }
        ).flow
            .flowOn(Dispatchers.IO)
            .cachedIn(viewModelScope)
    }

    private fun getArtistStr(artists: List<Artist>): String {
        return if (artists.isEmpty()) {
            "Unknown artist"
        } else {
            val sb = StringBuilder()
            for (artist in artists) {
                sb.append(artist.name).append(", ")
            }
            sb.deleteRange(sb.length - 2, sb.length).toString()
        }
    }

    private fun getCoverUrl(coverArts: List<CoverArt>): String? {
        return if (coverArts.isNotEmpty()) coverArts[0].url else null
    }

    fun setCurrentListId(listId: String) {
        controllerManager.setCurrentListId(listId)
    }

    fun setCurrentSongList(listId: String, songList: List<SingleSong>) {
        controllerManager.setCurrentSongList(listId, songList)
    }

    fun loadAndPlaySong(index: Int, song: SingleSong) {
        controllerManager.loadAndPlaySong(index, song)
    }

    fun playSong() {
        controllerManager.playSong()
    }

    fun pauseSong() {
        controllerManager.pauseSong()
    }

    fun nextSong() {
        controllerManager.nextSong()
    }

    fun beforeSong() {
        controllerManager.beforeSong()
    }

    fun setControllerShow(show: Boolean) {
        controllerManager.setControllerShow(show)
    }

    fun setDragProgress(dragProgress: Float?) {
        controllerManager.setDragProgress(dragProgress)
    }
}