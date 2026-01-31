package com.qytech.tidalplayer.ui

import com.qytech.tidalplayer.ui.listpage.model.DataType

object TidalRoute {
    const val TRACK_LIST_ID = "for you tracks"
    const val TRACK_LIST_SHRINK_ID = "for you tracks shrink"

    const val LOGIN_START = "login_start"
    const val LOGIN_WEB = "login_web"
    const val LOGIN_QRCODE = "login_qrcode"
    const val SONG_LIST_START = "song_list_start"

    const val SONG_LIST = "song_list"
    const val USER_INFO = "user_info"
    const val SEARCH_SONG = "search_song"
    const val ITEM_TRACK_LIST = "item_track_list?listId={listId}&dataType={dataType}&coverUrl={coverUrl}&title={title}&description={description}" // 是list中的item

    const val PLAYLIST_ALBUM_LIST = "playlist_album_list?artistId={artistId}&dataType={dataType}&title={title}"
    const val TRACK_LIST = "track_list" // 单独的item list
    const val ARTIST_LIST = "artist_list?title={title}"

    fun getItemTrackListRoute(
        listId: String,
        dataType: DataType,
        coverUrl: String?,
        title: String,
        description: String?
    ): String {
        return "item_track_list?listId=${listId}&dataType=${dataType.ordinal}&coverUrl=${coverUrl}&title=${title}&description=${description}"
    }

    fun getPlaylistAlbumListRoute(
        artistId: String = "",
        title: String,
        dataType: DataType,
    ): String {
        return "playlist_album_list?artistId=${artistId}&dataType=${dataType.ordinal}&title=${title}"
    }

    fun getArtistListRoute(
        title: String,
    ): String {
        return "artist_list?title=${title}"
    }
}