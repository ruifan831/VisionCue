package com.zrgenesis.teleprompter.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

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

expect fun generateUUID(): String 