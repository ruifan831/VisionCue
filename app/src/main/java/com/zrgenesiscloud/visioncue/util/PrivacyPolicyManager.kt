package com.zrgenesiscloud.visioncue.util

import android.content.Context
import android.content.SharedPreferences

class PrivacyPolicyManager(private val context: Context) {
    
    companion object {
        private const val PRIVACY_PREFS = "privacy_prefs"
        private const val KEY_PRIVACY_ACCEPTED = "privacy_policy_accepted"
    }
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PRIVACY_PREFS, Context.MODE_PRIVATE)
    }
    
    fun hasAcceptedPrivacyPolicy(): Boolean {
        return sharedPreferences.getBoolean(KEY_PRIVACY_ACCEPTED, false)
    }
    
    fun setPrivacyPolicyAccepted(accepted: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_PRIVACY_ACCEPTED, accepted).apply()
    }
} 