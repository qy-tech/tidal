package com.qytech.tidalplayer.ui.listpage.model

import com.tidal.sdk.player.Player
import com.tidal.sdk.player.common.model.MediaProduct
import com.tidal.sdk.player.playbackengine.model.Event
import com.tidal.sdk.player.playbackengine.model.PlaybackState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

data class ControllerUiState(
    val currentSong: SingleSong? = null,
    val currentIndex: Int = -1,
    val beforeSong: SingleSong? = null,
    val nextSong: SingleSong? = null,
    val showController: Boolean = false,
    val currentProduct: MediaProduct? = null,
    val beforeProduct: MediaProduct? = null,
    val nextProduct: MediaProduct? = null,
    val playbackState: PlaybackState? = null,
    val dragProgress: Float? = null,
    val currentProgress: Float = 0f,
    val totalProgress: Float = 0f
) {
}