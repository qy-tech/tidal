package com.qytech.tidal.data.response

import com.qytech.tidal.data.model.IncludedItem
import com.qytech.tidal.data.model.Links
import com.qytech.tidal.data.model.UserResource

data class UserInfoResponse(
    val data: UserResource,
)