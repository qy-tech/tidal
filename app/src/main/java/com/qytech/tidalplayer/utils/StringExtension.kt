package com.qytech.tidalplayer.utils

fun String.checkNameLegal(): Boolean {
    val regex = Regex("^(?! )(?!.* $)[^\\\\/:*?\"<>|.]+$")
    return regex.matches(this)
}

fun String.checkDescriptionLegal(): Boolean {
    val regex = Regex("[^\\\\/:*?\"<>|.]+$")
    return regex.matches(this)
}