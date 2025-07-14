package com.zrgenesiscloud.visioncue

import android.app.Application
import com.zrgenesiscloud.visioncue.manager.AdManager
import com.zrgenesiscloud.visioncue.repository.AdConfigRepositoryImpl

class VisionCueApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize AdManager at application level
        val adConfigRepository = AdConfigRepositoryImpl(this)
        AdManager.init(adConfigRepository)
    }
} 