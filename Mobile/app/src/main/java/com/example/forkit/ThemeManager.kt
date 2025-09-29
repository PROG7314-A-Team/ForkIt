package com.example.forkit

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

object ThemeManager {
    private var _isDarkMode by mutableStateOf(false)
    val isDarkMode: Boolean get() = _isDarkMode
    
    fun toggleDarkMode() {
        _isDarkMode = !_isDarkMode
    }
    
    fun setDarkMode(isDark: Boolean) {
        _isDarkMode = isDark
    }
    
    // Dark mode colors
    val darkBackground = Color(0xFF121212)
    val darkSurface = Color(0xFF1E1E1E)
    val darkOnBackground = Color(0xFFE0E0E0)
    val darkOnSurface = Color(0xFFE0E0E0)
    val darkCard = Color(0xFF424242) // Grey cards in dark mode
    val darkBorder = Color(0xFF404040)
    
    // Light mode colors (existing)
    val lightBackground = Color.White
    val lightSurface = Color.White
    val lightOnBackground = Color.Black
    val lightOnSurface = Color.Black
    val lightCard = Color.White
    val lightBorder = Color(0xFFE0E0E0)
    
    // ForkIt brand colors (same for both modes)
    val forkItGreen = Color(0xFF22B27D)
    val forkItBlue = Color(0xFF1E9ECD)
    
    // Dynamic colors based on current theme
    val backgroundColor: Color get() = if (isDarkMode) darkBackground else lightBackground
    val surfaceColor: Color get() = if (isDarkMode) darkSurface else lightSurface
    val onBackgroundColor: Color get() = if (isDarkMode) darkOnBackground else lightOnBackground
    val onSurfaceColor: Color get() = if (isDarkMode) darkOnSurface else lightOnSurface
    val cardColor: Color get() = if (isDarkMode) darkCard else lightCard
    val borderColor: Color get() = if (isDarkMode) darkBorder else lightBorder
}
