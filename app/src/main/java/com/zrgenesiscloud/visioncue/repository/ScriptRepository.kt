package com.zrgenesiscloud.visioncue.repository

import com.zrgenesiscloud.visioncue.model.Script
import kotlinx.coroutines.flow.Flow

interface ScriptRepository {
    suspend fun getAllScripts(): Flow<List<Script>>
    suspend fun getScript(id: String): Script?
    suspend fun saveScript(script: Script)
    suspend fun deleteScript(id: String)
    suspend fun importScript(title: String, content: String): Script
} 