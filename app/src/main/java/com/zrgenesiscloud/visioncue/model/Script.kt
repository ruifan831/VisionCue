package com.zrgenesiscloud.visioncue.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

fun generateUUID(): String {
    return java.util.UUID.randomUUID().toString()
}
@Serializable
data class Script(
    val id: String = generateUUID(),
    val title: String,
    val content: String,
    val formattingData: String = "",
    val textAlignment: String = "Left",
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now()
)

