package com.zrgenesiscloud.visioncue.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class APIResponse(
    val code: Int,
    val data: JsonElement,
    val message: String,
)