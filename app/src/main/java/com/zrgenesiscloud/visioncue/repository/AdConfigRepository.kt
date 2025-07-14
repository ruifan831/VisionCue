package com.zrgenesiscloud.visioncue.repository

import com.zrgenesiscloud.visioncue.model.AdConfig

interface AdConfigRepository {
    // Check if ads are enabled
    suspend fun getAdEnable(): Result<Boolean>
    
    // Get ad configuration
    suspend fun getAdConfig(): Result<AdConfig>
}