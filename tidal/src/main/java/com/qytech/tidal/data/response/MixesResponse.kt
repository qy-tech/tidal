package com.qytech.tidal.data.response

import com.qytech.tidal.data.model.IncludedItem
import com.qytech.tidal.data.model.MinimalistResources

data class MixesResponse(
    val data: List<MinimalistResources>,
    val included: List<IncludedItem>,
)