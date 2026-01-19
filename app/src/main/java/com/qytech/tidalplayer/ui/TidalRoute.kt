package com.qytech.tidalplayer.ui

import com.qytech.tidalplayer.ui.listpage.model.DataType

object TidalRoute {
    const val LOGIN_START = "login_start"
    const val LOGIN_WEB = "login_web"
    const val LOGIN_QRCODE = "login_qrcode"
    const val SONG_LIST_START = "song_list_start"

    const val SONG_LIST = "song_list"
    const val USER_INFO = "user_info"
    const val SEARCH_SONG = "search_song"
    const val ITEM_TRACK_LIST = "item_track_list?listId={listId}&dataType={dataType}" // 是list中的item
    const val TRACK_LIST_2 = "track_list_2" // 单独的item list

    fun getItemTrackListRoute(listId: String, dataType: DataType): String {
        return "item_track_list?listId=${listId}&dataType=${dataType.ordinal}"
    }
}