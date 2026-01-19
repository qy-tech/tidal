package com.qytech.tidal.data

import android.os.Parcelable
import com.qytech.tidal.data.model.ArtworkResource
import kotlinx.parcelize.Parcelize

@Parcelize
data class CoverArt(
    val url: String,
    val width: Int,
    val height: Int,
) : Parcelable

fun List<ArtworkResource>.toCoverArtList() = this.flatMap { artwork ->
    artwork.attributes.files.map { file ->
        CoverArt(
            url = file.href,
            width = file.meta.width,
            height = file.meta.height
        )
    }
}

fun List<CoverArt>.getBestImageBy(preferredWidth: Int = ImageSize.MEDIUM.size): CoverArt? {
    val exactMatch = this.firstOrNull { it.width == preferredWidth }
    if (exactMatch != null) {
        return exactMatch
    }
    val largerImage = this.filter { it.width >= preferredWidth }.minByOrNull { it.width }
    if (largerImage != null) {
        return largerImage
    }
    return this.maxByOrNull { it.width }
}