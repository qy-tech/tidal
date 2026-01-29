package com.qytech.tidalplayer.ui.listpage

import android.content.Context
import com.qytech.tidal.TidalService
import com.qytech.tidalplayer.ui.listpage.model.ControllerUiState
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.utils.ToastUtils
import com.tidal.sdk.player.Player
import com.tidal.sdk.player.common.model.MediaProduct
import com.tidal.sdk.player.common.model.ProductType
import com.tidal.sdk.player.playbackengine.model.Event
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ControllerManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    val player: Player? = TidalService.getPlayerInstance(context)
    private val _currentArtistId = MutableStateFlow("")
    val currentArtistId = _currentArtistId.asStateFlow()
    private val _currentListId = MutableStateFlow("")
    val currentListId = _currentListId.asStateFlow()
    private val _currentSongId = MutableStateFlow("")
    val currentSongId = _currentSongId.asStateFlow()
    private var currentSongList = arrayListOf<SingleSong>()
    private var currentSongIdSet = hashSetOf<String>()

    private val _controllerUiState = MutableStateFlow(ControllerUiState())
    val controllerUiState = _controllerUiState.asStateFlow()

    private var eventCollectionJob: Job? = null
    private var itemPollingJob: Job? = null

    init {
        eventCollectionJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            player?.playbackEngine?.events?.collect { value ->
                when (value) {
                    is Event.MediaProductTransition -> {
//                        // 调用 skipToNext才会经过这里
                        val currentSong = _controllerUiState.value.nextSong
                        val currentIndex = _controllerUiState.value.currentIndex + 1
                        val currentProduct = _controllerUiState.value.nextProduct
                        if (value.mediaProduct.referenceId != _controllerUiState.value.currentProduct?.referenceId) {
                            if (currentSong != null && currentProduct != null) {
                                updateControllerSong(currentIndex, currentSong, currentProduct, false)
                            }
                        }
                    }

                    is Event.MediaProductEnded -> {
//                state.update { it.copy(currentProduct = null) }

                    }

                    is Event.Release -> {
                        Timber.e("vent.Release ")
                    }

                    is Event.Error -> {
                        Timber.e("Event.Error ${value.message}")
                    }

                    is Event.StreamingPrivilegesRevoked -> {
                        withContext(Dispatchers.Main) {
                            ToastUtils.show("当前账号在别处正在播放，本处已暂停")
                            Timber.e("StreamingPrivilegesRevoked: ${value.privilegedClientDisplayName}")
                        }
                    }

                    is Event.DjSessionUpdate -> {

                    }

                    else -> {}
                }
            }
        }

        itemPollingJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            while (true) {
                _controllerUiState.update { oldState ->
                    val totalProgress = player?.playbackEngine?.playbackContext?.duration ?: 0f
                    oldState.copy(
                        playbackState = player?.playbackEngine?.playbackState,
                        currentProgress = oldState.dragProgress
                            ?: player?.playbackEngine?.assetPosition ?: 0f,
                        totalProgress = if (totalProgress >= 0f) totalProgress else 0f
                    )
                }
                delay(200)
            }
        }


    }

    fun clearIds() {
        _currentListId.update { "" }
        _currentSongId.update { "" }
        _currentArtistId.update { "" }
    }

    fun setCurrentListId(listId: String) {
        if (_currentListId.value != listId) {
            _currentListId.update { listId }
            currentSongIdSet = hashSetOf()
            currentSongList = arrayListOf()
        }
    }

    fun setCurrentSongList(listId: String, songList: List<SingleSong>) {
        if (_currentListId.value == listId) {
            songList.forEach {
                if (currentSongIdSet.add(it.id)) {
                    currentSongList.add(it)
                }
            }
        }
    }

    private fun applyPlayer(block: Player.() -> Unit) {
        player?.apply {
            block.invoke(this)
        }
    }

    fun loadAndPlaySong(index: Int, song: SingleSong) {
        val currentProduct = createMediaProduct(song.id)

        applyPlayer {
            playbackEngine.load(currentProduct)
            playbackEngine.play()
        }

        updateControllerSong(index, song, currentProduct)
    }

    private fun updateControllerSong(
        currentIndex: Int,
        currentSong: SingleSong,
        currentProduct: MediaProduct,
        isInit: Boolean = true
    ) {
        _currentSongId.update { currentSong.id }
        Timber.d("updateControllerSong currentSongListId: ${currentListId.value}")

        val currentSongList = currentSongList.toList()
        Timber.d("updateControllerSong currentSongList: ${currentSongList[currentSongList.size - 1]}")

        val nextIndex = currentIndex + 1
        val beforeIndex = currentIndex - 1

        Timber.d("updateControllerSong nextIndex: $nextIndex, beforeIndex: $beforeIndex")

        var nextSong: SingleSong? = null
        var beforeSong: SingleSong? = null

        if (isInit) {
            val indexOf = currentSongList.indexOf(currentSong)
            if (indexOf + 1 in 0..currentSongList.lastIndex) {
                nextSong = currentSongList[indexOf + 1]
            }
            if (indexOf - 1 in 0..currentSongList.lastIndex) {
                beforeSong = currentSongList[indexOf - 1]
            }
        } else {
            if (nextIndex in 0..currentSongList.lastIndex) {
                nextSong = currentSongList[nextIndex]
            }
            if (beforeIndex in 0..currentSongList.lastIndex) {
                beforeSong = currentSongList[beforeIndex]
            }
        }

        var nextProduct: MediaProduct? = null
        var beforeProduct: MediaProduct? = null


        if (nextSong != null) {
            nextProduct = createMediaProduct(nextSong.id)
            applyPlayer {
                playbackEngine.setNext(nextProduct)
            }
        }

        if (beforeSong != null) {
            beforeProduct = createMediaProduct(beforeSong.id)
        }

        _controllerUiState.update {
            it.copy(
                currentSong = currentSong,
                currentIndex = currentIndex,
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
            Timber.d("pauseSong: asd")
            playbackEngine.pause()
        }
    }

    fun nextSong() {
        val nextSong = _controllerUiState.value.nextSong
        if (nextSong != null) {
            val nextIndex = _controllerUiState.value.currentIndex + 1
            val nextProduct = createMediaProduct(nextSong.id)
            applyPlayer {
                playbackEngine.load(nextProduct)
                playSong()
            }
            updateControllerSong(nextIndex, nextSong, nextProduct, false)
        }
    }

    fun beforeSong() {
        val beforeSong = _controllerUiState.value.beforeSong
        if (beforeSong != null) {
            val beforeIndex = _controllerUiState.value.currentIndex - 1
            val beforeProduct = createMediaProduct(beforeSong.id)
            applyPlayer {
                playbackEngine.load(beforeProduct)
                playSong()
            }
            updateControllerSong(beforeIndex, beforeSong, beforeProduct, false)
        }
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

    fun createMediaProduct(
        mediaProductId: String,
        productType: ProductType = ProductType.TRACK,
        referenceId: String = UUID.randomUUID().toString()
    ) =
        MediaProduct(productType, mediaProductId, referenceId = referenceId)
}