package com.qytech.tidal.data.response

import com.qytech.tidal.data.Album
import com.qytech.tidal.data.TrackDetail
import com.qytech.tidal.data.model.AlbumResource
import com.qytech.tidal.data.model.ArtistResource
import com.qytech.tidal.data.model.ArtworkResource
import com.qytech.tidal.data.model.IncludedItem
import com.qytech.tidal.data.model.TrackResource
import com.qytech.tidal.data.model.toArtistList
import com.qytech.tidal.data.model.toTrack
import com.qytech.tidal.data.toCoverArtList

data class TracksResponse(
    val data: List<TrackResource>,
    val included: List<IncludedItem>,
)

fun TracksResponse.toTrackDetailList(): List<TrackDetail> {
    val includedData = this.included
    val artists = includedData.filterIsInstance<ArtistResource>().toArtistList()
    val artworks = includedData.filterIsInstance<ArtworkResource>()
    val albums = includedData.filterIsInstance<AlbumResource>().map { albumResource ->
        Album(
            id = albumResource.id,
            title = albumResource.attributes.title,
            barcodeId = albumResource.attributes.barcodeId,
            numberOfItems = albumResource.attributes.numberOfItems,
            duration = albumResource.attributes.duration,
            releaseDate = albumResource.attributes.releaseDate,
            coverArts = artworks.filter { it.id == albumResource.relationships.coverArt.data?.firstOrNull()?.id }
                .toCoverArtList(),
            artists = emptyList()
        )
    }
    return this.data.map {
        TrackDetail(
            trackInfo = it.toTrack(),
            artists = artists.filter { artist ->
                // todo 要是想弄多作者的话，就需要修改这个地方，这个地方限制死了只为首个，也就是说主要作者
                it.relationships.artists.data?.firstOrNull()?.id == artist.id
            },
            album = albums.first { album -> it.relationships.albums.data?.firstOrNull()?.id == album.id })
    }
}