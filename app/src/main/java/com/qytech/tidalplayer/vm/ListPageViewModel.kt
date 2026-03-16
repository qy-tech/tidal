package com.qytech.tidalplayer.vm

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import com.qytech.tidal.base.getOrNull
import com.qytech.tidal.base.onError
import com.qytech.tidal.base.onSuccess
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
import com.qytech.tidalplayer.base.BaseViewModel
import com.qytech.tidalplayer.ui.listpage.ControllerManager
import com.qytech.tidalplayer.ui.listpage.GlobalManager
import com.qytech.tidalplayer.ui.listpage.model.ChannelType
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.PagerFlow
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.ui.listpage.model.SongList
import com.qytech.tidalplayer.utils.ToastUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListPageViewModel @Inject constructor(
    private val controllerManager: ControllerManager,
    private val tidalLogin: TidalLogin,
    private val tidalCacheManager: TidalCacheManager,
    private val tidalRepository: TidalRepository,
    private val globalManager: GlobalManager
) : BaseViewModel() {

    val controllerUiState = controllerManager.controllerUiState
    val currentArtistId = controllerManager.currentArtistId
    val currentListId = controllerManager.currentListId
    val currentSongId = controllerManager.currentSongId
    val collectionTrackIds = tidalRepository.collectionTrackIds
    val collectionArtistIds = tidalRepository.collectionArtistIds
    val collectionAlbumIds = tidalRepository.collectionAlbumIds
    val collectionPlaylistIds = tidalRepository.collectionPlaylistIds
    private val _operationChannel = Channel<ChannelType>()
    val operationChannel = _operationChannel.receiveAsFlow()
    private val myMixTracksHashMap = hashMapOf<String, Flow<PagingData<SingleSong>>>()
    private val discoveryMixTracksHashMap = hashMapOf<String, Flow<PagingData<SingleSong>>>()
    private val newArrivalHashMap = hashMapOf<String, Flow<PagingData<SingleSong>>>()
    private val _deletePlaylistIds = MutableStateFlow(emptySet<String>())
    val deletePlaylistIds = _deletePlaylistIds.asStateFlow()
    private val _createPlaylist = MutableStateFlow(emptyList<SongList>())
    val createPlaylist = _createPlaylist.asStateFlow()
    private val _searchHistory = MutableStateFlow(tidalCacheManager.getSearchHistory())
    val searchHistory = _searchHistory.asStateFlow()

    val playlistRefresh = globalManager.playlistRefresh
    val myPlaylistRefresh = globalManager.myPlaylistRefresh
    val albumsRefresh = globalManager.albumsRefresh
    val tracksRefresh = globalManager.tracksRefresh
    val artistsRefresh = globalManager.artistsRefresh
    val myMixesRefresh = globalManager.myMixesRefresh
    val discoveryMixesRefresh = globalManager.discoveryMixesRefresh
    val newArrivalRefresh = globalManager.newArrivalRefresh

    val panelState = globalManager.panelState

    init {
        controllerManager.initController()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionPlaylistPagingData = playlistRefresh.flatMapLatest {
        PagerFlow.build(
            fetchData = { cursor ->
                val empty = emptyList<Playlist>() to Pagination()

                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) return@build empty

                return@build tidalRepository
                    .getCollectionPlaylists(userId, cursor)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: empty
            },
            toUiModel = { res ->
                SongList(
                    id = res.id,
                    title = res.name,
                    description = res.description ?: "",
                    coverUrl = getCoverUrl(res.coverArts),
                    dataType = DataType.PLAY_LIST,
//                        duration =
                )
            }
        )
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val myPlaylistPagingData = myPlaylistRefresh.flatMapLatest {
        PagerFlow.build(
            fetchData = { cursor ->
                val empty = emptyList<Playlist>() to Pagination()

                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) return@build empty

                return@build tidalRepository
                    .getUserPlaylists(userId, cursor)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: empty
            },
            toUiModel = { res ->
                SongList(
                    id = res.id,
                    title = res.name,
                    description = res.description ?: "",
                    coverUrl = getCoverUrl(res.coverArts),
                    dataType = DataType.PLAY_LIST,
//                        duration =
                )
            }
        )
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionAlbumPagingData = albumsRefresh.flatMapLatest {
        PagerFlow.build(
            fetchData = { cursor ->
                val empty = emptyList<Album>() to Pagination()

                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) return@build empty

                return@build tidalRepository
                    .getCollectionAlbums(userId, cursor)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: empty
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
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionTracksPagingData = tracksRefresh.flatMapLatest {
        PagerFlow.build(
            fetchData = { cursor ->
                val empty = emptyList<TrackDetail>() to Pagination()

                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) return@build empty

                return@build tidalRepository
                    .getCollectionTracks(userId, cursor)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: empty
            },
            toUiModel = { res ->
                SingleSong(
                    id = res.trackInfo.id,
                    title = res.trackInfo.title,
                    duration = res.trackInfo.duration.toDisplayDuration(),
                    coverUrl = getCoverUrl(res.album.coverArts),
                    description = getArtistStr(res.artists),
                    artists = res.artists,
                    album = res.album,
                    version = res.trackInfo.version,
                    dataType = DataType.TRACK
                )
            }
        )
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionArtistsPagingData = artistsRefresh.flatMapLatest {
        PagerFlow.build(
            fetchData = { cursor ->
                val empty = emptyList<ArtistDetail>() to Pagination()

                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) return@build empty

                return@build tidalRepository
                    .getCollectionArtists(userId, cursor)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: empty
            },
            toUiModel = { res ->
                SongList(
                    id = res.artistInfo.id,
                    title = res.artistInfo.name,
                    description = "",
                    coverUrl = getCoverUrl(res.profileArts),
                    dataType = DataType.ARTIST
                )
            }
        )
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val myMixesPagingData = myMixesRefresh.flatMapLatest {
        PagerFlow.build(
            fetchData = { cursor ->
                val empty = emptyList<Playlist>() to Pagination()

                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) return@build empty

                return@build tidalRepository
                    .getMyMixes(userId)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: empty
            },
            toUiModel = { res ->
                SongList(
                    id = res.id,
                    title = res.name,
                    description = "",
                    coverUrl = getCoverUrl(res.coverArts),
                    dataType = DataType.MY_MIX
                )
            }
        )
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val discoveryMixesPagingData = discoveryMixesRefresh.flatMapLatest {
        PagerFlow.build(
            fetchData = { cursor ->
                val empty = emptyList<Playlist>() to Pagination()
                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) return@build empty

                return@build tidalRepository
                    .getDiscoveryMixes(userId)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: empty
            },
            toUiModel = { res ->
                SongList(
                    id = res.id,
                    title = res.name,
                    description = "",
                    coverUrl = getCoverUrl(res.coverArts),
                    dataType = DataType.DISCOVERY_MIX
                )
            }
        )
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val newArrivalPagingData = newArrivalRefresh.flatMapLatest {
        PagerFlow.build(
            fetchData = { cursor ->
                val empty = emptyList<Playlist>() to Pagination()

                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) return@build empty

                return@build tidalRepository
                    .getNewArrivalMixes(userId)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: empty
            },
            toUiModel = { res ->
                SongList(
                    id = res.id,
                    title = res.name,
                    description = "",
                    coverUrl = getCoverUrl(res.coverArts),
                    dataType = DataType.NEW_ARRIVAL
                )
            }
        )
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)

    fun searchPlaylist(searchText: String): Flow<PagingData<SongList>> {
        return PagerFlow.build(
            fetchData = { cursor ->
                val empty = emptyList<Playlist>() to Pagination()

                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) return@build empty

                return@build tidalRepository
                    .searchPlaylists(searchText, cursor)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: empty
            },
            toUiModel = { res ->
                SongList(
                    id = res.id,
                    title = res.name,
                    description = res.description ?: "",
                    coverUrl = getCoverUrl(res.coverArts),
                    dataType = DataType.PLAY_LIST,
//                        duration =
                )
            }
        ).flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    fun searchTrack(searchText: String): Flow<PagingData<SingleSong>> {
        return PagerFlow.build(
            fetchData = { cursor ->
                val empty = emptyList<TrackDetail>() to Pagination()

                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) return@build empty

                return@build tidalRepository
                    .searchTracks(searchText, cursor)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: empty
            },
            toUiModel = { res ->
                SingleSong(
                    id = res.trackInfo.id,
                    title = res.trackInfo.title,
                    duration = res.trackInfo.duration.toDisplayDuration(),
                    coverUrl = getCoverUrl(res.album.coverArts),
                    description = getArtistStr(res.artists),
                    artists = res.artists,
                    album = res.album,
                    version = res.trackInfo.version,
                    dataType = DataType.TRACK
                )
            }
        ).flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    fun searchAlbum(searchText: String): Flow<PagingData<SongList>> {
        return PagerFlow.build(
            fetchData = { cursor ->
                val empty = emptyList<Album>() to Pagination()

                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) return@build empty

                return@build tidalRepository
                    .searchAlbums(searchText, cursor)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: empty
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
        ).flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    fun searchArtist(searchText: String): Flow<PagingData<SongList>> {
        return PagerFlow.build(
            fetchData = { cursor ->
                val empty = emptyList<ArtistDetail>() to Pagination()

                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) return@build empty

                return@build tidalRepository
                    .searchArtists(searchText, cursor)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: empty
            },
            toUiModel = { res ->
                SongList(
                    id = res.artistInfo.id,
                    title = res.artistInfo.name,
                    description = "",
                    coverUrl = getCoverUrl(res.profileArts),
                    dataType = DataType.ARTIST
                )
            }
        ).flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    fun addSearchHistory(searchText: String) {
        val newSearchHistory = tidalCacheManager.saveSearchHistory(searchText)
        _searchHistory.value = newSearchHistory
    }

    fun removeSearchHistory(searchText: String) {
        val newSearchHistory = tidalCacheManager.removeSearchHistory(searchText)
        _searchHistory.value = newSearchHistory
    }

    fun clearSearchHistory() {
        tidalCacheManager.clearSearchHistory()
        _searchHistory.value = emptyList<String>()
    }

    fun getMyMixTracksPagingData(listId: String): Flow<PagingData<SingleSong>> {
        if (myMixTracksHashMap.contains(listId)) return myMixTracksHashMap[listId]!!
        else {
            val pagingData = getPlaylistItemPagingData(listId)
            myMixTracksHashMap[listId] = pagingData
            return pagingData
        }
    }

    fun getDiscoveryMixTracksPagingData(listId: String): Flow<PagingData<SingleSong>> {
        if (discoveryMixTracksHashMap.contains(listId)) return discoveryMixTracksHashMap[listId]!!
        else {
            val pagingData = getPlaylistItemPagingData(listId)
            discoveryMixTracksHashMap[listId] = pagingData
            return pagingData
        }
    }

    fun getNewArrivalPagingData(listId: String): Flow<PagingData<SingleSong>> {
        if (newArrivalHashMap.contains(listId)) return newArrivalHashMap[listId]!!
        else {
            val pagingData = getPlaylistItemPagingData(listId)
            newArrivalHashMap[listId] = pagingData
            return pagingData
        }
    }

    fun getPlaylistItemPagingData(listId: String): Flow<PagingData<SingleSong>> {
        Timber.d("getPlaylistItemPagingData listId: $listId")
        return PagerFlow.build(
            fetchData = { cursor ->
                tidalRepository
                    .getPlaylistTracks(listId, cursor)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: Pair(emptyList(), Pagination())
            },
            toUiModel = { res ->
                SingleSong(
                    id = res.trackInfo.id,
                    title = res.trackInfo.title,
                    duration = res.trackInfo.duration.toDisplayDuration(),
                    coverUrl = getCoverUrl(res.album.coverArts),
                    description = getArtistStr(res.artists),
                    artists = res.artists,
                    album = res.album,
                    version = res.trackInfo.version,
                    dataType = DataType.TRACK
                )
            }
        )
            .flowOn(Dispatchers.IO)
            .cachedIn(viewModelScope)
    }

    fun getAlbumItemPagingData(listId: String): Flow<PagingData<SingleSong>> {
        Timber.Forest.d("getAlbumItemPagingData listId: $listId")
        return PagerFlow.build(
            fetchData = { cursor ->
                tidalRepository
                    .getAlbumsTracks(listId, cursor)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: Pair(emptyList(), Pagination())
            },
            toUiModel = { res ->
                SingleSong(
                    id = res.trackInfo.id,
                    title = res.trackInfo.title,
                    duration = res.trackInfo.duration.toDisplayDuration(),
                    coverUrl = getCoverUrl(res.album.coverArts),
                    description = getArtistStr(res.artists),
                    artists = res.artists,
                    album = res.album,
                    version = res.trackInfo.version,
                    dataType = DataType.TRACK
                )
            }
        )
            .flowOn(Dispatchers.IO)
            .cachedIn(viewModelScope)
    }

    fun getArtistAlbumsPagingData(artistId: String): Flow<PagingData<SongList>> {
        Timber.Forest.d("getArtistAlbumsPagingData artistId: $artistId")
        return PagerFlow.build(
            fetchData = { cursor ->
                val empty = emptyList<Album>() to Pagination()

                if (artistId.isEmpty()) return@build empty

                return@build tidalRepository
                    .getArtistAlbums(artistId, cursor)
                    .getOrNull()
                    ?.let { it.data to it.pagination }
                    ?: empty
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

    fun checkAuth() {
        viewModelScope.launch {
            tidalLogin.loginUiState.collect { value ->
                val cacheLoggedIn = !tidalCacheManager.getUserInfo()?.id.isNullOrBlank()
                if (cacheLoggedIn) {
                    try {
                        tidalRepository.getUser().collect { user ->
                            val oldUser = tidalCacheManager.getUserInfo()
                            tidalCacheManager.saveUserInfo(user.copy(token = oldUser?.token))
                        }
                        _operationChannel.send(
                            ChannelType.CheckAuth(
                                result = true
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (e.message?.contains("HTTP 401") == true) {
                            _operationChannel.send(
                                ChannelType.CheckAuth(
                                    result = false
                                )
                            )
                        } else {
                            Timber.Forest.w(e.message ?: "异常错误")
//                            ToastUtils.show(e.message ?: "异常错误")
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

    fun clearIds() {
        controllerManager.clearIds()
    }

    fun setCurrentListId(listId: String, isClearBeforeData: Boolean = true) {
        Timber.Forest.d("设置歌单id：$listId")
        controllerManager.setCurrentListId(listId, isClearBeforeData)
    }

    fun setCurrentSongList(listId: String, songList: List<SingleSong>) {
        controllerManager.setCurrentSongList(listId, songList)
    }

    fun loadAndPlaySong(index: Int, song: SingleSong) {
        Timber.Forest.d("songId: ${song}")
        controllerManager.loadAndPlay(index, song)
    }

    fun playToNext(index: Int, nextSong: SingleSong) {
        controllerManager.playToNext(index, nextSong)
    }

    fun playSong() {
        controllerManager.play()
    }

    fun pauseSong() {
        controllerManager.pause()
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

    suspend fun logout() {
        // 清除音乐控制器数据
        controllerManager.clearAll()
        tidalLogin.logout()
    }

    fun refreshData(type: DataType? = null) {
        globalManager.refreshData(type)
    }

    fun addTrackToCollection(trackId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            if (userId.isNullOrBlank()) {
                showToast("添加收藏失败，用户Id为空")
                return@launch
            }

            tidalRepository.addTracksToCollection(userId, listOf(trackId))
                .onSuccess {
                    refreshData(DataType.TRACK)
                }
                .onError {
                    showToast("添加收藏失败")
                }
        }
    }

    fun removeTrackToCollection(trackId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            if (userId.isNullOrBlank()) {
                showToast("取消收藏失败，用户Id为空")
                return@launch
            }

            tidalRepository.removeTracksFromCollection(userId, listOf(trackId))
                .onSuccess {
                    refreshData(DataType.TRACK)
                }
                .onError {
                    showToast("取消收藏失败")
                }
        }
    }

    fun addPlaylistToCollection(listId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            if (userId.isNullOrBlank()) {
                showToast("添加收藏失败，用户Id为空")
                return@launch
            }

            tidalRepository.addPlaylistsToCollection(userId, listOf(listId))
                .onSuccess {
                    refreshData(DataType.PLAY_LIST)
                    showToast("收藏成功")
                }
                .onError {
                    showToast("添加收藏失败")
                }
        }
    }

    fun removePlaylistToCollection(listId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            if (userId.isNullOrBlank()) {
                showToast("取消收藏失败，用户Id为空")
                return@launch
            }

            tidalRepository.removePlaylistsFromCollection(userId, listOf(listId))
                .onSuccess {
                    refreshData(DataType.PLAY_LIST)
                    showToast("取消收藏成功")
                }
                .onError {
                    showToast("取消收藏失败")
                }
        }
    }

    fun addAlbumToCollection(listId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            if (userId.isNullOrBlank()) {
                showToast("添加收藏失败，用户Id为空")
                return@launch
            }

            tidalRepository.addAlbumsToCollection(userId, listOf(listId))
                .onSuccess {
                    refreshData(DataType.ALBUM)
                    showToast("收藏成功")
                }
                .onError {
                    showToast("添加收藏失败")
                }
        }
    }

    fun removeAlbumToCollection(listId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            if (userId.isNullOrBlank()) {
                showToast("取消收藏失败，用户Id为空")
                return@launch
            }

            tidalRepository.removeAlbumsFromCollection(userId, listOf(listId))
                .onSuccess {
                    refreshData(DataType.ALBUM)
                    showToast("取消收藏成功")
                }
                .onError {
                    showToast("取消收藏失败")
                }
        }
    }

    fun addArtistToCollection(artistId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            if (userId.isNullOrBlank()) {
                showToast("添加收藏失败，用户Id为空")
                return@launch
            }

            tidalRepository.addArtistsToCollection(userId, listOf(artistId))
                .onSuccess {
                    refreshData(DataType.ARTIST)
                    showToast("收藏成功")
                }
                .onError {
                    showToast("添加收藏失败")
                }
        }
    }

    fun removeArtistToCollection(artistId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            if (userId.isNullOrBlank()) {
                showToast("取消收藏失败，用户Id为空")
                return@launch
            }

            tidalRepository.removeArtistsFromCollection(userId, listOf(artistId))
                .onSuccess {
                    refreshData(DataType.ARTIST)
                    showToast("取消收藏成功")
                }
                .onError {
                    showToast("取消收藏失败")
                }
        }
    }

    fun getUserInfo(): UserInfo? {
        return tidalCacheManager.getUserInfo()
    }

    fun addTracksToPlaylist(playlistId: String, trackId: String) {
        launchWithLoading {
            tidalRepository.addTracksToPlaylist(playlistId, listOf(trackId))
                .onSuccess {
                    showToast("添加成功")
                }
                .onError {
                    showToast("添加失败")
                }
        }
    }

    fun createPlaylist(name: String, description: String) {
        launchWithLoading {
            tidalRepository.createPlaylist(name, description)
                .onSuccess { response ->
                    if (response.id.isEmpty()) {
                        showToast("创建失败，歌单id为空")
                        return@onSuccess
                    }

                    val item = SongList(
                        id = response.id,
                        title = response.name,
                        description = response.description,
                        coverUrl = null,
                        dataType = DataType.PLAY_LIST
                    )
                    _operationChannel.send(
                        ChannelType.CreatePlaylist(
                            result = true,
                            data = item
                        )
                    )
                    _createPlaylist.addItem(item)
                    showToast("创建成功")
                    refreshData(DataType.PLAY_LIST)
                }
                .onError {
                    showToast("创建失败")
                }
        }
    }

    fun deletePlaylist(listId: String) {
        launchWithLoading {
            tidalRepository.deletePlaylist(listId)
                .onSuccess {
                    _deletePlaylistIds.addItemId(listId)
                    _operationChannel.send(
                        ChannelType.DeletePlaylist(
                            result = true
                        )
                    )
                    showToast("删除成功")
                    refreshData(DataType.PLAY_LIST)
                }
                .onError {
                    showToast("删除失败")
                }
        }
    }

    fun openPanel(
        singleSong: SingleSong? = null,
        dataType: DataType? = null,
        songList: SongList? = null,
        lazyList: LazyPagingItems<SingleSong>? = null
    ) {
        globalManager.openPanel(singleSong, dataType, songList, lazyList)
    }

//    fun open

    fun closePanel() {
        globalManager.closePanel()
    }

    private fun MutableStateFlow<Set<String>>.addItemId(itemId: String) {
        update { currentSet ->
            currentSet + itemId
        }
    }

    private fun MutableStateFlow<List<SongList>>.addItem(item: SongList) {
        update { currentList ->
            currentList + item
        }
    }
}