package com.qytech.tidal.data.paging

import com.qytech.tidal.data.TrackDetail

data class PlaylistTracks(
    val tracks: List<TrackDetail>,
    val pagination: Pagination,
)