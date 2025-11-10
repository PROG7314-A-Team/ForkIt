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

import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.TextStyle
import android.util.Log
import kotlinx.coroutines.delay
import com.example.forkit.ui.screens.HomeScreen
import com.example.forkit.ui.screens.MealsScreen
import com.example.forkit.ui.screens.HabitsScreen
import com.example.forkit.ui.screens.CoachScreen
import com.example.forkit.utils.NetworkConnectivityManager
import com.example.forkit.sync.SyncManager




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
        // Refresh data when activity resumes (e.g., returning from AddFullMealActivity)
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
    var mealsRefreshTrigger by remember { mutableStateOf(0) }
    
    // Network connectivity monitoring
    val networkManager = remember { NetworkConnectivityManager(context) }
    val syncManager = remember { SyncManager(context) }
    var isOnline by remember { mutableStateOf(networkManager.isOnline()) }
    
    // Initialize repositories for offline-first data access
    val database = remember { com.example.forkit.data.local.AppDatabase.getInstance(context) }
    val foodLogRepository = remember {
        com.example.forkit.data.repository.FoodLogRepository(
            apiService = com.example.forkit.data.RetrofitClient.api,
            foodLogDao = database.foodLogDao(),
            networkManager = networkManager
        )
    }
    val mealLogRepository = remember {
        com.example.forkit.data.repository.MealLogRepository(
            apiService = com.example.forkit.data.RetrofitClient.api,
            mealLogDao = database.mealLogDao(),
            networkManager = networkManager
        )
    }
    val waterLogRepository = remember {
        com.example.forkit.data.repository.WaterLogRepository(
            apiService = com.example.forkit.data.RetrofitClient.api,
            waterLogDao = database.waterLogDao(),
            networkManager = networkManager
        )
    }
    val exerciseLogRepository = remember {
        com.example.forkit.data.repository.ExerciseLogRepository(
            apiService = com.example.forkit.data.RetrofitClient.api,
            exerciseLogDao = database.exerciseLogDao(),
            networkManager = networkManager
        )
    }
    
    // Observe connectivity changes - moved after refreshData definition
    
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
    var lastRefreshAt by remember { mutableStateOf(0L) }
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
                    lastRefreshAt = System.currentTimeMillis()
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
                    
                    // NOTE: We now calculate food, exercise, and water totals from local DB
                    // This enables offline functionality - no need for summary API calls

                    // ─────────────────────────────────────────────
                    // Fetch from LOCAL DATABASE (includes both synced and unsynced)
                    // This enables offline functionality
                    // ─────────────────────────────────────────────
                    val foodLogsDeferred = async {
                        try {
                            foodLogRepository.getFoodLogsByDate(userId, todayDate)
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardActivity", "Error fetching food logs: ${e.message}", e)
                            emptyList()
                        }
                    }

                    val mealLogsDeferred = async {
                        try {
                            // Get only logged meals (not templates) for today
                            mealLogRepository.getLoggedMeals(userId, todayDate)
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardActivity", "Error fetching logged meals: ${e.message}", e)
                            emptyList()
                        }
                    }

                    
                    val exerciseLogsDeferred = async {
                        try {
                            exerciseLogRepository.getExerciseLogsByDate(userId, todayDate)
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardActivity", "Error fetching exercise logs: ${e.message}", e)
                            emptyList()
                        }
                    }
                    
                    val waterLogsDeferred = async {
                        try {
                            waterLogRepository.getWaterLogsByDate(userId, todayDate)
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardActivity", "Error fetching water logs: ${e.message}", e)
                            emptyList()
                        }
                    }
                    
                    // Wait for goals API call to complete
                    val goalsResponse = goalsDeferred.await()
                    
                    val endTime = System.currentTimeMillis()
                    android.util.Log.d("DashboardActivity", "✅ All API calls completed in ${endTime - startTime}ms")
                    
                    // Process goals response (still need this for goals)
                    goalsResponse?.let { response ->
                        if (response.isSuccessful) {
                            val goals = response.body()?.data
                            dailyGoal = goals?.dailyCalories ?: 2000
                            dailyWaterGoal = goals?.dailyWater ?: 2000
                            dailyStepsGoal = goals?.dailySteps ?: 8000
                            weeklyExercisesGoal = goals?.weeklyExercises ?: 3
                            android.util.Log.d("DashboardActivity", "✅ Goals loaded: Calories=$dailyGoal, Water=$dailyWaterGoal ml")
                        }
                    }

// ─────────────────────────────────────────────
// Process LOCAL DATABASE results (FoodLogEntity, MealLogEntity)
// ─────────────────────────────────────────────
                    val foodLogs = foodLogsDeferred.await()
                    val mealLogs = mealLogsDeferred.await()

                    val combinedList = mutableListOf<com.example.forkit.data.models.RecentActivityEntry>()

// 🥣 Process individual food logs from local DB
                    combinedList += foodLogs.map { log ->
                        com.example.forkit.data.models.RecentActivityEntry(
                            id = log.serverId ?: log.localId,
                            localId = log.localId,
                            foodName = log.foodName,
                            servingSize = log.servingSize,
                            measuringUnit = log.measuringUnit,
                            calories = log.calories.toInt(),
                            mealType = log.mealType,
                            date = log.date,
                            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                                .format(java.util.Date(log.createdAt)),
                            time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(log.createdAt))
                        )
                    }
                    android.util.Log.d("DashboardActivity", "✅ Added ${foodLogs.size} food logs from local DB")

                    // 🍱 Process logged meals from local DB (isTemplate=false)
                    combinedList += mealLogs.map { meal ->
                        com.example.forkit.data.models.RecentActivityEntry(
                            id = meal.serverId ?: meal.localId,
                            localId = meal.localId,
                            foodName = meal.name,
                            servingSize = meal.ingredients.size.toDouble(),
                            measuringUnit = "items",
                            calories = meal.totalCalories.toInt(),
                            mealType = meal.mealType ?: "Meal",
                            date = meal.date,
                            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                                .format(java.util.Date(meal.createdAt)),
                            time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(meal.createdAt))
                        )
                    }
                    android.util.Log.d("DashboardActivity", "✅ Added ${mealLogs.size} logged meals from local DB")

                    // 🔹 Merge + sort
                    recentMeals = combinedList.sortedByDescending { it.createdAt }

                    android.util.Log.d("DashboardActivity", "🍽️ Total combined entries: ${recentMeals.size}")
                    
                    // 🔹 Calculate totals from local database entries
                    consumed = foodLogs.sumOf { it.calories } + mealLogs.sumOf { it.totalCalories }
                    carbsCalories = (foodLogs.sumOf { it.carbs } + mealLogs.sumOf { it.totalCarbs }) * 4
                    proteinCalories = (foodLogs.sumOf { it.protein } + mealLogs.sumOf { it.totalProtein }) * 4
                    fatCalories = (foodLogs.sumOf { it.fat } + mealLogs.sumOf { it.totalFat }) * 9

                    Log.d("DashboardActivity", "🔥 Totals from local DB -> Consumed=$consumed kcal, Carbs=${carbsCalories}, Protein=${proteinCalories}, Fat=${fatCalories}")

                    
                    // Process exercise logs from local DB
                    val todayExerciseLogs = exerciseLogsDeferred.await()
                    burned = todayExerciseLogs.sumOf { it.caloriesBurnt }
                    recentWorkouts = todayExerciseLogs.map { log ->
                        com.example.forkit.data.models.RecentExerciseActivityEntry(
                            id = log.serverId ?: log.localId,
                            name = log.name,
                            type = log.type,
                            caloriesBurnt = log.caloriesBurnt.toInt(),
                            duration = log.duration?.toInt(),
                            date = log.date,
                            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                                .format(java.util.Date(log.createdAt)),
                            time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(log.createdAt))
                        )
                    }.sortedByDescending { it.createdAt }
                    android.util.Log.d("DashboardActivity", "✅ Workouts from local DB: ${recentWorkouts.size} items, burned=$burned kcal")
                    
                    // Process water logs from local DB
                    val todayWaterLogs = waterLogsDeferred.await()
                    waterAmount = todayWaterLogs.sumOf { it.amount }
                    waterEntries = todayWaterLogs.size
                    recentWaterLogs = todayWaterLogs.map { log ->
                        com.example.forkit.data.models.RecentWaterActivityEntry(
                            id = log.serverId ?: log.localId,
                            amount = log.amount.toInt(),
                            date = log.date,
                            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                                .format(java.util.Date(log.createdAt)),
                            time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(log.createdAt))
                        )
                    }.sortedByDescending { it.createdAt }
                    android.util.Log.d("DashboardActivity", "✅ Water logs from local DB: ${recentWaterLogs.size} items, total=$waterAmount ml")
                    
                } catch (e: Exception) {
                    android.util.Log.e("DashboardActivity", "Fatal error loading data: ${e.message}", e)
                    android.util.Log.e("DashboardActivity", "Error details: ${e.javaClass.simpleName}", e)
                    android.util.Log.e("DashboardActivity", "Stack trace: ${e.stackTrace.joinToString("\n")}")
                    errorMessage = "Error loading data: ${e.message}\n\nPlease check:\n1. API is running\n2. Network connection\n3. User ID: $userId\n4. Check logs for details"
                } finally {
                    isRefreshing = false
                    isLoading = false
                    // Trigger meals screen refresh
                    mealsRefreshTrigger++
                }
            }
        }
    }

    // Debounced refresh wrapper to prevent overlapping/rapid triggers
    val maybeRefresh: () -> Unit = {
        val now = System.currentTimeMillis()
        if (!isRefreshing && now - lastRefreshAt >= 1000) {
            refreshData()
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
                maybeRefresh()
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
            maybeRefresh()
        }
    }
    
    // Set the refresh callback for the activity
    LaunchedEffect(Unit) {
        onRefreshCallbackSet?.invoke(refreshData)
    }
    
    // Observe connectivity changes
    LaunchedEffect(Unit) {
        networkManager.observeConnectivity().collect { online ->
            val wasOffline = !isOnline
            isOnline = online
            
            // Trigger sync when coming back online
            if (online && wasOffline) {
                Log.d("DashboardActivity", "Device came online, triggering sync")
                syncManager.scheduleSync()
                // Refresh data after a short delay to get synced data
                kotlinx.coroutines.delay(2000)
                maybeRefresh()
            }
        }
    }
    
    // Observe database changes for real-time updates (works offline!)
    LaunchedEffect(userId, todayDate) {
        if (userId.isNotEmpty()) {
            // Collect changes from all log types
            launch {
                database.foodLogDao().getAllFlow(userId).collect {
                    // Trigger refresh when food logs change
                    android.util.Log.d("DashboardActivity", "🔄 Food logs changed, refreshing...")
                    maybeRefresh()
                }
            }
            launch {
                database.waterLogDao().getAllFlow(userId).collect {
                    // Trigger refresh when water logs change
                    android.util.Log.d("DashboardActivity", "🔄 Water logs changed, refreshing...")
                    maybeRefresh()
                }
            }
            launch {
                database.exerciseLogDao().getAllFlow(userId).collect {
                    // Trigger refresh when exercise logs change
                    android.util.Log.d("DashboardActivity", "🔄 Exercise logs changed, refreshing...")
                    maybeRefresh()
                }
            }
            launch {
                database.mealLogDao().getAllFlow(userId).collect {
                    // Trigger refresh when meal logs change
                    android.util.Log.d("DashboardActivity", "🔄 Meal logs changed, refreshing...")
                    maybeRefresh()
                }
            }
        }
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
            // Offline Indicator - Small badge at top
            if (!isOnline) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFF5252),
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Offline",
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Text(
                                text = "Offline",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
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
                                scope.launch {
                                    try {
                                        // Determine if it's a food log or meal log based on mealType
                                        val result = if (meal.mealType == "Meal") {
                                            // It's a logged meal, delete from meal log repository
                                            mealLogRepository.deleteMealLog(meal.localId)
                                        } else {
                                            // It's a food log, delete from food log repository
                                            foodLogRepository.deleteFoodLog(meal.localId)
                                        }
                                        
                                        result.onSuccess {
                                            recentMeals = recentMeals.filter { it.localId != meal.localId }
                                            refreshData()
                                        }.onFailure { e ->
                                            Log.e("DashboardActivity", "Failed to delete meal: ${e.message}", e)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("DashboardActivity", "Error deleting meal: ${e.message}", e)
                                    }
                                }
                            },
                            onWorkoutDelete = { workout ->
                                scope.launch {
                                    try {
                                        val result = exerciseLogRepository.deleteExerciseLog(workout.id)
                                        result.onSuccess {
                                            recentWorkouts = recentWorkouts.filter { it.id != workout.id }
                                            refreshData()
                                        }.onFailure { e ->
                                            Log.e("DashboardActivity", "Failed to delete workout: ${e.message}", e)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("DashboardActivity", "Error deleting workout: ${e.message}", e)
                                    }
                                }
                            },
                            onWaterLogDelete = { waterLog ->
                                scope.launch {
                                    try {
                                        val result = waterLogRepository.deleteWaterLog(waterLog.id)
                                        result.onSuccess {
                                            recentWaterLogs = recentWaterLogs.filter { it.id != waterLog.id }
                                            refreshData()
                                        }.onFailure { e ->
                                            Log.e("DashboardActivity", "Failed to delete water log: ${e.message}", e)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("DashboardActivity", "Error deleting water log: ${e.message}", e)
                                    }
                                }
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
                        com.example.forkit.ui.screens.MealsScreen(userId = userId, mealLogRepository = mealLogRepository, refreshTrigger = mealsRefreshTrigger)
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
                
                // Food Button
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
                            contentDescription = "Add Food",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.add_food),
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
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Canvas(modifier = Modifier.size(160.dp)) {
                val strokeWidth = 16.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)

                // Calculate angles based on total calories consumed
                val carbsAngle = if (totalCalories > 0) (animatedCarbsCalories / totalCalories * 360f) else 0f
                val proteinAngle = if (totalCalories > 0) (animatedProteinCalories / totalCalories * 360f) else 0f
                val fatAngle = if (totalCalories > 0) (animatedFatCalories / totalCalories * 360f) else 0f

                var startAngle = -90f

                // Draw background ring (remaining calories) - light grey
                drawArc(
                    color = Color(0xFFE0E0E0),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Carbs (ForkIt Blue)
                if (carbsAngle > 0) {
                    drawArc(
                        color = Color(0xFF1E9ECD), // ForkIt Blue
                        startAngle = startAngle,
                        sweepAngle = carbsAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += carbsAngle
                }

                // Protein (ForkIt Green)
                if (proteinAngle > 0) {
                    drawArc(
                        color = Color(0xFF22B27D), // ForkIt Green
                        startAngle = startAngle,
                        sweepAngle = proteinAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += proteinAngle
                }

                // Fat (Darker Purple)
                if (fatAngle > 0) {
                    drawArc(
                        color = Color(0xFF6A1B9A), // Darker Purple
                        startAngle = startAngle,
                        sweepAngle = fatAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
            
            // Center text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = totalCalories.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF22B27D), // ForkIt Green for the main number
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Today's Calories",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Carbs
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF1E9ECD), CircleShape) // ForkIt Blue
                )
                Text(
                    text = "Carbs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${carbsCalories}cal (${if (totalCalories > 0) ((carbsCalories * 100) / totalCalories) else 0}%)",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Protein
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF22B27D), CircleShape) // ForkIt Green
                )
                Text(
                    text = "Protein",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${proteinCalories}cal (${if (totalCalories > 0) ((proteinCalories * 100) / totalCalories) else 0}%)",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Fat
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF6A1B9A), CircleShape) // Darker Purple
                )
                Text(
                    text = "Fat",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${fatCalories}cal (${if (totalCalories > 0) ((fatCalories * 100) / totalCalories) else 0}%)",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            
            // Food Button
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
                        contentDescription = stringResource(R.string.add_food),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.add_food),
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
