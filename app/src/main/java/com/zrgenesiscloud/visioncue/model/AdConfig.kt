package com.zrgenesiscloud.visioncue.model

import kotlinx.serialization.Serializable

@Serializable
data class AdConfig(
    val app_name: String,
    val pangle_app_id: String,
    val ad_data: AdData
)

@Serializable
data class AdData(
    val interstitial: List<String> = emptyList(),
    val banner: List<String> = emptyList(),
    val splash: List<String> = emptyList()
)

// Simple response for ad enable check
@Serializable
data class AdEnableResponse(
    val enabled: Boolean
)