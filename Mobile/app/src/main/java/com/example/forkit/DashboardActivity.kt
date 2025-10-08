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




class DashboardActivity : ComponentActivity() {
    private var refreshCallback: (() -> Unit)? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
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
                    // Meals Screen
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        MealsScreen(userId = userId)

                    }
                }
                2 -> {
                    // Add functionality is handled below
                    Spacer(modifier = Modifier.weight(1f))
                }
                3 -> {
                    // Habits Screen - Navigate to HabitsActivity
                    Spacer(modifier = Modifier.weight(1f))
                }
                4 -> {
                    // Coach Screen - Navigate to CoachActivity
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            
            // Bottom Navigation (always clickable)
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { tabIndex ->
                    if (tabIndex == 3) {
                        // Navigate to HabitsActivity
                        val intent = Intent(context, HabitsActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        context.startActivity(intent)
                    } else if (tabIndex == 4) {
                        // Navigate to CoachActivity
                        val intent = Intent(context, CoachActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        context.startActivity(intent)
                    } else {
                        selectedTab = tabIndex
                    }
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

@Composable
fun CalorieWheel(
    carbsCalories: Int,
    proteinCalories: Int,
    fatCalories: Int,
    totalCalories: Int,
    isLoading: Boolean = false
) {
    val carbsProgress = if (totalCalories > 0) carbsCalories.toFloat() / totalCalories else 0f
    val proteinProgress = if (totalCalories > 0) proteinCalories.toFloat() / totalCalories else 0f
    val fatProgress = if (totalCalories > 0) fatCalories.toFloat() / totalCalories else 0f
    
    val animatedCarbs by animateFloatAsState(
        targetValue = carbsProgress,
        animationSpec = tween(1000)
    )
    val animatedProtein by animateFloatAsState(
        targetValue = proteinProgress,
        animationSpec = tween(1000)
    )
    val animatedFat by animateFloatAsState(
        targetValue = fatProgress,
        animationSpec = tween(1000)
    )
    
    // ForkIt brand colors for macronutrients
    val outlineColor = MaterialTheme.colorScheme.outline
    val carbsColor = Color(0xFF1E9ECD) // ForkIt Blue
    val proteinColor = Color(0xFF22B27D) // ForkIt Green
    val fatColor = Color(0xFF6B4FA0) // Dark Purple
    
    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            // Background circle
            drawCircle(
                color = outlineColor,
                radius = radius,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            var currentAngle = -90f
            
            // Carbs segment (ForkIt Blue)
            drawArc(
                color = carbsColor,
                startAngle = currentAngle,
                sweepAngle = 360f * animatedCarbs,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            currentAngle += 360f * animatedCarbs
            
            // Protein segment (ForkIt Green)
            drawArc(
                color = proteinColor,
                startAngle = currentAngle,
                sweepAngle = 360f * animatedProtein,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            currentAngle += 360f * animatedProtein
            
            // Fat segment (Dark Purple)
            drawArc(
                color = fatColor,
                startAngle = currentAngle,
                sweepAngle = 360f * animatedFat,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = totalCalories.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary // ForkIt Green
                )
                Text(
                    text = "Today's Calories",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground,
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
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Carbs
        MacronutrientItem(
            name = "Carbs",
            calories = carbsCalories,
            color = Color(0xFF1E9ECD), // ForkIt Blue
            percentage = if (totalCalories > 0) (carbsCalories * 100 / totalCalories) else 0
        )
        
        // Protein
        MacronutrientItem(
            name = "Protein",
            calories = proteinCalories,
            color = Color(0xFF22B27D), // ForkIt Green
            percentage = if (totalCalories > 0) (proteinCalories * 100 / totalCalories) else 0
        )
        
        // Fat
        MacronutrientItem(
            name = "Fat",
            calories = fatCalories,
            color = Color(0xFF6B4FA0), // Dark Purple
            percentage = if (totalCalories > 0) (fatCalories * 100 / totalCalories) else 0
        )
    }
}

@Composable
fun MacronutrientItem(
    name: String,
    calories: Int,
    color: Color,
    percentage: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        
        Column {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "${calories}cal (${percentage}%)",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onBackground
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF22B27D)) // ForkIt Green background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Home Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(0) }
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (selectedTab == 0) Color.White else Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "Home",
                    fontSize = 12.sp,
                    color = if (selectedTab == 0) Color.White else Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Meals Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(1) }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_meals),
                    contentDescription = "Meals",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(
                        if (selectedTab == 1) Color.White else Color.White.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = "Meals",
                    fontSize = 12.sp,
                    color = if (selectedTab == 1) Color.White else Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Add Tab (Special styling)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onAddButtonClick() }
            ) {
                Card(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.rotate(
                                animateFloatAsState(
                                    targetValue = if (showFloatingIcons) 45f else 0f,
                                    animationSpec = tween(300)
                                ).value
                            )
                        )
                    }
                }
                Text(
                    text = "Add",
                    fontSize = 12.sp,
                    color = if (showFloatingIcons) Color.White else Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Habits Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(3) }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_habits),
                    contentDescription = "Habits",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(
                        if (selectedTab == 3) Color.White else Color.White.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = "Habits",
                    fontSize = 12.sp,
                    color = if (selectedTab == 3) Color.White else Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Coach Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(4) }
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Coach",
                    tint = if (selectedTab == 4) Color.White else Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "Coach",
                    fontSize = 12.sp,
                    color = if (selectedTab == 4) Color.White else Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun FloatingIcons(
    context: android.content.Context,
    userId: String,
    onDismiss: () -> Unit
) {
    // Smooth fade and scale animation when opened
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400)
    )

    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
            .clickable { onDismiss() } // Tap anywhere to close
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp, top = 200.dp)
                .alpha(animatedAlpha),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 🍱 Full Meal Logging
            FloatingActionItem(
                icon = R.drawable.ic_meals,
                label = "Full Meal",
                color = MaterialTheme.colorScheme.primary,
                onClick = {
                    val intent = Intent(context, AddFullMealActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    context.startActivity(intent)
                    onDismiss()
                },
                scale = animatedScale
            )

            // 🍎 Food (Meal) Logging
            FloatingActionItem(
                icon = R.drawable.icon_logo_, // your icon in drawable
                label = "Food (Meal)",
                color = MaterialTheme.colorScheme.tertiary,
                onClick = {
                    val intent = Intent(context, AddMealActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    context.startActivity(intent)
                    onDismiss()
                },
                scale = animatedScale
            )

            // 💧 Water Logging
            FloatingActionItem(
                icon = R.drawable.ic_water,
                label = "Water",
                color = Color(0xFF2196F3), // blue
                onClick = {
                    val intent = Intent(context, AddWaterActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    context.startActivity(intent)
                    onDismiss()
                },
                scale = animatedScale
            )

            // 🏋️ Exercise Logging
            FloatingActionItem(
                icon = R.drawable.ic_workout,
                label = "Exercise",
                color = Color(0xFF22B27D), // green accent
                onClick = {
                    val intent = Intent(context, AddWorkoutActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    context.startActivity(intent)
                    onDismiss()
                },
                scale = animatedScale
            )
        }
    }
}

@Composable
fun FloatingActionItem(
    icon: Int,
    label: String,
    color: Color,
    onClick: () -> Unit,
    scale: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 6.dp) // clean layout, no scaling here
    ) {
        // Circular icon button (scaled + clickable)
        Box(
            modifier = Modifier
                .graphicsLayer(scaleX = scale, scaleY = scale) // ✅ scale icon only
                .size(60.dp)
                .clip(CircleShape)
                .background(color)
                .clickable { onClick() }, // ✅ click only applies to the circle
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Label text below icon
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun FloatingIcon(
    icon: Int,
    label: String,
    onClick: () -> Unit,
    scale: Float
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .scale(scale)
            .size(56.dp), // Fixed size like a floating action button
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), // Green background like in the image
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(Color.White) // White icon on green background
            )
        }
    }
}

// 🔧 Global tag for all meals-related logs
private const val DEBUG_TAG = "MealsDebug"

// ─────────────────────────────────────────────
// 🥗 MealsScreen Composable
// Handles listing, loading, and navigation to meal details
// ─────────────────────────────────────────────
@Composable
fun MealsScreen(userId: String) {
    val TAG = "MealsScreen"
    val context = LocalContext.current

    // 🔹 UI state variables
    var meals by remember { mutableStateOf<List<MealLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 🔹 Log the start of the screen
    Log.d(DEBUG_TAG, "$TAG: Initializing MealsScreen for userId=$userId")

    // ─────────────────────────────────────────────
    // Load meals
    // ─────────────────────────────────────────────
    LaunchedEffect(userId) {
        Log.d("MealsScreen", "Fetching real meals from API for userId=$userId")
        try {
            val response = RetrofitClient.api.getMealLogs(userId = userId)
            if (response.isSuccessful && response.body()?.success == true) {
                val allMeals = response.body()?.data ?: emptyList()

                // 🧹 Remove duplicates by unique name + calories + date combination
                meals = allMeals.distinctBy { "${it.name}-${it.totalCalories}-${it.date}" }

                Log.d("MealsScreen", "✅ Meals loaded: ${allMeals.size}, unique after filter: ${meals.size}")
            }
            else {
                Log.w("MealsScreen", "⚠️ Failed to fetch meals: ${response.message()}")
                meals = emptyList()
            }
        } catch (e: Exception) {
            Log.e("MealsScreen", "❌ Error fetching meals", e)
            meals = emptyList()
        } finally {
            isLoading = false
        }
    }


    // ─────────────────────────────────────────────
    // UI Composition
    // ─────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // 🔹 Header section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Meals.",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                )
                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {
                    Log.d(DEBUG_TAG, "$TAG: Refresh button clicked.")
                    Toast.makeText(context, "Refreshing meals...", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 🔹 Conditional UI states
            when {
                isLoading -> {
                    Log.d(DEBUG_TAG, "$TAG: Showing loading indicator...")
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                errorMessage != null -> {
                    Log.w(DEBUG_TAG, "$TAG: Displaying error message: $errorMessage")
                    Text(
                        text = "Error loading meals: $errorMessage",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                meals.isEmpty() -> {
                    Log.d(DEBUG_TAG, "$TAG: No meals found for user.")
                    Text(
                        text = "No meals logged yet.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }

                else -> {
                    Log.d(DEBUG_TAG, "$TAG: Rendering meals list...")
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(meals) { meal ->
                            MealCard(meal = meal) {
                                Log.i(DEBUG_TAG, "$TAG: Clicked meal -> ${meal.name}")

                                // Convert Ingredient objects to names
                                val ingredientNames = ArrayList(meal.ingredients.map { it.name })

                                val intent = Intent(context, MealDetailActivity::class.java).apply {
                                    putExtra("MEAL_NAME", meal.name)
                                    putExtra("MEAL_DESCRIPTION", meal.description ?: "No description available")
                                    putStringArrayListExtra("INGREDIENTS", ingredientNames)
                                    putExtra("CALORIES", meal.totalCalories)
                                    putExtra("USER_ID", userId)
                                }


                                context.startActivity(intent)
                            }

                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }

        // 🔹 Floating Add Meal Button
        FloatingActionButton(
            onClick = {
                Log.d(DEBUG_TAG, "$TAG: Add Meal button clicked. Opening AddFullMealActivity.")
                val intent = Intent(context, AddFullMealActivity::class.java).apply {
                    putExtra("USER_ID", userId)
                }
                context.startActivity(intent)
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Meal")
        }
    }
}

// ─────────────────────────────────────────────
// 🍱 Data Model for Meal
// ─────────────────────────────────────────────
data class Meal(
    val name: String,
    val ingredients: List<String>,
    val calories: Double
)

// ─────────────────────────────────────────────
// 🍽️ MealCard Composable
// Displays each meal item in a card layout
// ─────────────────────────────────────────────
@Composable
fun MealCard(meal: MealLog, onClick: () -> Unit) {
    val TAG = "MealCard"
    Log.v(DEBUG_TAG, "$TAG: Rendering card for ${meal.name}")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Log.d(DEBUG_TAG, "$TAG: Card clicked for ${meal.name}")
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {

                // 🟢 Meal name
                Text(
                    text = meal.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 🟢 Safely render ingredient preview
                val ingredientNames = meal.ingredients.map { it.name }
                val ingredientText = ingredientNames.joinToString(", ")
                val abbreviatedText = if (ingredientText.length > 40)
                    ingredientText.take(40) + "…"
                else
                    ingredientText

                Text(
                    text = "${meal.ingredients.size} ingredients: $abbreviatedText",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 🟢 Calories display (from totalCalories field)
            Text(
                text = "${meal.totalCalories.toInt()} kcal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
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

