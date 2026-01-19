package com.qytech.tidalplayer.ui.listpage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.qytech.tidal.TidalService
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
import com.qytech.tidalplayer.ui.listpage.model.ControllerUiState
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.ItemInfo
import com.qytech.tidalplayer.ui.listpage.model.PlaybackEngineEventCollector
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.ui.listpage.model.SongList
import com.tidal.sdk.player.Player
import com.tidal.sdk.player.common.model.MediaProduct
import com.tidal.sdk.player.common.model.ProductType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.StringBuilder
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ListPageViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val tidalCacheManager: TidalCacheManager,
    private val tidalRepository: TidalRepository
) : ViewModel() {

    private val player: Player? = TidalService.getPlayerInstance(context)

    private val _controllerUiState = MutableStateFlow(ControllerUiState())
    val controllerUiState = _controllerUiState.asStateFlow()

    init {
        _controllerUiState.update {
            it.copy(
                player = player,
                eventCollectionJob = viewModelScope.launch {
                    player?.playbackEngine?.events?.collect(
                        PlaybackEngineEventCollector(_controllerUiState)
                    )
                },
                itemPositionPollingJob = viewModelScope.launch {
                    while (true) {
                        _controllerUiState.update { oldState ->
                            oldState.copy(
                                playbackState = oldState.player?.playbackEngine?.playbackState,
                                currentProgress = oldState.dragProgress
                                    ?: oldState.player?.playbackEngine?.assetPosition ?: 0f,
                                totalProgress = oldState.player?.playbackEngine?.playbackContext?.duration
                                    ?: 0f
                            )
                        }
                        delay(200)
                    }
                }
            )
        }
    }

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

    private fun applyPlayer(block: Player.() -> Unit) {
        _controllerUiState.value.player?.apply {
            block.invoke(this)
        }
    }

    fun loadSong(song: SingleSong, beforeSong: SingleSong? = null, nextSong: SingleSong? = null) {
        val currentProduct = createMediaProduct(song.id)
        val beforeProduct = beforeSong?.let { createMediaProduct(beforeSong.id) }
        val nextProduct = nextSong?.let { createMediaProduct(nextSong.id) }
        applyPlayer {
            playbackEngine.load(currentProduct)
        }
        _controllerUiState.update {
            it.copy(
                singleSong = song,
                beforeSong = beforeSong,
                nextSong = nextSong,
                currentProduct = currentProduct,
                beforeProduct = beforeProduct,
                nextProduct = nextProduct
            )
        }
    }

    fun playSong() {
        applyPlayer {
            playbackEngine.play()
        }
    }

    fun pauseSong() {
        applyPlayer {
            playbackEngine.pause()
        }
    }

    fun nextSong() {
        applyPlayer {
//            loadSong()
        }
    }

    fun beforeSong() {

    }

    fun setControllerShow(show: Boolean) {
        _controllerUiState.update { it.copy(showController = show) }
    }

    fun setDragProgress(dragProgress: Float?) {
        _controllerUiState.update { it.copy(dragProgress = dragProgress) }
        if (dragProgress != null) {
            applyPlayer {
                playbackEngine.seek(dragProgress * 1000)
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
}

fun createMediaProduct(
    mediaProductId: String,
    productType: ProductType = ProductType.TRACK,
    referenceId: String = UUID.randomUUID().toString()
) =
    MediaProduct(productType, mediaProductId, referenceId = referenceId)