package com.zrgenesiscloud.visioncue.network

import com.zrgenesiscloud.visioncue.model.APIResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AdConfigService {
    @GET("/api/v1/ad/ad_enabled")
    suspend fun getAdEnabled(): APIResponse

    @GET("/api/v1/ad/ad_config")
    suspend fun getAdConfig(@Query("app") app: String): APIResponse
}