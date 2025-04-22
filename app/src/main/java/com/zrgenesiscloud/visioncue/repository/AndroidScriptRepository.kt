package com.zrgenesiscloud.visioncue.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zrgenesis.teleprompter.model.Script
import com.zrgenesis.teleprompter.repository.ScriptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "AndroidScriptRepository"
private val Context.scriptDataStore: DataStore<Preferences> by preferencesDataStore(name = "scripts")
private val SCRIPTS_KEY = stringPreferencesKey("saved_scripts")

class AndroidScriptRepository(private val context: Context) : ScriptRepository {
    
    override suspend fun getAllScripts(): Flow<List<Script>> {
        return context.scriptDataStore.data.map { preferences ->
            val scriptsJson = preferences[SCRIPTS_KEY] ?: "[]"
            try {
                Log.d(TAG, "Loading all scripts, JSON length: ${scriptsJson.length}")
                Json.decodeFromString<List<Script>>(scriptsJson).also {
                    Log.d(TAG, "Loaded ${it.size} scripts")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error decoding scripts JSON", e)
                emptyList()
            }
        }
    }
    
    override suspend fun getScript(id: String): Script? {
        Log.d(TAG, "Getting script with ID: $id")
        try {
            val scriptsJson = context.scriptDataStore.data.map { preferences ->
                preferences[SCRIPTS_KEY] ?: "[]"
            }.firstOrNull() ?: "[]"
            
            Log.d(TAG, "Scripts JSON for getScript: ${scriptsJson.take(100)}...")
            
            val scripts = Json.decodeFromString<List<Script>>(scriptsJson)
            Log.d(TAG, "Parsed ${scripts.size} scripts")
            
            val script = scripts.find { it.id == id }
            if (script != null) {
                Log.d(TAG, "Found script: ${script.title}")
            } else {
                Log.d(TAG, "Script with ID $id not found")
            }
            return script
        } catch (e: Exception) {
            Log.e(TAG, "Error in getScript", e)
            return null
        }
    }
    
    override suspend fun saveScript(script: Script) {
        Log.d(TAG, "Saving script: ${script.id} - ${script.title}")
        try {
            context.scriptDataStore.edit { preferences ->
                val currentScriptsJson = preferences[SCRIPTS_KEY] ?: "[]"
                Log.d(TAG, "Current scripts JSON length: ${currentScriptsJson.length}")
                
                val currentScripts = try {
                    Json.decodeFromString<List<Script>>(currentScriptsJson)
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding current scripts", e)
                    emptyList()
                }
                
                Log.d(TAG, "Current scripts count: ${currentScripts.size}")
                
                // Update existing script or add new one
                val updatedScripts = currentScripts.filter { it.id != script.id } + script
                Log.d(TAG, "Updated scripts count: ${updatedScripts.size}")
                
                val newJson = Json.encodeToString(updatedScripts)
                Log.d(TAG, "New JSON length: ${newJson.length}")
                
                preferences[SCRIPTS_KEY] = newJson
            }
            Log.d(TAG, "Script saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving script", e)
        }
    }
    
    override suspend fun deleteScript(id: String) {
        context.scriptDataStore.edit { preferences ->
            val currentScriptsJson = preferences[SCRIPTS_KEY] ?: "[]"
            val currentScripts = try {
                Json.decodeFromString<List<Script>>(currentScriptsJson)
            } catch (e: Exception) {
                emptyList()
            }
            
            val updatedScripts = currentScripts.filter { it.id != id }
            preferences[SCRIPTS_KEY] = Json.encodeToString(updatedScripts)
        }
    }
    
    override suspend fun importScript(title: String, content: String): Script {
        val script = Script(
            title = title,
            content = content
        )
        saveScript(script)
        return script
    }
} 