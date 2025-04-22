package com.zrgenesis.teleprompter.repository

import com.zrgenesis.teleprompter.model.Script
import kotlinx.coroutines.flow.Flow

interface ScriptRepository {
    suspend fun getAllScripts(): Flow<List<Script>>
    suspend fun getScript(id: String): Script?
    suspend fun saveScript(script: Script)
    suspend fun deleteScript(id: String)
    suspend fun importScript(title: String, content: String): Script
} 