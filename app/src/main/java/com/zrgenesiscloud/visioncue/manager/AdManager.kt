package com.zrgenesiscloud.visioncue.manager

import android.util.Log
import com.zrgenesiscloud.visioncue.model.AdConfig
import com.zrgenesiscloud.visioncue.repository.AdConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AdManager {
    
    private const val TAG = "AdManager"
    
    private lateinit var adConfigRepository: AdConfigRepository
    
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // State management
    private val _adConfig = MutableStateFlow<AdConfig?>(null)
    val adConfig: StateFlow<AdConfig?> = _adConfig.asStateFlow()
    
    private val _isAdEnabled = MutableStateFlow<Boolean?>(null)
    val isAdEnabled: StateFlow<Boolean?> = _isAdEnabled.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Initialize the manager with repository
    fun init(adConfigRepository: AdConfigRepository) {
        this.adConfigRepository = adConfigRepository
        
        Log.d(TAG, "AdManager initialized")
        
        // Automatically load data when initialized
        refreshAll()
    }
    
    // Check if initialized
    private fun checkInitialized() {
        if (!::adConfigRepository.isInitialized) {
            throw IllegalStateException("AdManager not initialized. Call AdManager.init(repository) first.")
        }
    }
    
    // Check if ads are enabled
    fun checkAdEnable() {
        checkInitialized()
        scope.launch {
            try {
                val result = adConfigRepository.getAdEnable()
                result.fold(
                    onSuccess = { enabled ->
                        _isAdEnabled.value = enabled
                        Log.d(TAG, "Ads enabled: $enabled")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        Log.e(TAG, "Failed to check ad enable status", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
                Log.e(TAG, "Error checking ad enable status", e)
            }
        }
    }
    
    // Load ad configuration
    fun loadAdConfig() {
        checkInitialized()
        scope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = adConfigRepository.getAdConfig()
                result.fold(
                    onSuccess = { config ->
                        _adConfig.value = config
                        Log.d(TAG, "Ad config loaded successfully: ${config.app_name}")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        Log.e(TAG, "Failed to load ad config", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
                Log.e(TAG, "Error loading ad config", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Get ad code IDs by type
    fun getInterstitialAdIds(): List<String> {
        return _adConfig.value?.ad_data?.interstitial ?: emptyList()
    }
    
    fun getBannerAdIds(): List<String> {
        return _adConfig.value?.ad_data?.banner ?: emptyList()
    }
    
    fun getSplashAdIds(): List<String> {
        return _adConfig.value?.ad_data?.splash ?: emptyList()
    }
    
    // Get Pangle App ID
    fun getPangleAppId(): String? {
        return _adConfig.value?.pangle_app_id
    }
    
    // Get App Name
    fun getAppName(): String? {
        return _adConfig.value?.app_name
    }
    
    // Check if ads should be displayed
    fun shouldShowAds(): Boolean {
        return _isAdEnabled.value == true
    }
    
    // Get a random ad ID for a specific type
    fun getRandomAdId(type: String): String? {
        val adIds = when (type.lowercase()) {
            "interstitial" -> getInterstitialAdIds()
            "banner" -> getBannerAdIds()
            "splash" -> getSplashAdIds()
            else -> emptyList()
        }
        
        return if (adIds.isNotEmpty()) {
            adIds.random()
        } else {
            null
        }
    }
    
    // Get specific ad ID by index
    fun getAdId(type: String, index: Int = 0): String? {
        val adIds = when (type.lowercase()) {
            "interstitial" -> getInterstitialAdIds()
            "banner" -> getBannerAdIds()
            "splash" -> getSplashAdIds()
            else -> emptyList()
        }
        
        return if (adIds.isNotEmpty() && index < adIds.size) {
            adIds[index]
        } else {
            null
        }
    }
    
    // Refresh both ad enable status and config
    fun refreshAll() {
        checkAdEnable()
        loadAdConfig()
    }
    
    // Clear error state
    fun clearError() {
        _error.value = null
    }
    
    // Get configuration summary
    fun getConfigSummary(): String {
        val config = _adConfig.value
        val enabled = _isAdEnabled.value
        
        return buildString {
            append("Ads Enabled: ${enabled ?: "Unknown"}")
            if (config != null) {
                append(", App: ${config.app_name}")
                append(", Pangle ID: ${config.pangle_app_id}")
                append(", Interstitial: ${config.ad_data.interstitial.size}")
                append(", Banner: ${config.ad_data.banner.size}")
                append(", Splash: ${config.ad_data.splash.size}")
            }
        }
    }
    
    // Debug information
    fun getDebugInfo(): String {
        return buildString {
            appendLine("=== AdManager Debug Info ===")
            appendLine("Initialized: ${::adConfigRepository.isInitialized}")
            appendLine("Config Summary: ${getConfigSummary()}")
            appendLine("Loading: ${_isLoading.value}")
            appendLine("Error: ${_error.value ?: "None"}")
            appendLine("==========================")
        }
    }
} 