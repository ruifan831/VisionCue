package com.zrgenesiscloud.visioncue.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.core.os.ConfigurationCompat
import java.util.*

class LocaleManager(private val context: Context) {
    
    companion object {
        private const val SELECTED_LANGUAGE = "selected_language"
    }
    
    fun getLocale(): Locale {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString(SELECTED_LANGUAGE, null)
        
        return if (language != null) {
            Locale(language)
        } else {
            // Return current locale if no preference saved
            ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0) ?: Locale.getDefault()
        }
    }
    
    fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val resources = context.resources
        val config = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        resources.updateConfiguration(config, resources.displayMetrics)
        
        // Save the language preference
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(SELECTED_LANGUAGE, languageCode)
            apply()
        }
    }
}
