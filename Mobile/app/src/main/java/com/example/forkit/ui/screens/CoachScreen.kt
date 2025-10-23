package com.example.forkit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.forkit.CoachMainScreen
import com.example.forkit.CalorieDetailScreen
import com.example.forkit.WaterDetailScreen
import com.example.forkit.WorkoutDetailScreen
import com.example.forkit.utils.NetworkConnectivityManager

@Composable
fun CoachScreen(userId: String) {
    var currentScreen by remember { mutableStateOf("main") } // main, calories, water, workouts
    
    // Network connectivity monitoring
    val context = LocalContext.current
    val networkManager = remember { NetworkConnectivityManager(context) }
    var isOnline by remember { mutableStateOf(networkManager.isOnline()) }
    
    // Observe connectivity changes
    LaunchedEffect(Unit) {
        networkManager.observeConnectivity().collect { online ->
            isOnline = online
        }
    }
    
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
                isOnline = isOnline,
                onNavigateToCalories = { if (isOnline) currentScreen = "calories" },
                onNavigateToWater = { if (isOnline) currentScreen = "water" },
                onNavigateToWorkouts = { if (isOnline) currentScreen = "workouts" }
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

