package com.example.forkit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.forkit.CoachMainScreen
import com.example.forkit.CalorieDetailScreen
import com.example.forkit.WaterDetailScreen
import com.example.forkit.WorkoutDetailScreen

@Composable
fun CoachScreen(userId: String) {
    var currentScreen by remember { mutableStateOf("main") } // main, calories, water, workouts
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Coach Content - switch based on current screen
        when (currentScreen) {
            "main" -> CoachMainScreen(
                userId = userId,
                modifier = Modifier.weight(1f),
                onNavigateToCalories = { currentScreen = "calories" },
                onNavigateToWater = { currentScreen = "water" },
                onNavigateToWorkouts = { currentScreen = "workouts" }
            )
            "calories" -> CalorieDetailScreen(
                userId = userId,
                modifier = Modifier.weight(1f),
                onBack = { currentScreen = "main" }
            )
            "water" -> WaterDetailScreen(
                userId = userId,
                modifier = Modifier.weight(1f),
                onBack = { currentScreen = "main" }
            )
            "workouts" -> WorkoutDetailScreen(
                userId = userId,
                modifier = Modifier.weight(1f),
                onBack = { currentScreen = "main" }
            )
        }
    }
}

