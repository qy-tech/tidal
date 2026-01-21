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
import com.qytech.tidal.data.ArtistDetail
import com.qytech.tidal.data.CoverArt
import com.qytech.tidal.data.Playlist
import com.qytech.tidal.data.TrackDetail
import com.qytech.tidal.data.UserInfo
import com.qytech.tidal.data.paging.Pagination
import com.qytech.tidal.data.toDisplayDuration
import com.qytech.tidal.login.TidalLogin
import com.qytech.tidal.repository.TidalRepository
import com.qytech.tidalplayer.ui.TidalPagingSource
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.ItemType
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.ui.listpage.model.SongList
import com.qytech.tidalplayer.utils.ToastUtils
import com.tidal.sdk.player.events.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListPageViewModel @Inject constructor(
    private val controllerManager: ControllerManager,
    private val tidalLogin: TidalLogin,
    private val tidalCacheManager: TidalCacheManager,
    private val tidalRepository: TidalRepository
) : ViewModel() {

    val controllerUiState = controllerManager.controllerUiState
    val currentArtistId = controllerManager.currentArtistId
    val currentListId = controllerManager.currentListId
    val currentSongId = controllerManager.currentSongId
    val userInfo = MutableStateFlow<UserInfo?>(null)
    val checkAuth = MutableStateFlow(true)
    private val _playlistRefresh = MutableStateFlow(0L)
    private val _albumsRefresh = MutableStateFlow(0L)
    private val _tracksRefresh = MutableStateFlow(0L)
    private val _artistsRefresh = MutableStateFlow(0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionPlaylistPagingData = _playlistRefresh.flatMapLatest {
        Pager(
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
        ).flow
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionAlbumPagingData = _albumsRefresh.flatMapLatest {
        Pager(
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
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionTracksPagingData = _tracksRefresh.flatMapLatest {
        Pager(
            config = PagingConfig(20),
            pagingSourceFactory = {
                TidalPagingSource(
                    fetchData = { cursor ->
                        val userId = tidalCacheManager.getUserInfo()?.id
                        if (userId.isNullOrBlank()) {
                            emptyList<TrackDetail>() to Pagination()
                        } else {
                            val response = tidalRepository.getCollectionTracks(userId, cursor)
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
                            version = res.trackInfo.version,
                            dataType = DataType.TRACK
                        )
                    }
                )
            }
        ).flow
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionArtistsPagingData = _artistsRefresh.flatMapLatest {
        Pager(
            config = PagingConfig(20),
            pagingSourceFactory = {
                TidalPagingSource(
                    fetchData = { cursor ->
                        val userId = tidalCacheManager.getUserInfo()?.id
                        if (userId.isNullOrBlank()) {
                            emptyList<ArtistDetail>() to Pagination()
                        } else {
                            val response = tidalRepository.getCollectionArtists(userId, cursor)
                            response.artists to response.pagination
                        }
                    },
                    toUiModel = { res ->
                        SongList(
                            id = res.artistInfo.id,
                            title = res.artistInfo.name,
                            description = null,
                            coverUrl = getCoverUrl(res.profileArts),
                            dataType = DataType.ARTIST
                        )
                    }
                )
            }
        ).flow
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)

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
                            version = res.trackInfo.version,
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
                            version = res.trackInfo.version,
                            dataType = DataType.TRACK
                        )
                    }
                )
            }
        ).flow
            .flowOn(Dispatchers.IO)
            .cachedIn(viewModelScope)
    }

    fun checkAuth() {
        viewModelScope.launch {
            tidalLogin.loginUiState.collect { value ->
                val cacheLoggedIn = !tidalCacheManager.getUserInfo()?.id.isNullOrBlank()
                if (cacheLoggedIn) {
                    try {
                        tidalRepository.getUser().collect { user ->
                            userInfo.update { user }
                        }
                        checkAuth.update { true }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (e.message?.contains("HTTP 401") == true) {
                            checkAuth.update { false }
                        } else {
                            ToastUtils.show(e.message ?: "")
                        }
                    }
                }
            }
        }
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

    fun clearCacheUserInfo() {
        tidalLogin.logout()
    }

    fun checkFavouriteTrack(trackId: String): Boolean {
        return tidalRepository.checkTrackIsCollected(trackId)
    }

    fun refreshAllCollection(type: DataType? = null) {
        when(type) {
            DataType.PLAY_LIST -> _playlistRefresh.update { it + 1 }
            DataType.ALBUM -> _albumsRefresh.update { it + 1 }
            DataType.TRACK -> _tracksRefresh.update { it + 1 }
            DataType.ARTIST -> _artistsRefresh.update { it + 1 }
            else -> {
                _playlistRefresh.update { it + 1 }
                _albumsRefresh.update { it + 1 }
                _tracksRefresh.update { it + 1 }
                _artistsRefresh.update { it + 1 }
            }
        }
    }
}