package com.qytech.tidalplayer.ui.listpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
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
) : ViewModel() {

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
    private val _showLoading = MutableStateFlow(false)
    val showLoading = _showLoading.asStateFlow()
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
                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) {
                    emptyList<Playlist>() to Pagination()
                } else {
                    val response = tidalRepository.getUserPlaylists(userId, cursor)
                    response.playlists to response.pagination
                }
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
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionTracksPagingData = tracksRefresh.flatMapLatest {
        PagerFlow.build(
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
                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) {
                    emptyList<Playlist>() to Pagination()
                } else {
                    val response = tidalRepository.getMyMixes(userId)
                    response to Pagination()
                }
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
                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) {
                    emptyList<Playlist>() to Pagination()
                } else {
                    val response = tidalRepository.getDiscoveryMixes(userId)
                    response to Pagination()
                }
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
                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) {
                    emptyList<Playlist>() to Pagination()
                } else {
                    val response = tidalRepository.getNewArrivalMixes(userId)
                    response to Pagination()
                }
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
                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) {
                    emptyList<Playlist>() to Pagination()
                } else {
                    val response = tidalRepository.searchPlaylists(searchText, cursor)
                    response.playlists to response.pagination
                }
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
                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) {
                    emptyList<TrackDetail>() to Pagination()
                } else {
                    val response = tidalRepository.searchTracks(searchText, cursor)
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
                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) {
                    emptyList<Album>() to Pagination()
                } else {
                    val response = tidalRepository.searchAlbums(searchText, cursor)
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
        ).flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    fun searchArtist(searchText: String): Flow<PagingData<SongList>> {
        return PagerFlow.build(
            fetchData = { cursor ->
                val userId = tidalCacheManager.getUserInfo()?.id
                if (userId.isNullOrBlank()) {
                    emptyList<ArtistDetail>() to Pagination()
                } else {
                    val response = tidalRepository.searchArtists(searchText, cursor)
                    response.artists to response.pagination
                }
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

    fun getPagingDataHashMap(dataType: DataType): HashMap<String, Flow<PagingData<SingleSong>>> {
        return when (dataType) {
            DataType.NEW_ARRIVAL -> newArrivalHashMap
            else -> hashMapOf()
        }
    }

    fun getPlaylistItemPagingData(listId: String): Flow<PagingData<SingleSong>> {
        Timber.d("getPlaylistItemPagingData listId: $listId")
        return PagerFlow.build(
            fetchData = { cursor ->
                val response =
                    tidalRepository.getPlaylistTracks(listId, cursor)
                response.tracks to response.pagination
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
        Timber.d("getAlbumItemPagingData listId: $listId")
        return PagerFlow.build(
            fetchData = { cursor ->
                val response =
                    tidalRepository.getAlbumsTracks(listId, cursor)
                response.tracks to response.pagination
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
        Timber.d("getArtistAlbumsPagingData artistId: $artistId")
        return PagerFlow.build(
            fetchData = { cursor ->
                if (artistId.isEmpty()) {
                    emptyList<Album>() to Pagination()
                } else {
                    val response = tidalRepository.getArtistAlbums(artistId, cursor)
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
                            Timber.w(e.message ?: "异常错误")
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
        Timber.d("设置歌单id：$listId")
        controllerManager.setCurrentListId(listId, isClearBeforeData)
    }

    fun setCurrentSongList(listId: String, songList: List<SingleSong>) {
        controllerManager.setCurrentSongList(listId, songList)
    }

    fun loadAndPlaySong(index: Int, song: SingleSong) {
        Timber.d("songId: ${song}")
        controllerManager.loadAndPlaySong(index, song)
    }

    fun playToNext(index: Int, nextSong: SingleSong) {
        controllerManager.playToNext(index, nextSong)
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
            try {
                if (!userId.isNullOrBlank()) {
                    tidalRepository.addTracksToCollection(userId, listOf(trackId))
                    refreshData(DataType.TRACK)
                } else {
                    ToastUtils.show("添加收藏失败，用户Id为空")
                }
            } catch (e: Exception) {
                ToastUtils.show("添加收藏失败")
            }
        }
    }

    fun removeTrackToCollection(trackId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!userId.isNullOrBlank()) {
                    tidalRepository.removeTracksFromCollection(userId, listOf(trackId))
                    refreshData(DataType.TRACK)
                } else {
                    ToastUtils.show("取消收藏失败，用户Id为空")
                }
            } catch (e: Exception) {
                ToastUtils.show("取消收藏失败")
            }
        }
    }

    fun addPlaylistToCollection(listId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!userId.isNullOrBlank()) {
                    tidalRepository.addPlaylistsToCollection(userId, listOf(listId))
                    refreshData(DataType.PLAY_LIST)
                } else {
                    ToastUtils.show("添加收藏失败，用户Id为空")
                }
            } catch (e: Exception) {
                ToastUtils.show("添加收藏失败")
            }
        }
    }

    fun removePlaylistToCollection(listId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!userId.isNullOrBlank()) {
                    tidalRepository.removePlaylistsFromCollection(userId, listOf(listId))
                    refreshData(DataType.PLAY_LIST)
                } else {
                    ToastUtils.show("取消收藏失败，用户Id为空")
                }
            } catch (e: Exception) {
                ToastUtils.show("取消收藏失败")
            }
        }
    }

    fun addAlbumToCollection(listId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!userId.isNullOrBlank()) {
                    tidalRepository.addAlbumsToCollection(userId, listOf(listId))
                    refreshData(DataType.ALBUM)
                } else {
                    ToastUtils.show("添加收藏失败，用户Id为空")
                }
            } catch (e: Exception) {
                ToastUtils.show("添加收藏失败")
            }
        }
    }

    fun removeAlbumToCollection(listId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!userId.isNullOrBlank()) {
                    tidalRepository.removeAlbumsFromCollection(userId, listOf(listId))
                    refreshData(DataType.ALBUM)
                } else {
                    ToastUtils.show("取消收藏失败，用户Id为空")
                }
            } catch (e: Exception) {
                ToastUtils.show("取消收藏失败")
            }
        }
    }

    fun addArtistToCollection(artistId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!userId.isNullOrBlank()) {
                    tidalRepository.addArtistsToCollection(userId, listOf(artistId))
                    refreshData(DataType.ARTIST)
                } else {
                    ToastUtils.show("添加收藏失败，用户Id为空")
                }
            } catch (e: Exception) {
                ToastUtils.show("添加收藏失败")
            }
        }
    }

    fun removeArtistToCollection(artistId: String) {
        val userId = tidalCacheManager.getUserInfo()?.id
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!userId.isNullOrBlank()) {
                    tidalRepository.removeArtistsFromCollection(userId, listOf(artistId))
                    refreshData(DataType.ARTIST)
                } else {
                    ToastUtils.show("取消收藏失败，用户Id为空")
                }
            } catch (e: Exception) {
                ToastUtils.show("取消收藏失败")
            }
        }
    }

    fun getUserInfo(): UserInfo? {
        return tidalCacheManager.getUserInfo()
    }

    fun addTracksToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.update { true }
            try {
                tidalRepository.addTracksToPlaylist(playlistId, listOf(trackId))

                ToastUtils.show("添加成功")
            } catch (e: Exception) {
                ToastUtils.show("添加失败")
            }
            _showLoading.update { false }
        }
    }

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.update { true }
            try {
                val response = tidalRepository.createPlaylist(
                    name = name,
                    description = description
                )

                if (response.id.isEmpty()) {
                    ToastUtils.show("创建失败，歌单id为空")
                } else {
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
                    ToastUtils.show("创建成功")
                    refreshData(DataType.PLAY_LIST)
                }
            } catch (e: Exception) {
                ToastUtils.show("创建失败")
            }
            _showLoading.update { false }
        }
    }

    fun deletePlaylist(listId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.update { true }
            try {
                tidalRepository.deletePlaylist(listId)
                _deletePlaylistIds.addItemId(listId)
                _operationChannel.send(
                    ChannelType.DeletePlaylist(
                        result = true
                    )
                )
                ToastUtils.show("删除成功")
                refreshData(DataType.PLAY_LIST)
            } catch (e: Exception) {
                ToastUtils.show("删除失败")
            }
            _showLoading.update { false }
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