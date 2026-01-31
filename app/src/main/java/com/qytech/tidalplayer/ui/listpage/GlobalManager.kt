package com.qytech.tidalplayer.ui.listpage

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.paging.compose.LazyPagingItems
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.ItemInfo
import com.qytech.tidalplayer.ui.listpage.model.PanelData
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.ui.listpage.model.SongList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sin

@Singleton
class GlobalManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val _playlistRefresh = MutableStateFlow(0L)
    val playlistRefresh = _playlistRefresh.asStateFlow()
    private val _myPlaylistRefresh = MutableStateFlow(0L)
    val myPlaylistRefresh = _myPlaylistRefresh.asStateFlow()
    private val _albumsRefresh = MutableStateFlow(0L)
    val albumsRefresh = _albumsRefresh.asStateFlow()
    private val _tracksRefresh = MutableStateFlow(0L)
    val tracksRefresh = _tracksRefresh.asStateFlow()
    private val _artistsRefresh = MutableStateFlow(0L)
    val artistsRefresh = _artistsRefresh.asStateFlow()
    private val _myMixesRefresh = MutableStateFlow(0L)
    val myMixesRefresh = _myMixesRefresh.asStateFlow()
    private val _discoveryMixesRefresh = MutableStateFlow(0L)
    val discoveryMixesRefresh = _discoveryMixesRefresh.asStateFlow()
    private val _newArrivalRefresh = MutableStateFlow(0L)
    val newArrivalRefresh = _newArrivalRefresh.asStateFlow()

    // 右侧滑栏
    private val _panelState = MutableStateFlow(PanelData())
    val panelState = _panelState.asStateFlow()

    fun refreshData(type: DataType? = null) {
        when (type) {
            DataType.PLAY_LIST -> _playlistRefresh.update { it + 1 }
            DataType.MY_PLAY_LIST -> _myPlaylistRefresh.update { it + 1 }
            DataType.ALBUM -> _albumsRefresh.update { it + 1 }
            DataType.TRACK -> _tracksRefresh.update { it + 1 }
            DataType.ARTIST -> _artistsRefresh.update { it + 1 }
            else -> {
                _playlistRefresh.update { it + 1 }
                _myPlaylistRefresh.update { it + 1 }
                _albumsRefresh.update { it + 1 }
                _tracksRefresh.update { it + 1 }
                _artistsRefresh.update { it + 1 }
            }
        }
    }

    fun openPanel(
        singleSong: SingleSong? = null,
        dataType: DataType? = null,
        songList: SongList? = null,
        lazyList: LazyPagingItems<SingleSong>? = null
    ) {
        _panelState.update {
            it.copy(
                showPanel = true,
                dataType = dataType,
                singleSong = singleSong,
                songList = songList,
                lazyList = lazyList
            )
        }
    }

    fun closePanel() {
        _panelState.update {
            PanelData()
        }
    }

}