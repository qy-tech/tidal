package com.qytech.tidal.repository

import com.qytech.tidal.api.TidalApi
import com.qytech.tidal.cache.TidalStore
import com.qytech.tidal.data.Album
import com.qytech.tidal.data.AlbumDetail
import com.qytech.tidal.data.Artist
import com.qytech.tidal.data.ArtistDetail
import com.qytech.tidal.data.Playlist
import com.qytech.tidal.data.ResourceType
import com.qytech.tidal.data.TrackDetail
import com.qytech.tidal.data.UserInfo
import com.qytech.tidal.data.model.AlbumResource
import com.qytech.tidal.data.model.ArtistResource
import com.qytech.tidal.data.model.ArtworkResource
import com.qytech.tidal.data.model.MinimalistResources
import com.qytech.tidal.data.model.PlaylistItemMeta
import com.qytech.tidal.data.model.PlaylistResource
import com.qytech.tidal.data.model.TrackResource
import com.qytech.tidal.data.model.toArtistList
import com.qytech.tidal.data.model.toTrackList
import com.qytech.tidal.data.paging.AlbumList
import com.qytech.tidal.data.paging.AlbumsTracks
import com.qytech.tidal.data.paging.ArtistList
import com.qytech.tidal.data.paging.Pagination
import com.qytech.tidal.data.paging.PlaylistList
import com.qytech.tidal.data.paging.PlaylistTracks
import com.qytech.tidal.data.request.AddTracksToPlaylistData
import com.qytech.tidal.data.request.AddTracksToPlaylistRequest
import com.qytech.tidal.data.request.CollectionRequest
import com.qytech.tidal.data.request.CreatePlaylistAttributes
import com.qytech.tidal.data.request.CreatePlaylistData
import com.qytech.tidal.data.request.CreatePlaylistRequest
import com.qytech.tidal.data.request.RemoveTracksFromPlaylistData
import com.qytech.tidal.data.request.RemoveTracksFromPlaylistRequest
import com.qytech.tidal.data.response.toTrackDetailList
import com.qytech.tidal.data.toCoverArtList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TidalRepository @Inject constructor(
    private val api: TidalApi,
    private val store: TidalStore
) {
    private val _collectionTrackIds = MutableStateFlow<Set<String>>(emptySet())
    val collectionTrackIds = _collectionTrackIds.asStateFlow()
    private var _collectionAlbumIds = MutableStateFlow<Set<String>>(emptySet())
    val collectionAlbumIds = _collectionAlbumIds.asStateFlow()
    private var _collectionArtistIds = MutableStateFlow<Set<String>>(emptySet())
    val collectionArtistIds = _collectionArtistIds.asStateFlow()
    private var _collectionPlaylistIds = MutableStateFlow<Set<String>>(emptySet())
    val collectionPlaylistIds = _collectionPlaylistIds.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        repositoryScope.launch {
            store.tidalStoreData.collect { data ->
                _collectionTrackIds.update { data.collectionTrackIds.toSet() }
                _collectionAlbumIds.update { data.collectionAlbumIds.toSet() }
                _collectionArtistIds.update { data.collectArtistIds.toSet() }
                _collectionPlaylistIds.update { data.collectionPlaylistIds.toSet() }
            }
        }
    }

    private fun updateCollectionTrackIds(trackIds: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            store.updateByKey(TidalStore.StoreKeys.COLLECTION_TRACK_IDS, trackIds)
        }
    }

    private fun addTrackIdsToCollection(trackIds: List<String>) {
        updateCollectionTrackIds((collectionTrackIds.value + trackIds).toList())
    }

    private fun removeTrackIdsFromCollection(trackIds: List<String>) {
        updateCollectionTrackIds((collectionTrackIds.value - trackIds).toList())
    }

    private fun updateCollectionAlbumIds(albumIds: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            store.updateByKey(TidalStore.StoreKeys.COLLECTION_ALBUM_IDS, albumIds)
        }
    }

    private fun addAlbumIdsToCollection(albumIds: List<String>) {
        updateCollectionAlbumIds(albumIds.union(_collectionAlbumIds.value).toList())
    }

    private fun removeAlbumIdsFromCollection(albumIds: List<String>) {
        updateCollectionAlbumIds(_collectionAlbumIds.value.toList() - albumIds.toSet())
    }

    private fun updateCollectionArtistIds(artistIds: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            store.updateByKey(TidalStore.StoreKeys.COLLECTION_ARTIST_IDS, artistIds)
        }
    }

    private fun addArtistIdsToCollection(artistIds: List<String>) {
        updateCollectionArtistIds(artistIds.union(_collectionArtistIds.value).toList())
    }

    private fun removeArtistIdsFromCollection(artistIds: List<String>) {
        updateCollectionArtistIds(_collectionArtistIds.value.toList() - artistIds.toSet())
    }

    private fun updateCollectionPlaylistIds(playlistIds: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            store.updateByKey(TidalStore.StoreKeys.COLLECTION_PLAYLIST_IDS, playlistIds)
        }
    }

    private fun addPlaylistIdsToCollection(playlistIds: List<String>) {
        updateCollectionPlaylistIds(playlistIds.union(_collectionPlaylistIds.value).toList())
    }

    private fun removePlaylistIdsFromCollection(playlistIds: List<String>) {
        updateCollectionPlaylistIds(_collectionPlaylistIds.value.toList() - playlistIds.toSet())
    }

    /**
     * 获取专辑详情
     */
    fun getAlbumDetail(albumId: String): Flow<AlbumDetail> = flow {
        emit(api.getAlbum(albumId))
    }.map { response ->
        val albumData = response.data.attributes
        val includedData = response.included
        val artists = includedData.filterIsInstance<ArtistResource>().toArtistList()
        val coverArts = includedData.filterIsInstance<ArtworkResource>().toCoverArtList()
        val tracks = includedData.filterIsInstance<TrackResource>().toTrackList()
        val albumInfo = Album(
            id = response.data.id,
            title = albumData.title,
            barcodeId = albumData.barcodeId,
            numberOfItems = albumData.numberOfItems,
            duration = albumData.duration,
            releaseDate = albumData.releaseDate,
            coverArts = coverArts,
            artists = artists
        )
        AlbumDetail(
            albumInfo = albumInfo,
            tracks = tracks
        )
    }.flowOn(Dispatchers.IO).catch { throw it }

    /**
     * 获取歌单详情
     */
    fun getPlaylistDetail(playlistId: String): Flow<Playlist> = flow {
        emit(api.getPlaylist(playlistId))
    }.map { response ->
        val playlistData = response.data.attributes
        val includedData = response.included
        val coverArts = includedData.filterIsInstance<ArtworkResource>().toCoverArtList()
        Playlist(
            id = response.data.id,
            name = playlistData.name,
            description = playlistData.description,
            createdAt = playlistData.createdAt,
            lastModifiedAt = playlistData.lastModifiedAt,
            coverArts = coverArts
        )
    }.flowOn(Dispatchers.IO).catch { throw it }

    /**
     * 获取用户推荐-myMixes
     */
    suspend fun getMyMixes(userId: String): List<Playlist> {
        val response = api.getMyMixes(userId)
        val includedData = response.included
        val artworks = includedData.filterIsInstance<ArtworkResource>()
        return includedData.filterIsInstance<PlaylistResource>().map { playlist ->
            Playlist(
                id = playlist.id,
                name = playlist.attributes.name,
                description = playlist.attributes.description,
                createdAt = playlist.attributes.createdAt,
                lastModifiedAt = playlist.attributes.lastModifiedAt,
                coverArts = artworks.filter { it.id == playlist.relationships.coverArt.data?.firstOrNull()?.id }
                    .toCoverArtList()
            )
        }.sortedBy { it.name }
    }

    /**
     * 获取用户推荐-discoveryMixes
     */
    suspend fun getDiscoveryMixes(userId: String): List<Playlist> {
        val response = api.getDiscoveryMixes(userId)
        val includedData = response.included
        val artworks = includedData.filterIsInstance<ArtworkResource>()
        return includedData.filterIsInstance<PlaylistResource>().map { playlist ->
            Playlist(
                id = playlist.id,
                name = playlist.attributes.name,
                description = playlist.attributes.description,
                createdAt = playlist.attributes.createdAt,
                lastModifiedAt = playlist.attributes.lastModifiedAt,
                coverArts = artworks.filter { it.id == playlist.relationships.coverArt.data?.firstOrNull()?.id }
                    .toCoverArtList()
            )
        }
    }

    /**
     * 获取用户推荐-newArrivalMixes
     */
    suspend fun getNewArrivalMixes(userId: String): List<Playlist> {
        val response = api.getNewArrivalMixes(userId)
        val includedData = response.included
        val artworks = includedData.filterIsInstance<ArtworkResource>()
        return includedData.filterIsInstance<PlaylistResource>().map { playlist ->
            Playlist(
                id = playlist.id,
                name = playlist.attributes.name,
                description = playlist.attributes.description,
                createdAt = playlist.attributes.createdAt,
                lastModifiedAt = playlist.attributes.lastModifiedAt,
                coverArts = artworks.filter { it.id == playlist.relationships.coverArt.data?.firstOrNull()?.id }
                    .toCoverArtList()
            )
        }
    }

    /**
     * 获取多个单曲
     */
    fun getTracks(trackIds: List<String>): Flow<List<TrackDetail>> = flow {
        emit(api.getTracks(trackIds = trackIds))
    }.map { response ->
        response.toTrackDetailList()
    }.flowOn(Dispatchers.IO).catch { emit(emptyList()) }

    /**
     * 获取歌单中的单曲
     */
    suspend fun getPlaylistTracks(playlistId: String, pageCursor: String? = null): PlaylistTracks {
        val playlistItemsResponse =
            api.getPlaylistItems(playlistId = playlistId, pageCursor = pageCursor)
        val pagination = Pagination(nextCursor = playlistItemsResponse.links.meta?.nextCursor)
        val trackIds = playlistItemsResponse.data.map { it.id }

        val tracksResponse = api.getTracks(trackIds = trackIds)
        val tracks = tracksResponse.toTrackDetailList().map { track ->
            track.copy(itemId = playlistItemsResponse.data.firstOrNull { it.id == track.trackInfo.id }?.meta?.itemId)
        }

        return PlaylistTracks(
            tracks = tracks,
            pagination = pagination
        )
    }

    suspend fun getAlbumsTracks(albumId: String, pageCursor: String? = null): AlbumsTracks {
        val response = api.getAlbumsItems(albumsId = albumId, pageCursor = pageCursor)
        val pagination = Pagination(nextCursor = response.links.meta?.nextCursor)
        val trackIds = response.data.map { it.id }

        val tracksResponse = api.getTracks(trackIds = trackIds)
        val tracks = tracksResponse.toTrackDetailList()

        return AlbumsTracks(
            tracks = tracks,
            pagination = pagination
        )
    }

    /**
     * 获取收藏的专辑
     */
    suspend fun getCollectionAlbums(userId: String, pageCursor: String? = null): AlbumList {
        val response = api.getCollectionAlbums(userId = userId, pageCursor = pageCursor)
        val pagination = Pagination(nextCursor = response.links.meta?.nextCursor)
        val includedData = response.included!!
        val artists = includedData.filterIsInstance<ArtistResource>().toArtistList()
        val artworks = includedData.filterIsInstance<ArtworkResource>()
        val albums = includedData.filterIsInstance<AlbumResource>().map { albumResource ->
            val albumData = albumResource.attributes
            val coverArts =
                artworks.filter { artwork -> albumResource.relationships.coverArt.data?.firstOrNull()?.id == artwork.id }
                    .toCoverArtList()
            val artists =
                artists.filter { artist -> albumResource.relationships.artists.data?.firstOrNull()?.id == artist.id }
            Album(
                id = albumResource.id,
                title = albumData.title,
                barcodeId = albumData.barcodeId,
                numberOfItems = albumData.numberOfItems,
                duration = albumData.duration,
                releaseDate = albumData.releaseDate,
                coverArts = coverArts,
                artists = artists
            )
        }

        addAlbumIdsToCollection(albums.map { it.id })

        return AlbumList(
            albums = albums,
            pagination = pagination
        )
    }

    /**
     * 收藏专辑
     */
    suspend fun addAlbumsToCollection(userId: String, albumIds: List<String>) {
        api.addAlbumsToCollection(
            userId = userId,
            requestBody = CollectionRequest(
                albumIds.map { MinimalistResources(id = it, type = ResourceType.ALBUM.type) }
            )
        )

        addAlbumIdsToCollection(albumIds)
    }

    /**
     * 检查专辑是否被收藏
     */
    fun checkAlbumIsCollected(albumId: String): Boolean = _collectionAlbumIds.value.contains(albumId)

    /**
     * 取消专辑收藏
     */
    suspend fun removeAlbumsFromCollection(userId: String, albumIds: List<String>) {
        api.removeAlbumsFromCollection(
            userId = userId,
            requestBody = CollectionRequest(
                albumIds.map { MinimalistResources(id = it, type = ResourceType.ALBUM.type) }
            )
        )

        removeAlbumIdsFromCollection(albumIds)
    }

    /**
     * 获取收藏的歌单
     */
    suspend fun getCollectionPlaylists(userId: String, pageCursor: String? = null): PlaylistList {
        val response = api.getCollectionPlaylists(userId = userId, pageCursor = pageCursor)
        val pagination = Pagination(nextCursor = response.links.meta?.nextCursor)
        val includedData = response.included!!
        val artworks = includedData.filterIsInstance<ArtworkResource>()
        val playlists = includedData.filterIsInstance<PlaylistResource>().map {
            Playlist(
                id = it.id,
                name = it.attributes.name,
                description = it.attributes.description,
                createdAt = it.attributes.createdAt,
                lastModifiedAt = it.attributes.lastModifiedAt,
                coverArts = artworks.filter { artwork -> it.relationships.coverArt.data?.firstOrNull()?.id == artwork.id }
                    .toCoverArtList()
            )
        }

        addPlaylistIdsToCollection(playlists.map { it.id })

        return PlaylistList(
            playlists = playlists,
            pagination = pagination
        )
    }

    /**
     * 收藏歌单
     */
    suspend fun addPlaylistsToCollection(userId: String, playlistIds: List<String>) {
        api.addPlaylistsToCollection(
            userId = userId,
            requestBody = CollectionRequest(
                playlistIds.map {
                    MinimalistResources(
                        id = it,
                        type = ResourceType.PLAYLIST.type
                    )
                }
            )
        )

        addPlaylistIdsToCollection(playlistIds)
    }

    /**
     * 检查歌单是否被收藏
     */
    fun checkPlaylistIsCollected(playlistId: String): Boolean =
        _collectionPlaylistIds.value.contains(playlistId)

    /**
     * 取消歌单收藏
     */
    suspend fun removePlaylistsFromCollection(userId: String, playlistIds: List<String>) {
        api.removePlaylistsFromCollection(
            userId = userId,
            requestBody = CollectionRequest(
                playlistIds.map {
                    MinimalistResources(
                        id = it,
                        type = ResourceType.PLAYLIST.type
                    )
                }
            )
        )

        removePlaylistIdsFromCollection(playlistIds)
    }

    /**
     * 获取收藏的单曲
     */
    suspend fun getCollectionTracks(userId: String, pageCursor: String? = null): PlaylistTracks {
        val collectionResponse =
            api.getCollectionTracks(userId = userId, pageCursor = pageCursor)
        val pagination = Pagination(nextCursor = collectionResponse.links.meta?.nextCursor)
        val trackIds = collectionResponse.data.map { it.id }

        val tracksResponse = api.getTracks(trackIds = trackIds)
        val tracks = tracksResponse.toTrackDetailList()

        addTrackIdsToCollection(trackIds)

        return PlaylistTracks(
            tracks = tracks,
            pagination = pagination
        )
    }

    /**
     * 收藏单曲
     */
    suspend fun addTracksToCollection(userId: String, trackIds: List<String>) {
        api.addTracksToCollection(
            userId = userId,
            requestBody = CollectionRequest(
                trackIds.map { MinimalistResources(id = it, type = ResourceType.TRACK.type) }
            )
        )
        addTrackIdsToCollection(trackIds)
    }

    /**
     * 取消单曲收藏
     */
    suspend fun removeTracksFromCollection(userId: String, trackIds: List<String>) {
        api.removeTracksFromCollection(
            userId = userId,
            requestBody = CollectionRequest(
                trackIds.map { MinimalistResources(id = it, type = ResourceType.TRACK.type) }
            )
        )

        removeTrackIdsFromCollection(trackIds)
    }

    /**
     * 获取收藏的艺术家
     */
    suspend fun getCollectionArtists(userId: String, pageCursor: String? = null): ArtistList {
        val response = api.getCollectionArtists(userId = userId, pageCursor = pageCursor)
        val pagination = Pagination(nextCursor = response.links.meta?.nextCursor)
        val includedData = response.included!!
        val artworks = includedData.filterIsInstance<ArtworkResource>()
        val artists = includedData.filterIsInstance<ArtistResource>().map {
            ArtistDetail(
                artistInfo = Artist(
                    id = it.id,
                    name = it.attributes.name
                ),
                profileArts = artworks.filter { artwork -> it.relationships.profileArt.data?.firstOrNull()?.id == artwork.id }
                    .toCoverArtList()
            )
        }

        addArtistIdsToCollection(artists.map { it.artistInfo.id })

        return ArtistList(
            artists = artists,
            pagination = pagination
        )
    }

    /**
     * 收藏艺术家
     */
    suspend fun addArtistsToCollection(userId: String, artistIds: List<String>) {
        api.addArtistsToCollection(
            userId = userId,
            requestBody = CollectionRequest(
                artistIds.map { MinimalistResources(id = it, type = ResourceType.ARTIST.type) }
            )
        )

        addArtistIdsToCollection(artistIds)
    }

    /**
     * 检查艺术家是否被收藏
     */
    fun checkArtistIsCollected(artistId: String): Boolean = _collectionArtistIds.value.contains(artistId)

    /**
     * 取消艺术家收藏
     */
    suspend fun removeArtistsFromCollection(userId: String, artistIds: List<String>) {
        api.removeArtistsFromCollection(
            userId = userId,
            requestBody = CollectionRequest(
                artistIds.map { MinimalistResources(id = it, type = ResourceType.ARTIST.type) }
            )
        )

        removeArtistIdsFromCollection(artistIds)
    }

    /**
     * 搜索专辑
     */
    suspend fun searchAlbums(query: String, pageCursor: String? = null): AlbumList {
        val response = api.searchAlbums(query = query, pageCursor = pageCursor)
        val pagination = Pagination(nextCursor = response.links.meta?.nextCursor)
        val includedData = response.included!!
        val artists = includedData.filterIsInstance<ArtistResource>().toArtistList()
        val artworks = includedData.filterIsInstance<ArtworkResource>()
        val albums = includedData.filterIsInstance<AlbumResource>().map { albumResource ->
            val albumData = albumResource.attributes
            val coverArts =
                artworks.filter { artwork -> albumResource.relationships.coverArt.data?.firstOrNull()?.id == artwork.id }
                    .toCoverArtList()
            val artists =
                artists.filter { artist -> albumResource.relationships.artists.data?.firstOrNull()?.id == artist.id }
            Album(
                id = albumResource.id,
                title = albumData.title,
                barcodeId = albumData.barcodeId,
                numberOfItems = albumData.numberOfItems,
                duration = albumData.duration,
                releaseDate = albumData.releaseDate,
                coverArts = coverArts,
                artists = artists
            )
        }
        return AlbumList(
            albums = albums,
            pagination = pagination
        )
    }

    /**
     * 搜索艺术家
     */
    suspend fun searchArtists(query: String, pageCursor: String? = null): ArtistList{
        val response = api.searchArtists(query = query, pageCursor = pageCursor)
        val pagination = Pagination(nextCursor = response.links.meta?.nextCursor)
        val includedData = response.included!!
        val artworks = includedData.filterIsInstance<ArtworkResource>()
        val artists = includedData.filterIsInstance<ArtistResource>().map {
            ArtistDetail(
                artistInfo = Artist(
                    id = it.id,
                    name = it.attributes.name
                ),
                profileArts = artworks.filter { artwork -> it.relationships.profileArt.data?.firstOrNull()?.id == artwork.id }
                    .toCoverArtList()
            )
        }
        return ArtistList(
            artists = artists,
            pagination = pagination
        )
    }

    /**
     * 搜索歌单
     */
    suspend fun searchPlaylists(query: String, pageCursor: String? = null): PlaylistList {
        val response = api.searchPlaylists(query = query, pageCursor = pageCursor)

        val pagination = Pagination(nextCursor = response.links.meta?.nextCursor)
        val includedData = response.included!!
        val artworks = includedData.filterIsInstance<ArtworkResource>()
        val playlists = includedData.filterIsInstance<PlaylistResource>().map {
            Playlist(
                id = it.id,
                name = it.attributes.name,
                description = it.attributes.description,
                createdAt = it.attributes.createdAt,
                lastModifiedAt = it.attributes.lastModifiedAt,
                coverArts = artworks.filter { artwork -> it.relationships.coverArt.data?.firstOrNull()?.id == artwork.id }
                    .toCoverArtList()
            )
        }
        return PlaylistList(
            playlists = playlists,
            pagination = pagination
        )
    }

    /**
     * 搜索单曲
     */
    suspend fun searchTracks(query: String, pageCursor: String? = null): PlaylistTracks {
        val searchResponse = api.searchTracks(query = query, pageCursor = pageCursor)
        val pagination = Pagination(nextCursor = searchResponse.links.meta?.nextCursor)
        val trackIds = searchResponse.data.map { it.id }

        val tracksResponse = api.getTracks(trackIds = trackIds)
        val tracks = tracksResponse.toTrackDetailList()
        return                 PlaylistTracks(
            tracks = tracks,
            pagination = pagination
        )
    }

    /**
     * 获取用户的歌单
     */
    suspend fun getUserPlaylists(userId: String, pageCursor: String? = null): PlaylistList {
        val response = api.getUserPlaylists(userId = userId, pageCursor = pageCursor)
        val pagination = Pagination(nextCursor = response.links.meta?.nextCursor)
        val includedData = response.included
        val artworks = includedData.filterIsInstance<ArtworkResource>()
        val playlists = response.data.map {
            Playlist(
                id = it.id,
                name = it.attributes.name,
                description = it.attributes.description,
                createdAt = it.attributes.createdAt,
                lastModifiedAt = it.attributes.lastModifiedAt,
                coverArts = artworks.filter { artwork -> it.relationships.coverArt.data?.firstOrNull()?.id == artwork.id }
                    .toCoverArtList()
            )
        }
        return PlaylistList(
            playlists = playlists,
            pagination = pagination
        )
    }

    /**
     * 创建歌单
     */
    suspend fun createPlaylist(
        name: String,
        description: String = "",
    ): Playlist {
        val response = api.createPlaylist(
            requestBody = CreatePlaylistRequest(
                CreatePlaylistData(
                    type = ResourceType.PLAYLIST.type,
                    attributes = CreatePlaylistAttributes(
                        name = name,
                        description = description
                    )
                )
            )
        )
        val playlistData = response.data.attributes
        return Playlist(
            id = response.data.id,
            name = playlistData.name,
            description = playlistData.description,
            createdAt = playlistData.createdAt,
            lastModifiedAt = playlistData.lastModifiedAt,
            coverArts = emptyList()
        )
    }

    /**
     * 删除歌单
     */
    suspend fun deletePlaylist(
        playlistId: String,
    ) {
        api.deletePlaylist(playlistId = playlistId)
    }

    /**
     * 添加单曲到歌单
     */
    fun addTracksToPlaylist(
        playlistId: String,
        trackIds: List<String>,
    ): Flow<Unit> = flow {
        emit(
            api.addTracksToPlaylist(
                playlistId = playlistId,
                requestBody = AddTracksToPlaylistRequest(
                    data = trackIds.map {
                        AddTracksToPlaylistData(
                            type = ResourceType.TRACK.type,
                            id = it
                        )
                    }
                )
            )
        )
    }.flowOn(Dispatchers.IO).catch { throw it }

    /**
     * 从歌单中删除单曲
     */
    fun removeTracksFromPlaylist(
        playlistId: String,
        tracks: List<TrackDetail>,
    ): Flow<Unit> = flow {
        emit(
            api.removeTracksFromPlaylist(
                playlistId = playlistId,
                requestBody = RemoveTracksFromPlaylistRequest(
                    data = tracks.map {
                        RemoveTracksFromPlaylistData(
                            type = ResourceType.TRACK.type,
                            id = it.trackInfo.id,
                            meta = PlaylistItemMeta(
                                itemId = it.itemId ?: playlistId
                            )
                        )
                    }
                )
            )
        )
    }.flowOn(Dispatchers.IO).catch { throw it }

    /**
     * 获取艺术家的专辑
     */
    suspend fun getArtistAlbums(artistId: String, pageCursor: String? = null): AlbumList {
        val response = api.getArtistAlbums(artistId = artistId, pageCursor = pageCursor)
        val pagination = Pagination(nextCursor = response.links.meta?.nextCursor)
        val includedData = response.included!!
        val artists = includedData.filterIsInstance<ArtistResource>().toArtistList()
        val artworks = includedData.filterIsInstance<ArtworkResource>()
        val albums = includedData.filterIsInstance<AlbumResource>().map { albumResource ->
            val albumData = albumResource.attributes
            val coverArts =
                artworks.filter { artwork -> albumResource.relationships.coverArt.data?.firstOrNull()?.id == artwork.id }
                    .toCoverArtList()
            val artists =
                artists.filter { artist -> albumResource.relationships.artists.data?.firstOrNull()?.id == artist.id }
            Album(
                id = albumResource.id,
                title = albumData.title,
                barcodeId = albumData.barcodeId,
                numberOfItems = albumData.numberOfItems,
                duration = albumData.duration,
                releaseDate = albumData.releaseDate,
                coverArts = coverArts,
                artists = artists
            )
        }
        return AlbumList(
            albums = albums,
            pagination = pagination
        )
    }

    /**
     * 获取用户信息
     */
    fun getUser(): Flow<UserInfo> = flow {
        emit(api.getUser())
    }.map { response ->
        val userData = response.data.attributes
        UserInfo(
            id = response.data.id,
            userName = userData.username,
            email = userData.email,
            firstName = userData.firstName,
            lastName = userData.lastName
        )
    }.flowOn(Dispatchers.IO).catch { throw it }
}