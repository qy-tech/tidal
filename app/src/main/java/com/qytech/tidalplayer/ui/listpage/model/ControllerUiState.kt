package com.qytech.tidalplayer.ui.listpage.model

import com.tidal.sdk.player.Player
import com.tidal.sdk.player.common.model.MediaProduct
import com.tidal.sdk.player.playbackengine.model.Event
import com.tidal.sdk.player.playbackengine.model.PlaybackState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

data class ControllerUiState(
    val singleSong: SingleSong = SingleSong(),
    val beforeSong: SingleSong? = null,
    val nextSong: SingleSong? = null,
    val showController: Boolean = false,
    val player: Player? = null,
    val eventCollectionJob: Job? = null,
    val itemPositionPollingJob: Job? = null,
    val currentProduct: MediaProduct? = null,
    val beforeProduct: MediaProduct? = null,
    val nextProduct: MediaProduct? = null,
    val playbackState: PlaybackState? = null,
    val dragProgress: Float? = null,
    val currentProgress: Float = 0f,
    val totalProgress: Float = 0f
) {
}

class PlaybackEngineEventCollector(private val state: MutableStateFlow<ControllerUiState>) :
    FlowCollector<Event> {

    override suspend fun emit(value: Event) {
        when (value) {
            is Event.MediaProductTransition -> {
                // 调用 skipToNext才会经过这里
            }

            is Event.MediaProductEnded -> {
//                state.update { it.copy(currentProduct = null) }
                state.value.currentProduct?.apply {
                    state.value.player?.playbackEngine?.load(this)
                }
            }

            is Event.Release -> {

            }

            is Event.Error -> {

            }

            is Event.StreamingPrivilegesRevoked,
            is Event.DjSessionUpdate -> {

            }

            else -> {}
        }
    }
}