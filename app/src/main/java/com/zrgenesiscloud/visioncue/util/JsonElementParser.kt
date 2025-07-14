package com.zrgenesiscloud.visioncue.util

import kotlinx.serialization.json.*
import com.zrgenesiscloud.visioncue.model.AdConfig
import com.zrgenesiscloud.visioncue.model.Script

object JsonElementParser {
    
    // Parse JsonElement to String
    fun parseString(element: JsonElement): String? {
        return when (element) {
            is JsonPrimitive -> {
                if (element.isString) element.content else null
            }
            else -> null
        }
    }
    
    // Parse JsonElement to Int
    fun parseInt(element: JsonElement): Int? {
        return when (element) {
            is JsonPrimitive -> element.intOrNull
            else -> null
        }
    }
    
    // Parse JsonElement to Boolean
    fun parseBoolean(element: JsonElement): Boolean? {
        return when (element) {
            is JsonPrimitive -> element.booleanOrNull
            else -> null
        }
    }
    
    // Parse JsonElement to Double
    fun parseDouble(element: JsonElement): Double? {
        return when (element) {
            is JsonPrimitive -> element.doubleOrNull
            else -> null
        }
    }
    
    // Parse JsonElement to Map<String, Any>
    fun parseObject(element: JsonElement): Map<String, Any>? {
        return when (element) {
            is JsonObject -> element.toMap()
            else -> null
        }
    }
    
    // Parse JsonElement to List<Any>
    fun parseArray(element: JsonElement): List<Any>? {
        return when (element) {
            is JsonArray -> element.toList()
            else -> null
        }
    }
    
    // Parse JsonElement to AdConfig
    fun parseAdConfig(element: JsonElement): AdConfig? {
        return when (element) {
            is JsonObject -> {
                try {
                    Json.decodeFromJsonElement(AdConfig.serializer(), element)
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
    }

    
    // Generic method to parse any JsonElement
    fun parseAny(element: JsonElement): Any? {
        return when (element) {
            is JsonPrimitive -> {
                when {
                    element.isString -> element.content
                    element.booleanOrNull != null -> element.boolean
                    element.intOrNull != null -> element.int
                    element.longOrNull != null -> element.long
                    element.doubleOrNull != null -> element.double
                    else -> element.content
                }
            }
            is JsonObject -> element.toMap()
            is JsonArray -> element.toList()
        }
    }
} 