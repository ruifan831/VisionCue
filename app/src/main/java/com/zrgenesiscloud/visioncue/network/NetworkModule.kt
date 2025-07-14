package com.zrgenesiscloud.visioncue.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit
import com.zrgenesiscloud.visioncue.BuildConfig

object NetworkModule {
    
    // Use BuildConfig for build-specific configuration with fallback
    private val BASE_URL = try {
        BuildConfig.API_BASE_URL
    } catch (e: Exception) {
        // Fallback if BuildConfig fields are not available
        if (BuildConfig.DEBUG) {
            "http://10.0.2.2:8000/"
        } else {
            "https://api.visioncue.com/"
        }
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = try {
            if (BuildConfig.ENABLE_LOGGING) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        } catch (e: Exception) {
            // Fallback logging configuration
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(AuthInterceptor())
        .connectTimeout(if (BuildConfig.DEBUG) 10 else 30, TimeUnit.SECONDS)
        .readTimeout(if (BuildConfig.DEBUG) 10 else 30, TimeUnit.SECONDS)
        .writeTimeout(if (BuildConfig.DEBUG) 10 else 30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
    
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
    
    inline fun <reified T> createService(): T {
        return createService(T::class.java)
    }
    
    // Helper methods for debugging
    fun getCurrentBaseUrl(): String = BASE_URL
    fun isDebugBuild(): Boolean = BuildConfig.DEBUG
} 