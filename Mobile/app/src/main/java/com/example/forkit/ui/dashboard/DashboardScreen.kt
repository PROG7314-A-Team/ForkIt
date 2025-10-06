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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.example.forkit.BottomNavigationBar
import com.example.forkit.CalorieWheel
import com.example.forkit.CoachActivity
import com.example.forkit.FloatingIcons
import com.example.forkit.HabitsActivity
import com.example.forkit.MacronutrientBreakdown
import com.example.forkit.ProfileActivity
import com.example.forkit.R
import com.example.forkit.StepTracker

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

                    val foodLogsDeferred = async {
                        try {
                            com.example.forkit.data.RetrofitClient.api.getFoodLogs(userId, todayDate)
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardActivity", "Error fetching food logs: ${e.message}", e)
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
                    val foodLogsResponse = foodLogsDeferred.await()
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

                    // Process food logs response
                    foodLogsResponse?.let { response ->
                        if (response.isSuccessful) {
                            val todayLogs = response.body()?.data ?: emptyList()
                            recentMeals = todayLogs.map { log ->
                                com.example.forkit.data.models.RecentActivityEntry(
                                    id = log.id,
                                    foodName = log.foodName,
                                    servingSize = log.servingSize,
                                    measuringUnit = log.measuringUnit,
                                    calories = log.calories.toInt(),
                                    mealType = log.mealType,
                                    date = log.date,
                                    createdAt = log.createdAt,
                                    time = log.createdAt.substring(11, 16)
                                )
                            }.sortedByDescending { it.createdAt }
                            android.util.Log.d("DashboardActivity", "Meals loaded: ${recentMeals.size} items")
                        }
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
                            text = "⚠️ Error",
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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Top Header with Logo and Profile
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ForkIt Logo
                            Image(
                                painter = painterResource(id = R.drawable.forkit_logo),
                                contentDescription = "ForkIt Logo",
                                modifier = Modifier.size(40.dp)
                            )

                            // Profile Tab
                            Card(
                                modifier = Modifier.clickable {
                                    val intent = Intent(context, ProfileActivity::class.java)
                                    intent.putExtra("USER_ID", userId)
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary) // ForkIt blue border
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = MaterialTheme.colorScheme.secondary, // ForkIt blue color
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "PROFILE",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary // ForkIt blue color
                                    )
                                }
                            }
                        }

                        // Welcome Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary) // ForkIt green border
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Welcome to the Dashboard",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Track your health and fitness journey",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }



                        // Total Caloric Intake Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Today's Caloric Intake",
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$total kcal",
                                        fontSize = 24.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }



                        // Consumed and Burned Cards
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Consumed Card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Consumed",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$consumed kcal",
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Burned Card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Burned",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$burned kcal",
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Daily Goal Progress Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                // Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Today's Calorie Budget",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$consumed / $dailyGoal kcal",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOverBudget) Color(0xFFE53935) else MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Progress Bar - red when over budget, green when within budget
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    color = if (isOverBudget) Color(0xFFE53935) else MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.outline
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Progress Text
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${(animatedProgress * 100).toInt()}% Used",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = if (isOverBudget) "Over Budget! ⚠️" else if (caloriesRemaining >= 0) "$caloriesRemaining kcal remaining" else "0 kcal remaining",
                                        fontSize = 12.sp,
                                        color = if (isOverBudget) Color(0xFFE53935) else if (isWithinBudget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                        fontWeight = if (isOverBudget) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Today's Calorie Wheel Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            if (!isLoading && consumed == 0.0) {
                                // Empty state when no food logged
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🍽️",
                                        fontSize = 48.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No food logged today",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Start logging your meals to see your nutrition breakdown",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                                ) {
                                    // Calorie Wheel (Left side)
                                    CalorieWheel(
                                        carbsCalories = carbsCalories.toInt(),
                                        proteinCalories = proteinCalories.toInt(),
                                        fatCalories = fatCalories.toInt(),
                                        totalCalories = consumed.toInt(),
                                        isLoading = isLoading
                                    )

                                    // Macronutrient Breakdown (Right side)
                                    MacronutrientBreakdown(
                                        carbsCalories = carbsCalories.toInt(),
                                        proteinCalories = proteinCalories.toInt(),
                                        fatCalories = fatCalories.toInt(),
                                        totalCalories = consumed.toInt()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Additional Stats Cards
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Water Intake Card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "💧",
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Water",
                                        fontSize = 14.sp,
                                        color = Color(0xFF333333), // Black text
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (isLoading) {
                                        Text(
                                            text = "Loading...",
                                            fontSize = 14.sp,
                                            color = Color(0xFF333333).copy(alpha = 0.8f)
                                        )
                                    } else {
                                        // Amount and Goal
                                        Text(
                                            text = "${waterAmount.toInt()} / $dailyWaterGoal ml",
                                            fontSize = 16.sp,
                                            color = Color(0xFF333333), // Black text
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Progress bar
                                        val waterProgress = (waterAmount.toFloat() / dailyWaterGoal.toFloat()).coerceIn(0f, 1f)
                                        LinearProgressIndicator(
                                            progress = { waterProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = Color(0xFF333333),
                                            trackColor = Color(0xFF333333).copy(alpha = 0.3f)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Percentage
                                        Text(
                                            text = "${(waterProgress * 100).toInt()}%",
                                            fontSize = 12.sp,
                                            color = Color(0xFF333333).copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }

                            // Steps Card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "👟",
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Steps",
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (isLoading) {
                                        Text(
                                            text = "Loading...",
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    } else if (!isStepTrackingAvailable) {
                                        Text(
                                            text = "Not available",
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    } else {
                                        // Steps and Goal
                                        Text(
                                            text = "$currentSteps / $dailyStepsGoal",
                                            fontSize = 16.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Progress bar with actual data
                                        val stepsProgress = (currentSteps.toFloat() / dailyStepsGoal.toFloat()).coerceIn(0f, 1f)
                                        LinearProgressIndicator(
                                            progress = { stepsProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = Color.White,
                                            trackColor = Color.White.copy(alpha = 0.3f)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Percentage
                                        Text(
                                            text = "${(stepsProgress * 100).toInt()}%",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Recent Meals Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = "Today's Meals",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Show loading or meals
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else if (recentMeals.isEmpty()) {
                                    Text(
                                        text = "No meals logged today",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                } else {
                                    // Display real meal items from API
                                    var mealToDelete by remember { mutableStateOf<com.example.forkit.data.models.RecentActivityEntry?>(null) }

                                    recentMeals.forEach { meal ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = meal.foodName,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                Text(
                                                    text = "${meal.mealType} - ${meal.calories} kcal",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                                )
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = meal.time,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )

                                                // Delete button
                                                IconButton(
                                                    onClick = { mealToDelete = meal },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = Color(0xFFE53935),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Delete confirmation dialog
                                    mealToDelete?.let { meal ->
                                        AlertDialog(
                                            onDismissRequest = { mealToDelete = null },
                                            title = {
                                                Text(
                                                    "Delete Food Log?",
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            text = {
                                                Text("Are you sure you want to delete \"${meal.foodName}\" from your log?")
                                            },
                                            confirmButton = {
                                                TextButton(
                                                    onClick = {
                                                        scope.launch {
                                                            try {
                                                                val response = RetrofitClient.api.deleteFoodLog(meal.id)
                                                                if (response.isSuccessful && response.body()?.success == true) {
                                                                    // Remove from local list and refresh data
                                                                    recentMeals = recentMeals.filter { it.id != meal.id }
                                                                    mealToDelete = null

                                                                    // Refresh dashboard data
                                                                    refreshData()

                                                                    Toast.makeText(context, "Food deleted successfully", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    Toast.makeText(context, "Failed to delete food", Toast.LENGTH_SHORT).show()
                                                                }
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Text("Delete", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                                                }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { mealToDelete = null }) {
                                                    Text("Cancel")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Today's Workouts Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(2.dp, Color(0xFF673AB7)) // Dark purple border
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Today's Workouts",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333), // Black text like meals
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else if (recentWorkouts.isEmpty()) {
                                    Text(
                                        text = "No workouts logged today",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                } else {
                                    // Display real workout items from API
                                    var workoutToDelete by remember { mutableStateOf<com.example.forkit.data.models.RecentExerciseActivityEntry?>(null) }

                                    recentWorkouts.forEach { workout ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = workout.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${workout.type} • ${workout.caloriesBurnt} cal${if (workout.duration != null) " • ${workout.duration}min" else ""}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                Text(
                                                    text = workout.time,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                                )
                                            }

                                            IconButton(
                                                onClick = { workoutToDelete = workout }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete workout",
                                                    tint = Color(0xFFE53935),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Delete confirmation dialog
                                    workoutToDelete?.let { workout ->
                                        AlertDialog(
                                            onDismissRequest = { workoutToDelete = null },
                                            title = {
                                                Text(
                                                    "Delete Workout?",
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            text = {
                                                Text("Are you sure you want to delete \"${workout.name}\" from your log?")
                                            },
                                            confirmButton = {
                                                TextButton(
                                                    onClick = {
                                                        scope.launch {
                                                            try {
                                                                val response = RetrofitClient.api.deleteExerciseLog(workout.id)
                                                                if (response.isSuccessful && response.body()?.success == true) {
                                                                    // Remove from local list and refresh data
                                                                    recentWorkouts = recentWorkouts.filter { it.id != workout.id }
                                                                    workoutToDelete = null

                                                                    // Refresh dashboard data
                                                                    refreshData()

                                                                    Toast.makeText(context, "Workout deleted successfully", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    Toast.makeText(context, "Failed to delete workout", Toast.LENGTH_SHORT).show()
                                                                }
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Text("Delete", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                                                }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { workoutToDelete = null }) {
                                                    Text("Cancel")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Today's Water Logs Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(2.dp, Color(0xFF1E9ECD)) // ForkIt blue border
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Today's Water Logs",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E9ECD), // ForkIt blue
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else if (recentWaterLogs.isEmpty()) {
                                    Text(
                                        text = "No water logged today",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                } else {
                                    // Display real water log items from API
                                    var waterLogToDelete by remember { mutableStateOf<com.example.forkit.data.models.RecentWaterActivityEntry?>(null) }

                                    recentWaterLogs.forEach { waterLog ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = "${waterLog.amount}ml",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Water intake",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                Text(
                                                    text = waterLog.time,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                                )
                                            }

                                            IconButton(
                                                onClick = { waterLogToDelete = waterLog }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete water log",
                                                    tint = Color(0xFFE53935),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Delete confirmation dialog
                                    waterLogToDelete?.let { waterLog ->
                                        AlertDialog(
                                            onDismissRequest = { waterLogToDelete = null },
                                            title = {
                                                Text(
                                                    "Delete Water Log?",
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            text = {
                                                Text("Are you sure you want to delete ${waterLog.amount}ml of water from your log?")
                                            },
                                            confirmButton = {
                                                TextButton(
                                                    onClick = {
                                                        scope.launch {
                                                            try {
                                                                val response = RetrofitClient.api.deleteWaterLog(waterLog.id)
                                                                if (response.isSuccessful && response.body()?.success == true) {
                                                                    // Remove from local list and refresh data
                                                                    recentWaterLogs = recentWaterLogs.filter { it.id != waterLog.id }
                                                                    waterLogToDelete = null

                                                                    // Refresh dashboard data
                                                                    refreshData()

                                                                    Toast.makeText(context, "Water log deleted successfully", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    Toast.makeText(context, "Failed to delete water log", Toast.LENGTH_SHORT).show()
                                                                }
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Text("Delete", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                                                }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { waterLogToDelete = null }) {
                                                    Text("Cancel")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bottom padding to ensure last content is visible above navigation
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
                1 -> {
                    Box(modifier = Modifier.weight(1f)) {
                        MealsScreen(
                            userId = userId,
                            onMealSelected = { /* TODO */ },
                            onAddMealClicked = { /* TODO */ }
                        )
                    }
                }



                2 -> {
                    // Add functionality is handled below
                    Spacer(modifier = Modifier.weight(1f))
                }
                3 -> {
                    // ✅ Habits screen launches the activity once
                    LaunchedEffect(Unit) {
                        val intent = Intent(context, HabitsActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        context.startActivity(intent)
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("Opening Habits...", color = MaterialTheme.colorScheme.primary)
                    }
                }

                4 -> {
                    // ✅ Coach screen launches the activity once
                    LaunchedEffect(Unit) {
                        val intent = Intent(context, CoachActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        context.startActivity(intent)
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("Opening Coach...", color = MaterialTheme.colorScheme.primary)
                    }
                }

            }

            // Bottom Navigation (always clickable)
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { tabIndex ->
                    selectedTab = tabIndex  // ✅ simple tab switch, no more Intents or reloads
                },
                showFloatingIcons = showFloatingIcons,
                onAddButtonClick = { showFloatingIcons = true }
            )

        }

        // Pull to refresh indicator
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp), // Below the status bar
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )

        // Floating Icons Overlay (when add button is clicked)
        if (showFloatingIcons) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)) // Semi-transparent overlay
            ) {
                FloatingIcons(
                    context = context,
                    userId = userId,
                    onDismiss = { showFloatingIcons = false } // Hide the overlay
                )
            }
        }

    }
}

