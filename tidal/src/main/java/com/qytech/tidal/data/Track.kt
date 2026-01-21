package com.qytech.tidal.data

data class Track(
    val id: String,
    val title: String,
    val version: String = "",
    val duration: String,
)

fun String.toDisplayDuration(): String {
    val split = this.replace("PT|S".toRegex(), "").split("[HM]".toRegex())
    if (!this.contains("H") && !this.contains("M")) {
        return if (split.isEmpty()) {
            "00:00"
        } else {
            "00:${split[0]}"
        }
    }
    // 分割，如果长度为3，必定有H和M
    // 如果长度为2，判断是否有H，如果有H，则需要后补 00：00
    // 如果没有H，则就是M，则需要后补 00
    val sb = StringBuilder()
    for (s in split) {
        if (s.length == 1) {
            sb.append("0$s")
        } else if (s.isEmpty()) {
            sb.append("00")
        } else {
            sb.append(s)
        }
        sb.append(":")
    }
    if (split.size == 2 && this.contains("H")) {
        sb.append("00").append(":")
    }

    return sb.deleteCharAt(sb.length - 1).toString()
}