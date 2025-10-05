package com.example.forkit.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.forkit.data.RetrofitClient
import com.example.forkit.ui.meals.MealsScreen
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import android.util.Log

/**
 * 🧠 DashboardScreen.kt
 *
 * This file contains the full composable layout and logic for the ForkIt Dashboard.
 * - Manages tabs (Home, Meals, Habits, Coach)
 * - Fetches all daily user data (meals, water, workouts)
 * - Handles refresh, step tracking, and navigation
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    userId: String = "",
    initialSelectedTab: Int = 0,
    onRefreshCallbackSet: ((() -> Unit) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedTab by remember { mutableStateOf(initialSelectedTab) }

    // 🔢 API and state variables
    var consumed by remember { mutableStateOf(0.0) }
    var burned by remember { mutableStateOf(0.0) }
    var waterAmount by remember { mutableStateOf(0.0) }
    var recentMeals by remember { mutableStateOf(emptyList<com.example.forkit.data.models.RecentActivityEntry>()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // 🧩 Helper state
    var showFloatingIcons by remember { mutableStateOf(false) }

    // 🧭 Refresh logic (simplified placeholder)
    val refreshData: () -> Unit = {
        scope.launch {
            try {
                Log.d("DashboardScreen", "📊 Refreshing dashboard data for userId=$userId")
                isRefreshing = true
                val response = RetrofitClient.api.getDailyWaterTotal(userId, "2025-10-05")
                if (response.isSuccessful) {
                    waterAmount = response.body()?.data?.totalAmount ?: 0.0
                    Log.d("DashboardScreen", "💧 Water total fetched: $waterAmount ml")
                }
            } catch (e: Exception) {
                Log.e("DashboardScreen", "❌ Error fetching data: ${e.message}", e)
            } finally {
                isRefreshing = false
                isLoading = false
            }
        }
    }


    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { Log.d("DashboardScreen", "🔁 Manual pull-to-refresh"); refreshData() }
    )


    // 📡 Lifecycle-based refresh
    LaunchedEffect(Unit) {
        Log.d("DashboardScreen", "🚀 Initial launch - loading data")
        refreshData()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d("DashboardScreen", "🔄 Activity resumed - refreshing data")
                refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 💾 Allow parent activity to trigger refresh
    LaunchedEffect(Unit) {
        onRefreshCallbackSet?.invoke(refreshData)
    }

    // 🧱 Main UI layout
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
                .statusBarsPadding()
        ) {
            when (selectedTab) {
                0 -> {
                    // 🏠 Home Tab
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Welcome to ForkIt Dashboard 🍽️",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                1 -> {
                    // 🥗 Meals Tab (now embedded, bottom nav stays visible)
                    Box(modifier = Modifier.weight(1f)) {
                        MealsScreen(
                            userId = userId,
                            onMealSelected = { meal ->
                                Log.d("DashboardScreen", "🍴 Selected meal: ${meal.name}")
                                Toast.makeText(context, "Opening ${meal.name}", Toast.LENGTH_SHORT).show()
                            },
                            onAddMealClicked = {
                                Log.d("DashboardScreen", "➕ Add meal clicked")
                                Toast.makeText(context, "Opening Add Meal...", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    Log.d("DashboardScreen", "✅ Meals tab rendered successfully")
                }

                3 -> {
                    // 🧘 Habits placeholder
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("Habits Coming Soon 🕒")
                    }
                }

                4 -> {
                    // 🧠 Coach placeholder
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("Coach Screen Coming Soon 🚀")
                    }
                }
            }

            // 🧭 Bottom Navigation bar (always visible)
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                showFloatingIcons = showFloatingIcons,
                onAddButtonClick = { showFloatingIcons = true }
            )
        }

        // Pull-to-refresh indicator
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 56.dp)
        )

        // Floating overlay
        if (showFloatingIcons) {
            FloatingIcons(
                context = context,
                userId = userId,
                onDismiss = { showFloatingIcons = false }
            )
        }
    }
}
