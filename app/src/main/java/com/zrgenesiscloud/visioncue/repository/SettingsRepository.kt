package com.zrgenesiscloud.visioncue.repository

import com.zrgenesiscloud.visioncue.model.TeleprompterSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<TeleprompterSettings>
    suspend fun updateSettings(settings: TeleprompterSettings)
    suspend fun resetToDefaults()
} 