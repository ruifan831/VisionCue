package com.zrgenesis.teleprompter.repository

import com.zrgenesis.teleprompter.model.TeleprompterSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<TeleprompterSettings>
    suspend fun updateSettings(settings: TeleprompterSettings)
    suspend fun resetToDefaults()
} 