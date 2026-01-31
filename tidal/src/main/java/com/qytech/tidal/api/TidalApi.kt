package com.qytech.tidal.api

import com.qytech.tidal.data.request.AddTracksToPlaylistRequest
import com.qytech.tidal.data.response.AlbumDetailResponse
import com.qytech.tidal.data.request.CollectionRequest
import com.qytech.tidal.data.response.CollectionResponse
import com.qytech.tidal.data.request.CreatePlaylistRequest
import com.qytech.tidal.data.response.MixesResponse
import com.qytech.tidal.data.response.PlaylistDetailResponse
import com.qytech.tidal.data.response.PlaylistItemsResponse
import com.qytech.tidal.data.response.PlaylistsResponse
import com.qytech.tidal.data.request.RemoveTracksFromPlaylistRequest
import com.qytech.tidal.data.response.AlbumsItemsResponse
import com.qytech.tidal.data.response.SearchResultResponse
import com.qytech.tidal.data.response.TracksResponse
import com.qytech.tidal.data.response.UserInfoResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TidalApi {

    /**
     * 获取专辑详情
     */
    @GET("albums/{id}")
    suspend fun getAlbum(
        @Path("id") id: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("include") include: String = "artists,coverArt,items",
    ): AlbumDetailResponse

    /**
     * 获取歌单详情
     */
    @GET("playlists/{id}")
    suspend fun getPlaylist(
        @Path("id") id: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("include") include: String = "coverArt",
    ): PlaylistDetailResponse

    /**
     * 获取用户推荐-myMixes
     */
    @GET("userRecommendations/{id}/relationships/myMixes")
    suspend fun getMyMixes(
        @Path("id") userId: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("locale") locale: String = "en-US",
        @Query("include") include: String = "myMixes.coverArt",
    ): MixesResponse

    /**
     * 获取用户推荐-discoveryMixes
     */
    @GET("userRecommendations/{id}/relationships/discoveryMixes")
    suspend fun getDiscoveryMixes(
        @Path("id") userId: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("locale") locale: String = "en-US",
        @Query("include") include: String = "discoveryMixes.coverArt",
    ): MixesResponse

    /**
     * 获取用户推荐-newArrivalMixes
     */
    @GET("userRecommendations/{id}/relationships/newArrivalMixes")
    suspend fun getNewArrivalMixes(
        @Path("id") userId: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("locale") locale: String = "en-US",
        @Query("include") include: String = "newArrivalMixes.coverArt",
    ): MixesResponse

    /**
     * 获取多个单曲
     */
    @GET("tracks")
    suspend fun getTracks(
        @Query("countryCode") countryCode: String = "US",
        @Query("include") include: String = "albums.coverArt,artists",
        @Query("filter[id]") trackIds: List<String>,
    ): TracksResponse

    /**
     * 获取歌单中的单曲
     */
    @GET("playlists/{id}/relationships/items")
    suspend fun getPlaylistItems(
        @Path("id") playlistId: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("page[cursor]") pageCursor: String? = null,
        @Query("include") include: String? = null,
    ): PlaylistItemsResponse

    @GET("albums/{id}/relationships/items")
    suspend fun getAlbumsItems(
        @Path("id") albumsId: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("page[cursor]") pageCursor: String? = null,
        @Query("include") include: String? = null,
    ): AlbumsItemsResponse

    /**
     * 获取收藏的专辑
     */
    @GET("userCollections/{id}/relationships/albums")
    suspend fun getCollectionAlbums(
        @Path("id") userId: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("locale") locale: String = "en-US",
        @Query("page[cursor]") pageCursor: String? = null,
        @Query("sort") sort: String = "-albums.addedAt",
        @Query("include") include: String? = "albums.coverArt,albums.artists",
    ): CollectionResponse

    /**
     * 收藏专辑
     */
    @POST("userCollections/{id}/relationships/albums")
    suspend fun addAlbumsToCollection(
        @Path("id") userId: String,
        @Query("countryCode") countryCode: String = "US",
        @Body requestBody: CollectionRequest,
    )

    /**
     * 取消专辑收藏
     */
    @HTTP(method = "DELETE", path = "userCollections/{id}/relationships/albums", hasBody = true)
    suspend fun removeAlbumsFromCollection(
        @Path("id") userId: String,
        @Body requestBody: CollectionRequest,
    )

    /**
     * 获取收藏的歌单
     */
    @GET("userCollections/{id}/relationships/playlists")
    suspend fun getCollectionPlaylists(
        @Path("id") userId: String,
        @Query("page[cursor]") pageCursor: String? = null,
        @Query("sort") sort: String = "-playlists.addedAt",
        @Query("include") include: String? = "playlists.coverArt",
    ): CollectionResponse

    /**
     * 收藏歌单
     */
    @POST("userCollections/{id}/relationships/playlists")
    suspend fun addPlaylistsToCollection(
        @Path("id") userId: String,
        @Body requestBody: CollectionRequest,
    )

    /**
     * 取消歌单收藏
     */
    @HTTP(method = "DELETE", path = "userCollections/{id}/relationships/playlists", hasBody = true)
    suspend fun removePlaylistsFromCollection(
        @Path("id") userId: String,
        @Body requestBody: CollectionRequest,
    )

    /**
     * 获取收藏的单曲
     */
    @GET("userCollections/{id}/relationships/tracks")
    suspend fun getCollectionTracks(
        @Path("id") userId: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("locale") locale: String = "en-US",
        @Query("page[cursor]") pageCursor: String? = null,
        @Query("sort") sort: String = "-tracks.addedAt",
        @Query("include") include: String? = null,
    ): CollectionResponse

    /**
     * 收藏单曲
     */
    @POST("userCollections/{id}/relationships/tracks")
    suspend fun addTracksToCollection(
        @Path("id") userId: String,
        @Query("countryCode") countryCode: String = "US",
        @Body requestBody: CollectionRequest,
    )

    /**
     * 取消单曲收藏
     */
    @HTTP(method = "DELETE", path = "userCollections/{id}/relationships/tracks", hasBody = true)
    suspend fun removeTracksFromCollection(
        @Path("id") userId: String,
        @Body requestBody: CollectionRequest,
    )

    /**
     * 获取收藏的艺术家
     */
    @GET("userCollections/{id}/relationships/artists")
    suspend fun getCollectionArtists(
        @Path("id") userId: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("locale") locale: String = "en-US",
        @Query("page[cursor]") pageCursor: String? = null,
        @Query("sort") sort: String = "-artists.addedAt",
        @Query("include") include: String? = "artists.profileArt",
    ): CollectionResponse

    /**
     * 收藏艺术家
     */
    @POST("userCollections/{id}/relationships/artists")
    suspend fun addArtistsToCollection(
        @Path("id") userId: String,
        @Query("countryCode") countryCode: String = "US",
        @Body requestBody: CollectionRequest,
    )

    /**
     * 取消艺术家收藏
     */
    @HTTP(method = "DELETE", path = "userCollections/{id}/relationships/artists", hasBody = true)
    suspend fun removeArtistsFromCollection(
        @Path("id") userId: String,
        @Body requestBody: CollectionRequest,
    )

    /**
     * 搜索专辑
     */
    @GET("searchResults/{id}/relationships/albums")
    suspend fun searchAlbums(
        @Path("id") query: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("page[cursor]") pageCursor: String? = null,
        @Query("include") include: String = "albums.coverArt,albums.artists",
    ): SearchResultResponse

    /**
     * 搜索艺术家
     */
    @GET("searchResults/{id}/relationships/artists")
    suspend fun searchArtists(
        @Path("id") query: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("page[cursor]") pageCursor: String? = null,
        @Query("include") include: String = "artists.profileArt",
    ): SearchResultResponse

    /**
     * 搜索歌单
     */
    @GET("searchResults/{id}/relationships/playlists")
    suspend fun searchPlaylists(
        @Path("id") query: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("page[cursor]") pageCursor: String? = null,
        @Query("include") include: String = "playlists.coverArt",
    ): SearchResultResponse

    /**
     * 搜索单曲
     */
    @GET("searchResults/{id}/relationships/tracks")
    suspend fun searchTracks(
        @Path("id") query: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("page[cursor]") pageCursor: String? = null,
        @Query("include") include: String? = null,
    ): SearchResultResponse

    /**
     * 获取用户的歌单
     */
    @GET("playlists")
    suspend fun getUserPlaylists(
        @Query("countryCode") countryCode: String = "US",
        @Query("page[cursor]") pageCursor: String? = null,
        @Query("sort") sort: String = "-createdAt",
        @Query("include") include: String = "coverArt",
        @Query("filter[id]") playlistId: String? = null,
        @Query("filter[owners.id]") userId: String,
    ): PlaylistsResponse

    /**
     * 创建歌单
     */
    @POST("playlists")
    suspend fun createPlaylist(
        @Query("countryCode") countryCode: String = "US",
        @Body requestBody: CreatePlaylistRequest,
    ): PlaylistDetailResponse

    /**
     * 删除歌单
     */
    @DELETE("playlists/{id}")
    suspend fun deletePlaylist(
        @Path("id") playlistId: String,
    )

    /**
     * 添加单曲到歌单
     */
    @POST("playlists/{id}/relationships/items")
    suspend fun addTracksToPlaylist(
        @Path("id") playlistId: String,
        @Query("countryCode") countryCode: String = "US",
        @Body requestBody: AddTracksToPlaylistRequest,
    )

    /**
     * 从歌单中删除单曲
     */
    @HTTP(method = "DELETE", path = "playlists/{id}/relationships/items", hasBody = true)
    suspend fun removeTracksFromPlaylist(
        @Path("id") playlistId: String,
        @Body requestBody: RemoveTracksFromPlaylistRequest,
    )

    /**
     * 获取艺术家的专辑
     */
    @GET("artists/{id}/relationships/albums")
    suspend fun getArtistAlbums(
        @Path("id") artistId: String,
        @Query("countryCode") countryCode: String = "US",
        @Query("page[cursor]") pageCursor: String? = null,
        @Query("include") include: String = "albums.coverArt,albums.artists",
    ): CollectionResponse

    /**
     * 获取用户信息
     */
    @GET("users/me")
    suspend fun getUser(): UserInfoResponse

}