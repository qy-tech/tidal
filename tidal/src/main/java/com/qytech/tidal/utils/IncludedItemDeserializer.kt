package com.qytech.tidal.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.qytech.tidal.data.ResourceType
import com.qytech.tidal.data.model.AlbumResource
import com.qytech.tidal.data.model.ArtistResource
import com.qytech.tidal.data.model.ArtworkResource
import com.qytech.tidal.data.model.IncludedItem
import com.qytech.tidal.data.model.PlaylistResource
import com.qytech.tidal.data.model.TrackResource
import java.lang.reflect.Type

class IncludedItemDeserializer: JsonDeserializer<IncludedItem> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): IncludedItem {
        val jsonObject = json.asJsonObject
        val concreteType: Type = when (val type = jsonObject.get("type").asString) {
            ResourceType.TRACK.type -> object : TypeToken<TrackResource>() {}.type
            ResourceType.ARTWORK.type -> object : TypeToken<ArtworkResource>() {}.type
            ResourceType.ARTIST.type -> object : TypeToken<ArtistResource>() {}.type
            ResourceType.ALBUM.type -> object : TypeToken<AlbumResource>() {}.type
            ResourceType.PLAYLIST.type -> object : TypeToken<PlaylistResource>() {}.type
            else -> throw IllegalArgumentException("Unknown type: $type")
        }

        return context.deserialize(jsonObject, concreteType)
    }
}