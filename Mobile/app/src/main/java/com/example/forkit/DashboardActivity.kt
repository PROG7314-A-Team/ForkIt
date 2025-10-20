package com.example.forkit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.ContextCompat
import androidx.health.connect.client.PermissionController
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.forkit.data.RetrofitClient
import com.example.forkit.ui.theme.ForkItTheme
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.ui.meals.*
import com.example.forkit.data.models.MealLog
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.TextStyle
import com.example.forkit.ui.meals.AddFullMealActivity
import com.example.forkit.ui.meals.MealDetailActivity
import android.util.Log
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer
import com.example.forkit.ui.screens.HomeScreen
import com.example.forkit.ui.screens.MealsScreen
import com.example.forkit.ui.screens.HabitsScreen
import com.example.forkit.ui.screens.CoachScreen




class DashboardActivity : ComponentActivity() {
    private var refreshCallback: (() -> Unit)? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Apply saved language
        LanguageManager.applyLanguage(this)
        
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val initialTab = intent.getIntExtra("SELECTED_TAB", 0)
        
        setContent {
            ForkItTheme {
                DashboardScreen(
                    userId = userId,
                    initialSelectedTab = initialTab,
                    onRefreshCallbackSet = { callback -> refreshCallback = callback }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when activity resumes (e.g., returning from AddMealActivity)
        refreshCallback?.invoke()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    userId: String = "",
    initialSelectedTab: Int = 0,
    onRefreshCallbackSet: ((() -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var selectedTab by remember { mutableStateOf(initialSelectedTab) }
    
    // State variables for API data
    var consumed by remember { mutableStateOf(0.0) }
    var burned by remember { mutableStateOf(0.0) }
    var carbsCalories by remember { mutableStateOf(0.0) }
    var proteinCalories by remember { mutableStateOf(0.0) }
    var fatCalories by remember { mutableStateOf(0.0) }
    var waterAmount by remember { mutableStateOf(0.0) }
    var waterEntries by remember { mutableStateOf(0) }
    var recentMeals by remember { mutableStateOf<List<com.example.forkit.data.models.RecentActivityEntry>>(emptyList()) }
    var recentWorkouts by remember { mutableStateOf<List<com.example.forkit.data.models.RecentExerciseActivityEntry>>(emptyList()) }
    var recentWaterLogs by remember { mutableStateOf<List<com.example.forkit.data.models.RecentWaterActivityEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // User goals - fetched from API
    var dailyGoal by remember { mutableStateOf(2000) }
    var dailyWaterGoal by remember { mutableStateOf(2000) }
    var dailyStepsGoal by remember { mutableStateOf(8000) }
    var weeklyExercisesGoal by remember { mutableStateOf(3) }
    
    // Step tracking state
    var currentSteps by remember { mutableStateOf(0) }
    var isStepTrackingAvailable by remember { mutableStateOf(false) }
    var stepTracker by remember { mutableStateOf<StepTracker?>(null) }
    
    // Calculate totals
    val total = (consumed - burned).toInt()
    
    // Progress bar calculations - treated as budget (remaining calories)
    val progressPercentage = (consumed.toFloat() / dailyGoal).coerceIn(0f, 1f)
    val caloriesRemaining = (dailyGoal - consumed).toInt()
    val isOverBudget = consumed > dailyGoal
    val isWithinBudget = consumed <= dailyGoal && consumed > 0
    
    // Animated progress for smooth transitions
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercentage,
        animationSpec = tween(1000)
    )
    
    // Total macronutrient calories
    val totalCalories = (carbsCalories + proteinCalories + fatCalories).toInt()
    
    // State for showing floating icons overlay
    var showFloatingIcons by remember { mutableStateOf(false) }
    
    // Get today's date in YYYY-MM-DD format
    val todayDate = remember {
        val calendar = java.util.Calendar.getInstance()
        String.format("%04d-%02d-%02d", 
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH) + 1,
            calendar.get(java.util.Calendar.DAY_OF_MONTH))
    }
    
    // Health Connect permission launcher
    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        android.util.Log.d("DashboardActivity", "Health Connect permissions granted: $granted")
        if (granted.isNotEmpty()) {
            // Permissions granted, fetch steps
            scope.launch {
                val steps = stepTracker?.fetchTodaySteps() ?: 0
                currentSteps = steps
            }
        }
    }
    
    // Activity Recognition permission launcher (for Android 10+)
    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        android.util.Log.d("DashboardActivity", "Activity Recognition permission granted: $isGranted")
        if (isGranted) {
            // Permission granted, start sensor tracking
            stepTracker?.startSensorTracking()
            scope.launch {
                val steps = stepTracker?.fetchTodaySteps() ?: 0
                currentSteps = steps
            }
        }
    }
    
    // Initialize step tracker
    LaunchedEffect(Unit) {
        stepTracker = StepTracker(context)
        isStepTrackingAvailable = stepTracker?.isHealthConnectAvailable() == true || 
                                   stepTracker?.isSensorAvailable() == true
        
        // Request permissions and fetch steps
        if (stepTracker?.isHealthConnectAvailable() == true) {
            // Try Health Connect first
            if (stepTracker?.hasHealthConnectPermissions() == false) {
                // Request Health Connect permissions
                val permissions = stepTracker?.getHealthConnectPermissions() ?: emptySet()
                healthConnectPermissionLauncher.launch(permissions)
            } else {
                // Already have permissions, fetch steps
                currentSteps = stepTracker?.fetchTodaySteps() ?: 0
            }
        } else if (stepTracker?.isSensorAvailable() == true) {
            // Use sensor fallback
            // Check if we need to request ACTIVITY_RECOGNITION permission (Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                } else {
                    stepTracker?.startSensorTracking()
                    currentSteps = stepTracker?.fetchTodaySteps() ?: 0
                }
            } else {
                // No permission needed for Android 9 and below
                stepTracker?.startSensorTracking()
                currentSteps = stepTracker?.fetchTodaySteps() ?: 0
            }
        }
    }
    
    // Observe step count changes from sensor
    LaunchedEffect(stepTracker) {
        stepTracker?.stepCount?.collect { steps ->
            currentSteps = steps
        }
    }
    
    // Cleanup step tracker on dispose
    DisposableEffect(Unit) {
        onDispose {
            stepTracker?.cleanup()
        }
    }
    
    // Refresh function to fetch all data
    val refreshData: () -> Unit = {
        scope.launch {
            if (userId.isNotEmpty()) {
                try {
                    isRefreshing = true
                    android.util.Log.d("DashboardActivity", "Refreshing data for userId: $userId on date: $todayDate")
                    
                    // Refresh step count
                    currentSteps = stepTracker?.fetchTodaySteps() ?: 0
                    
                    // **OPTIMIZED: Fetch all data in parallel using async**
                    android.util.Log.d("DashboardActivity", "Starting parallel API calls...")
                    val startTime = System.currentTimeMillis()
                    
                    // Launch all API calls simultaneously
                    val goalsDeferred = async {
                        try {
                            android.util.Log.d("DashboardActivity", "Fetching user goals...")
                            com.example.forkit.data.RetrofitClient.api.getUserGoals(userId)
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardActivity", "Error fetching goals: ${e.message}", e)
                            null
                        }
                    }
                    
                    val foodSummaryDeferred = async {
                        try {
                            com.example.forkit.data.RetrofitClient.api.getDailyCalorieSummary(userId, todayDate)
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardActivity", "Error fetching food summary: ${e.message}", e)
                            null
                        }
                    }
                    
                    val exerciseDeferred = async {
                        try {
                            com.example.forkit.data.RetrofitClient.api.getDailyExerciseTotal(userId, todayDate)
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardActivity", "Error fetching exercise: ${e.message}", e)
                            null
                        }
                    }
                    
                    val waterDeferred = async {
                        try {
                            com.example.forkit.data.RetrofitClient.api.getDailyWaterTotal(userId, todayDate)
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardActivity", "Error fetching water: ${e.message}", e)
                            null
                        }
                    }

                    // ─────────────────────────────────────────────
                    // Fetch both food logs and meal logs in parallel
                    // ─────────────────────────────────────────────
                    val foodLogsDeferred = async {
                        try {
                            com.example.forkit.data.RetrofitClient.api.getFoodLogs(userId, todayDate)
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardActivity", "Error fetching food logs: ${e.message}", e)
                            null
                        }
                    }

                    val mealLogsDeferred = async {
                        try {
                            com.example.forkit.data.RetrofitClient.api.getMealLogsByDateRange(
                                userId = userId,
                                startDate = todayDate,
                                endDate = todayDate
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardActivity", "Error fetching meal logs: ${e.message}", e)
                            null
                        }
                    }

                    
                    val exerciseLogsDeferred = async {
                        try {
                            com.example.forkit.data.RetrofitClient.api.getExerciseLogs(userId, todayDate)
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardActivity", "Error fetching exercise logs: ${e.message}", e)
                            null
                        }
                    }
                    
                    val waterLogsDeferred = async {
                        try {
                            com.example.forkit.data.RetrofitClient.api.getWaterLogs(userId, todayDate)
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardActivity", "Error fetching water logs: ${e.message}", e)
                            null
                        }
                    }
                    
                    // Wait for all API calls to complete in parallel
                    val goalsResponse = goalsDeferred.await()
                    val foodSummaryResponse = foodSummaryDeferred.await()
                    val exerciseResponse = exerciseDeferred.await()
                    val waterResponse = waterDeferred.await()
                    //val foodLogsResponse = foodLogsDeferred.await()
                    val exerciseLogsResponse = exerciseLogsDeferred.await()
                    val waterLogsResponse = waterLogsDeferred.await()
                    
                    val endTime = System.currentTimeMillis()
                    android.util.Log.d("DashboardActivity", "✅ All API calls completed in ${endTime - startTime}ms")
                    
                    // Process goals response
                    goalsResponse?.let { response ->
                        if (response.isSuccessful) {
                            val goals = response.body()?.data
                            dailyGoal = goals?.dailyCalories ?: 2000
                            dailyWaterGoal = goals?.dailyWater ?: 2000
                            dailyStepsGoal = goals?.dailySteps ?: 8000
                            weeklyExercisesGoal = goals?.weeklyExercises ?: 3
                            android.util.Log.d("DashboardActivity", "Goals loaded: Calories=$dailyGoal, Water=$dailyWaterGoal")
                        }
                    }
                    
                    // Process food summary response
                    foodSummaryResponse?.let { response ->
                        if (response.isSuccessful) {
                            val foodData = response.body()?.data
                            consumed = foodData?.totalCalories ?: 0.0
                            carbsCalories = (foodData?.totalCarbs ?: 0.0) * 4
                            proteinCalories = (foodData?.totalProtein ?: 0.0) * 4
                            fatCalories = (foodData?.totalFat ?: 0.0) * 9
                        }
                    }
                    
                    // Process exercise response
                    exerciseResponse?.let { response ->
                        if (response.isSuccessful) {
                            val exerciseData = response.body()?.data
                            burned = exerciseData?.totalCaloriesBurnt ?: 0.0
                        }
                    }
                    
                    // Process water response
                    waterResponse?.let { response ->
                        if (response.isSuccessful) {
                            val waterData = response.body()?.data
                            waterAmount = waterData?.totalAmount ?: 0.0
                            waterEntries = waterData?.entries ?: 0
                        }
                    }

// ─────────────────────────────────────────────
// Combine Food Logs + Meal Logs
// ─────────────────────────────────────────────
                    val foodLogsResponse = foodLogsDeferred.await()
                    val mealLogsResponse = mealLogsDeferred.await()

                    val combinedList = mutableListOf<com.example.forkit.data.models.RecentActivityEntry>()

// 🥣 Process individual food logs
                    foodLogsResponse?.let { response ->
                        if (response.isSuccessful) {
                            val foodLogs = response.body()?.data ?: emptyList()
                            combinedList += foodLogs.map { log ->
                                com.example.forkit.data.models.RecentActivityEntry(
                                    id = log.id,
                                    foodName = log.foodName,
                                    servingSize = log.servingSize,
                                    measuringUnit = log.measuringUnit,
                                    calories = log.calories.toInt(),
                                    mealType = log.mealType ?: "Food",
                                    date = log.date,
                                    createdAt = log.createdAt,
                                    time = log.createdAt.substring(11, 16)
                                )
                            }
                            android.util.Log.d("DashboardActivity", "✅ Added ${foodLogs.size} food logs")
                        }
                    }

                    // 🍱 Process full meal logs
                    mealLogsResponse?.let { response ->
                        if (response.isSuccessful) {
                            val mealLogs = response.body()?.data ?: emptyList()
                            combinedList += mealLogs.map { meal ->
                                com.example.forkit.data.models.RecentActivityEntry(
                                    id = meal.id,
                                    foodName = meal.name,
                                    servingSize = meal.ingredients.size.toDouble(),
                                    measuringUnit = "items",
                                    calories = meal.totalCalories.toInt(),
                                    mealType = meal.mealType ?: "Meal",
                                    date = meal.date,
                                    createdAt = meal.createdAt,
                                    time = meal.createdAt.substring(11, 16)
                                )
                            }
                            android.util.Log.d("DashboardActivity", "✅ Added ${mealLogs.size} meal logs")
                        }
                    }

                    // 🔹 Merge + sort
                    recentMeals = combinedList.sortedByDescending { it.createdAt }

                    android.util.Log.d("DashboardActivity", "🍽️ Total combined entries: ${recentMeals.size}")
                    // 🔹 Recalculate totals for combined data (food + meal logs)
                    if (combinedList.isNotEmpty()) {
                        // Sum up all calories from both food and meal logs
                        consumed = combinedList.sumOf { it.calories.toDouble() }

                        // 🧠 Approximate macro split (optional placeholder)
                        // These will make your pie chart work again
                        carbsCalories = consumed * 0.5   // ~50% from carbs
                        proteinCalories = consumed * 0.3 // ~30% from protein
                        fatCalories = consumed * 0.2     // ~20% from fat

                        Log.d("DashboardActivity", "🔥 Combined totals recalculated -> Consumed=$consumed kcal")
                    }

                    
                    // Process exercise logs response
                    exerciseLogsResponse?.let { response ->
                        if (response.isSuccessful) {
                            val todayExerciseLogs = response.body()?.data ?: emptyList()
                            recentWorkouts = todayExerciseLogs.map { log ->
                                com.example.forkit.data.models.RecentExerciseActivityEntry(
                                    id = log.id,
                                    name = log.name,
                                    type = log.type,
                                    caloriesBurnt = log.caloriesBurnt.toInt(),
                                    duration = log.duration?.toInt(),
                                    date = log.date,
                                    createdAt = log.createdAt,
                                    time = log.createdAt.substring(11, 16)
                                )
                            }.sortedByDescending { it.createdAt }
                            android.util.Log.d("DashboardActivity", "Workouts loaded: ${recentWorkouts.size} items")
                        }
                    }
                    
                    // Process water logs response
                    waterLogsResponse?.let { response ->
                        if (response.isSuccessful) {
                            val todayWaterLogs = response.body()?.data ?: emptyList()
                            recentWaterLogs = todayWaterLogs.map { log ->
                                com.example.forkit.data.models.RecentWaterActivityEntry(
                                    id = log.id,
                                    amount = log.amount.toInt(),
                                    date = log.date,
                                    createdAt = log.createdAt,
                                    time = log.createdAt.substring(11, 16)
                                )
                            }.sortedByDescending { it.createdAt }
                            android.util.Log.d("DashboardActivity", "Water logs loaded: ${recentWaterLogs.size} items")
                        }
                    }
                    
                } catch (e: Exception) {
                    android.util.Log.e("DashboardActivity", "Fatal error loading data: ${e.message}", e)
                    android.util.Log.e("DashboardActivity", "Error details: ${e.javaClass.simpleName}", e)
                    android.util.Log.e("DashboardActivity", "Stack trace: ${e.stackTrace.joinToString("\n")}")
                    errorMessage = "Error loading data: ${e.message}\n\nPlease check:\n1. API is running\n2. Network connection\n3. User ID: $userId\n4. Check logs for details"
                } finally {
                    isRefreshing = false
                    isLoading = false
                }
            }
        }
    }
    
    // Pull refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = refreshData
    )
    
    // Initial data load when screen first loads
    LaunchedEffect(userId) {
        isLoading = true
        refreshData()
    }
    
    // Refresh data when activity resumes (user returns from another screen)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                android.util.Log.d("DashboardActivity", "Activity resumed - refreshing data")
                refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Refresh data when switching to home tab (index 0)
    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            android.util.Log.d("DashboardActivity", "Switched to home tab - refreshing data")
            refreshData()
        }
    }
    
    // Set the refresh callback for the activity
    LaunchedEffect(Unit) {
        onRefreshCallbackSet?.invoke(refreshData)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
                .statusBarsPadding()
        ) {
            // Show error message if there's an error
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.error),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            fontSize = 14.sp,
                            color = Color(0xFF555555)
                        )
                    }
                }
            }

            // Screen Content based on selected tab
            when (selectedTab) {
                0 -> {
                    // Home/Dashboard content
                    Box(modifier = Modifier.weight(1f)) {
                        HomeScreen(
                            userId = userId,
                            consumed = consumed,
                            burned = burned,
                            carbsCalories = carbsCalories,
                            proteinCalories = proteinCalories,
                            fatCalories = fatCalories,
                            waterAmount = waterAmount,
                            waterEntries = waterEntries,
                            recentMeals = recentMeals,
                            recentWorkouts = recentWorkouts,
                            recentWaterLogs = recentWaterLogs,
                            isLoading = isLoading,
                            dailyGoal = dailyGoal,
                            dailyWaterGoal = dailyWaterGoal,
                            dailyStepsGoal = dailyStepsGoal,
                            currentSteps = currentSteps,
                            isStepTrackingAvailable = isStepTrackingAvailable,
                            total = total,
                            progressPercentage = progressPercentage,
                            caloriesRemaining = caloriesRemaining,
                            isOverBudget = isOverBudget,
                            isWithinBudget = isWithinBudget,
                            animatedProgress = animatedProgress,
                            refreshData = refreshData,
                            onMealDelete = { meal -> 
                                recentMeals = recentMeals.filter { it.id != meal.id }
                            },
                            onWorkoutDelete = { workout ->
                                recentWorkouts = recentWorkouts.filter { it.id != workout.id }
                            },
                            onWaterLogDelete = { waterLog ->
                                recentWaterLogs = recentWaterLogs.filter { it.id != waterLog.id }
                            }
                        )
                    }
                }
                1 -> {
                    // Meals Screen
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        com.example.forkit.ui.screens.MealsScreen(userId = userId)
                    }
                }
                2 -> {
                    // Add functionality is handled below
                    Spacer(modifier = Modifier.weight(1f))
                }
                3 -> {
                    // Habits Screen
                    Box(modifier = Modifier.weight(1f)) {
                        HabitsScreen(userId = userId)
                    }
                }
                4 -> {
                    // Coach Screen
                    Box(modifier = Modifier.weight(1f)) {
                        CoachScreen(userId = userId)
                    }
                }
            }
            
            // Bottom Navigation (always clickable)
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { tabIndex ->
                    // All tabs now switch instantly within the same activity
                    selectedTab = tabIndex
                },
                showFloatingIcons = showFloatingIcons,
                onAddButtonClick = { showFloatingIcons = true }
            )
        }
        
        // Pull to refresh indicator
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // Add Floating Action Menu
        if (showFloatingIcons) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showFloatingIcons = false }
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Water Button
                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, AddWaterActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        context.startActivity(intent)
                        showFloatingIcons = false
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_water),
                            contentDescription = "Add Water",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.water),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Meal Button
                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, AddMealActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        context.startActivity(intent)
                        showFloatingIcons = false
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_meals),
                            contentDescription = "Add Meal",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.meal),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Workout Button
                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, AddWorkoutActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        context.startActivity(intent)
                        showFloatingIcons = false
                    },
                    containerColor = Color(0xFF673AB7),
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_workout),
                            contentDescription = "Add Workout",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.workout),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalorieWheel(
    carbsCalories: Int,
    proteinCalories: Int,
    fatCalories: Int,
    totalCalories: Int,
    isLoading: Boolean
) {
    val animatedCarbsCalories by animateFloatAsState(
        targetValue = carbsCalories.toFloat(),
        animationSpec = tween(1000)
    )
    val animatedProteinCalories by animateFloatAsState(
        targetValue = proteinCalories.toFloat(),
        animationSpec = tween(1000)
    )
    val animatedFatCalories by animateFloatAsState(
        targetValue = fatCalories.toFloat(),
        animationSpec = tween(1000)
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Canvas(modifier = Modifier.size(120.dp)) {
                val strokeWidth = 20.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2

                val carbsAngle = if (totalCalories > 0) (animatedCarbsCalories / totalCalories * 360f) else 0f
                val proteinAngle = if (totalCalories > 0) (animatedProteinCalories / totalCalories * 360f) else 0f
                val fatAngle = if (totalCalories > 0) (animatedFatCalories / totalCalories * 360f) else 0f

                var startAngle = -90f

                // Carbs (Yellow)
                drawArc(
                    color = Color(0xFFFFA726),
                    startAngle = startAngle,
                    sweepAngle = carbsAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += carbsAngle

                // Protein (Red)
                drawArc(
                    color = Color(0xFFEF5350),
                    startAngle = startAngle,
                    sweepAngle = proteinAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += proteinAngle

                // Fat (Blue)
                drawArc(
                    color = Color(0xFF42A5F5),
                    startAngle = startAngle,
                    sweepAngle = fatAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
fun MacronutrientBreakdown(
    carbsCalories: Int,
    proteinCalories: Int,
    fatCalories: Int,
    totalCalories: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Carbs
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFFFFA726), CircleShape)
            )
            Text(
                text = stringResource(R.string.carbs_calories, carbsCalories),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Protein
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFFEF5350), CircleShape)
            )
            Text(
                text = stringResource(R.string.protein_calories, proteinCalories),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Fat
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF42A5F5), CircleShape)
            )
            Text(
                text = stringResource(R.string.fat_calories, fatCalories),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    showFloatingIcons: Boolean,
    onAddButtonClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) },
            label = { Text(stringResource(R.string.home)) },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.ic_meals), contentDescription = stringResource(R.string.meals)) },
            label = { Text(stringResource(R.string.meals)) },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            FloatingActionButton(
                onClick = onAddButtonClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(if (showFloatingIcons) 45f else 0f)
                )
            }
        }
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.ic_habits), contentDescription = stringResource(R.string.habits)) },
            label = { Text(stringResource(R.string.habits)) },
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Info, contentDescription = stringResource(R.string.coach)) },
            label = { Text(stringResource(R.string.coach)) },
            selected = selectedTab == 4,
            onClick = { onTabSelected(4) }
        )
    }
}

@Composable
fun FloatingIcons(
    context: android.content.Context,
    userId: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Water Button
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, AddWaterActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    context.startActivity(intent)
                    onDismiss()
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_water),
                        contentDescription = stringResource(R.string.add_water),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.water),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            
            // Meal Button
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, AddMealActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    context.startActivity(intent)
                    onDismiss()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_meals),
                        contentDescription = stringResource(R.string.add_meal),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.meal),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            
            // Workout Button
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, AddWorkoutActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    context.startActivity(intent)
                    onDismiss()
                },
                containerColor = Color(0xFF673AB7),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_workout),
                        contentDescription = stringResource(R.string.add_workout),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.workout),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    ForkItTheme {
        DashboardScreen()
    }
}
