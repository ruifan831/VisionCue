package com.zrgenesiscloud.visioncue.util

import android.util.Log
import com.zrgenesiscloud.visioncue.BuildConfig

object BuildConfigTest {
    
    fun logBuildConfiguration() {
        Log.d("BuildConfig", "=== Build Configuration ===")
        Log.d("BuildConfig", "Build Type: ${BuildConfig.BUILD_TYPE}")
        Log.d("BuildConfig", "Is Debug: ${BuildConfig.DEBUG}")
        Log.d("BuildConfig", "API Base URL: ${BuildConfig.API_BASE_URL}")
        Log.d("BuildConfig", "Enable Logging: ${BuildConfig.ENABLE_LOGGING}")
        Log.d("BuildConfig", "Application ID: ${BuildConfig.APPLICATION_ID}")
        Log.d("BuildConfig", "Version Name: ${BuildConfig.VERSION_NAME}")
        Log.d("BuildConfig", "Version Code: ${BuildConfig.VERSION_CODE}")
        Log.d("BuildConfig", "========================")
    }
} 