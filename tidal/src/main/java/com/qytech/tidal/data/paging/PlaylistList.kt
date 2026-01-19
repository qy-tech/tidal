package com.qytech.tidal.data.paging

import com.qytech.tidal.data.Playlist

data class PlaylistList(
    val playlists: List<Playlist>,
    val pagination: Pagination,
)