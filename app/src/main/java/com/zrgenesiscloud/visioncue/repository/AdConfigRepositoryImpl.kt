package com.zrgenesiscloud.visioncue.repository

import android.content.Context
import com.zrgenesiscloud.visioncue.model.AdConfig
import com.zrgenesiscloud.visioncue.network.AdConfigService
import com.zrgenesiscloud.visioncue.network.NetworkModule
import com.zrgenesiscloud.visioncue.util.JsonElementParser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class AdConfigRepositoryImpl(
    private val context: Context,
    private val apiService: AdConfigService = NetworkModule.createService()
) : AdConfigRepository {
    
    override suspend fun getAdEnable(): Result<Boolean> {
        return try {
            val response = apiService.getAdEnabled()
            if (response.code == 200) {
                val enabled = JsonElementParser.parseBoolean(response.data)
                if (enabled != null) {
                    Result.success(enabled)
                } else {
                    Result.failure(Exception("Failed to parse ad enable status"))
                }
            } else {
                Result.failure(Exception("API Error: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAdConfig(): Result<AdConfig> {
        return try {
            val response = apiService.getAdConfig("visioncue")
            if (response.code == 200) {
                val config = parseAdConfigFromResponse(response.data)
                if (config != null) {
                    Result.success(config)
                } else {
                    Result.failure(Exception("Failed to parse ad config"))
                }
            } else {
                Result.failure(Exception("API Error: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseAdConfigFromResponse(data: JsonElement): AdConfig? {
        return when (data) {
            is JsonObject -> {
                try {
                    Json.decodeFromJsonElement(AdConfig.serializer(), data)
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
    }
} 