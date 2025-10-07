package com.example.forkit.utils

import android.content.Context
import android.content.SharedPreferences

class AuthPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "ForkItAuth"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ID_TOKEN = "id_token"
        private const val KEY_EMAIL = "email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
    }
    
    fun saveLoginData(userId: String, idToken: String, email: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_ID_TOKEN, idToken)
            putString(KEY_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
            apply()
        }
    }
    
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    
    fun getIdToken(): String? {
        return prefs.getString(KEY_ID_TOKEN, null)
    }
    
    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun getLoginTimestamp(): Long {
        return prefs.getLong(KEY_LOGIN_TIMESTAMP, 0)
    }
    
    fun clearLoginData() {
        prefs.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_ID_TOKEN)
            remove(KEY_EMAIL)
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_LOGIN_TIMESTAMP)
            apply()
        }
    }
    
    fun logout() {
        clearLoginData()
    }
    
    // Optional: Check if login is still valid (e.g., not expired)
    fun isLoginValid(): Boolean {
        if (!isLoggedIn()) return false
        
        val loginTime = getLoginTimestamp()
        val currentTime = System.currentTimeMillis()
        val thirtyDaysInMillis = 30 * 24 * 60 * 60 * 1000L // 30 days
        
        // Consider login invalid if it's older than 30 days
        return (currentTime - loginTime) < thirtyDaysInMillis
    }
}
