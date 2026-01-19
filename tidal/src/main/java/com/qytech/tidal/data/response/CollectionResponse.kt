package com.qytech.tidal.data.response

import com.qytech.tidal.data.model.IncludedItem
import com.qytech.tidal.data.model.Links
import com.qytech.tidal.data.model.MinimalistResources

data class CollectionResponse(
    val data: List<MinimalistResources>,
    val included: List<IncludedItem>?,
    val links: Links
)