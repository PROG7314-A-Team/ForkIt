package com.example.forkit

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    SYSTEM,  // Follow device setting
    LIGHT,   // Always light
    DARK     // Always dark
}

object ThemeManager {
    private var _themeMode by mutableStateOf(ThemeMode.SYSTEM)
    val themeMode: ThemeMode get() = _themeMode
    
    private const val PREFS_NAME = "theme_preferences"
    private const val KEY_THEME_MODE = "theme_mode"
    
    // Returns the actual dark mode state based on theme mode and system preference
    fun isDarkMode(isSystemInDarkTheme: Boolean): Boolean {
        return when (_themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
    }
    
    fun setThemeMode(mode: ThemeMode) {
        _themeMode = mode
    }
    
    fun saveThemeMode(context: Context, mode: ThemeMode) {
        _themeMode = mode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
    }
    
    fun loadThemeMode(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedMode = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        _themeMode = try {
            ThemeMode.valueOf(savedMode ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
    
    // Legacy support for existing code
    @Deprecated("Use isDarkMode(isSystemInDarkTheme) instead")
    val isDarkMode: Boolean get() = _themeMode == ThemeMode.DARK
    
    @Deprecated("Use setThemeMode() instead")
    fun toggleDarkMode() {
        _themeMode = when (_themeMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
    }
    
    @Deprecated("Use setThemeMode() instead")
    fun setDarkMode(isDark: Boolean) {
        _themeMode = if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
    }
}
