package com.example.forkit

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE_CODE = "language_code"
    
    // Supported languages
    enum class Language(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        AFRIKAANS("af", "Afrikaans"),
        ZULU("zu", "isiZulu")
    }
    
    var currentLanguage by mutableStateOf(Language.ENGLISH)
        private set
    
    /**
     * Initialize language from shared preferences
     */
    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(KEY_LANGUAGE_CODE, Language.ENGLISH.code) ?: Language.ENGLISH.code
        currentLanguage = Language.values().find { it.code == languageCode } ?: Language.ENGLISH
    }
    
    /**
     * Save and apply language preference
     */
    fun saveLanguage(context: Context, language: Language) {
        currentLanguage = language
        
        // Save to SharedPreferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE_CODE, language.code).apply()
        
        // Apply language to app
        setLocale(context, language.code)
    }
    
    /**
     * Get current language code
     */
    fun getCurrentLanguageCode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE_CODE, Language.ENGLISH.code) ?: Language.ENGLISH.code
    }
    
    /**
     * Set locale for the app
     */
    private fun setLocale(context: Context, languageCode: String) {
        val locales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(locales)
        // Keep currentLanguage in sync
        currentLanguage = Language.values().find { it.code == languageCode } ?: Language.ENGLISH
    }
    
    /**
     * Apply saved language on app start
     */
    fun applyLanguage(context: Context) {
        val languageCode = getCurrentLanguageCode(context)
        setLocale(context, languageCode)
        currentLanguage = Language.values().find { it.code == languageCode } ?: Language.ENGLISH
    }
}

