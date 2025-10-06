package com.example.forkit

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.TrendEntry
import com.example.forkit.ui.theme.ForkItTheme
import kotlinx.coroutines.launch
import com.example.forkit.ui.dashboard.DashboardActivity


class CoachActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val userId = intent.getStringExtra("USER_ID") ?: ""
        
        setContent {
            ForkItTheme {
                CoachScreenWithNav(userId = userId)
            }
        }
    }
}

@Composable
fun CoachScreenWithNav(userId: String) {
    val context = LocalContext.current
    var showFloatingIcons by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("main") } // main, calories, water, workouts
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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
            
            // Bottom Navigation Bar
            BottomNavigationBar(
                selectedTab = 4, // Coach tab is index 4
                onTabSelected = { tabIndex ->
                    when (tabIndex) {
                        0 -> {
                            // Home - Navigate to Dashboard
                            val intent = Intent(context, DashboardActivity::class.java)
                            intent.putExtra("USER_ID", userId)
                            intent.putExtra("SELECTED_TAB", 0)
                            context.startActivity(intent)
                        }
                        1 -> {
                            // Meals - Navigate to Dashboard and select Meals tab
                            val intent = Intent(context, DashboardActivity::class.java)
                            intent.putExtra("USER_ID", userId)
                            intent.putExtra("SELECTED_TAB", 1)
                            context.startActivity(intent)
                        }
                        2 -> {
                            // Add button - Show floating icons
                            showFloatingIcons = true
                        }
                        3 -> {
                            // Habits - Navigate to HabitsActivity
                            val intent = Intent(context, HabitsActivity::class.java)
                            intent.putExtra("USER_ID", userId)
                            context.startActivity(intent)
                        }
                        4 -> {
                            // Already on Coach - do nothing
                        }
                    }
                },
                showFloatingIcons = showFloatingIcons,
                onAddButtonClick = { showFloatingIcons = true }
            )
        }
        
        // Floating Icons Overlay (when add button is clicked)
        if (showFloatingIcons) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
            ) {
                FloatingIcons(
                    context = context,
                    userId = userId,
                    onDismiss = { showFloatingIcons = false }
                )
            }
        }
    }
}

@Composable
fun CoachMainScreen(
    userId: String,
    modifier: Modifier = Modifier,
    onNavigateToCalories: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToWorkouts: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Coach",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            style = androidx.compose.ui.text.TextStyle(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Text(
            text = "Track your health metrics over time",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Calorie Tracking Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clickable { onNavigateToCalories() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            )
                        )
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_meals),
                            contentDescription = "Calorie Tracking",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(48.dp)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Calorie Tracking",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Track your daily budget",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                    Text(
                        text = "→",
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Water Tracking Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clickable { onNavigateToWater() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                                )
                            )
                        )
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_water),
                            contentDescription = "Water Tracking",
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(48.dp)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Water Tracking",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Monitor your hydration",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                    Text(
                        text = "→",
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Workout Tracking Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clickable { onNavigateToWorkouts() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(2.dp, Color(0xFF673AB7))
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF673AB7).copy(alpha = 0.1f),
                                    Color(0xFF673AB7).copy(alpha = 0.05f)
                                )
                            )
                        )
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_workout),
                            contentDescription = "Workout Tracking",
                            tint = Color(0xFF673AB7).copy(alpha = 0.7f),
                            modifier = Modifier.size(48.dp)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Workout Tracking",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF673AB7)
                            )
                            Text(
                                text = "Track your exercises",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                    Text(
                        text = "→",
                        fontSize = 32.sp,
                        color = Color(0xFF673AB7)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp)) // Space for bottom nav
    }
}

@Composable
fun CalorieDetailScreen(
    userId: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State for selected time period
    var selectedPeriod by remember { mutableStateOf("Week") }
    
    // State for chart data
    var chartData by remember { mutableStateOf<List<TrendEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var dailyGoal by remember { mutableStateOf(2000) }
    
    // Add icons import
    val ArrowBack = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack
    
    // Function to fetch calorie trends
    fun fetchCalorieTrends(period: String) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Fetch user goals first
                val goalsResponse = RetrofitClient.api.getUserGoals(userId)
                if (goalsResponse.isSuccessful && goalsResponse.body()?.success == true) {
                    dailyGoal = goalsResponse.body()?.data?.dailyCalories ?: 2000
                }
                
                // Calculate date range based on period
                val today = java.time.LocalDate.now()
                val (startDate, endDate) = when (period) {
                    "Day" -> today to today // Today only (12am to 12am)
                    "Week" -> {
                        // Get Monday of current week
                        val dayOfWeek = today.dayOfWeek.value // 1=Monday, 7=Sunday
                        val monday = today.minusDays((dayOfWeek - 1).toLong())
                        val sunday = monday.plusDays(6)
                        monday to sunday
                    }
                    "Month" -> {
                        val firstDay = today.withDayOfMonth(1)
                        val lastDay = today.withDayOfMonth(today.lengthOfMonth())
                        firstDay to lastDay
                    }
                    else -> today to today
                }
                
                val groupBy = when (period) {
                    "Day" -> "day"
                    "Week" -> "day" // Show daily breakdown for the week
                    "Month" -> "day" // Show daily breakdown for the month
                    else -> "day"
                }
                
                // Fetch calorie trends
                val response = RetrofitClient.api.getCalorieTrends(
                    userId = userId,
                    startDate = startDate.toString(),
                    endDate = endDate.toString(),
                    groupBy = groupBy
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val apiData = response.body()?.data?.trends ?: emptyList()
                    
                    // Create a complete list of dates for the period
                    val completeData = when (period) {
                        "Day" -> {
                            // Just today
                            listOf(
                                apiData.firstOrNull { it.date == endDate.toString() }
                                    ?: TrendEntry(
                                        date = endDate.toString(),
                                        calories = 0,
                                        carbs = 0,
                                        fat = 0,
                                        protein = 0,
                                        entryCount = 0
                                    )
                            )
                        }
                        "Week" -> {
                            // Always show Monday to Sunday (7 days)
                            (0..6).map { dayOffset ->
                                val date = startDate.plusDays(dayOffset.toLong())
                                apiData.firstOrNull { it.date == date.toString() }
                                    ?: TrendEntry(
                                        date = date.toString(),
                                        calories = 0,
                                        carbs = 0,
                                        fat = 0,
                                        protein = 0,
                                        entryCount = 0
                                    )
                            }
                        }
                        "Month" -> {
                            // Always show all days in the month
                            val firstDayOfMonth = startDate
                            val lastDayOfMonth = endDate
                            val daysInMonth = firstDayOfMonth.until(lastDayOfMonth).days + 1
                            
                            (0 until daysInMonth).map { dayOffset ->
                                val date = firstDayOfMonth.plusDays(dayOffset.toLong())
                                apiData.firstOrNull { it.date == date.toString() }
                                    ?: TrendEntry(
                                        date = date.toString(),
                                        calories = 0,
                                        carbs = 0,
                                        fat = 0,
                                        protein = 0,
                                        entryCount = 0
                                    )
                            }
                        }
                        else -> apiData
                    }
                    
                    chartData = completeData
                } else {
                    errorMessage = "Failed to load data"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred"
                android.util.Log.e("CoachScreen", "Error fetching trends", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    // Load data on screen load and when period changes
    LaunchedEffect(selectedPeriod) {
        fetchCalorieTrends(selectedPeriod)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Back button and Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                androidx.compose.material.Icon(
                    imageVector = ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Calorie Tracking",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        val subtitle = when (selectedPeriod) {
            "Day" -> "Stay within your daily calorie budget"
            "Week" -> "This week's calorie budget tracking (Mon-Sun)"
            "Month" -> {
                val monthName = java.time.LocalDate.now().month.toString().lowercase().replaceFirstChar { it.uppercase() }
                val daysInMonth = java.time.LocalDate.now().lengthOfMonth()
                "$monthName calorie budget tracking (1-$daysInMonth)"
            }
            else -> "Track your calorie budget over time"
        }
        
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Period Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Day", "Week", "Month").forEach { period ->
                    Button(
                        onClick = { selectedPeriod = period },
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPeriod == period) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                Color(0xFFE8E8E8)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = period,
                            color = if (selectedPeriod == period) Color.White else Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calorie Budget",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Below goal indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF22B27D),
                                                Color(0xFF1A8A63)
                                            )
                                        ),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Text(
                                text = "Below",
                                fontSize = 9.sp,
                                color = Color(0xFF999999)
                            )
                        }
                        
                        // Over budget indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                Color(0xFFEF5350),
                                                Color(0xFFE53935)
                                            )
                                        ),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Text(
                                text = "Over Goal",
                                fontSize = 9.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage ?: "Error loading data",
                                    color = Color(0xFF999999),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    chartData.isEmpty() || chartData.all { it.calories == 0 } -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "📊",
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No data available for this period",
                                    color = Color(0xFF999999),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Start logging your meals to see your trends!",
                                    fontSize = 12.sp,
                                    color = Color(0xFFBBBBBB),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        SimpleCalorieChart(
                            data = chartData,
                            dailyGoal = dailyGoal,
                            selectedPeriod = selectedPeriod
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Statistics Card - only show if there's actual data (not all zeros)
        if (chartData.isNotEmpty() && chartData.any { it.calories > 0 }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Statistics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Calculate stats excluding zero values for average
                    val nonZeroData = chartData.filter { it.calories > 0 }
                    val avgCalories = if (nonZeroData.isNotEmpty()) {
                        nonZeroData.map { it.calories }.average().toInt()
                    } else {
                        0
                    }
                    val maxCalories = chartData.maxByOrNull { it.calories }?.calories ?: 0
                    val minCalories = chartData.filter { it.calories > 0 }.minByOrNull { it.calories }?.calories ?: 0
                    val daysWithinGoal = chartData.count { it.calories > 0 && it.calories <= dailyGoal }
                    val daysOverGoal = chartData.count { it.calories > dailyGoal }
                    val totalDaysWithData = chartData.count { it.calories > 0 }
                    
                    StatRow(label = "Average", value = "$avgCalories kcal", color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "Highest", value = "$maxCalories kcal", color = Color(0xFFE53935))
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "Lowest", value = "$minCalories kcal", color = Color(0xFF22B27D))
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "Daily Budget", value = "$dailyGoal kcal", color = Color(0xFFFF9800))
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "Within Budget", value = "$daysWithinGoal / $totalDaysWithData days", color = Color(0xFF22B27D))
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "Over Budget", value = "$daysOverGoal / $totalDaysWithData days", color = Color(0xFFE53935))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp)) // Space for bottom nav
    }
}

@Composable
fun SimpleCalorieChart(
    data: List<TrendEntry>,
    dailyGoal: Int,
    selectedPeriod: String
) {
    val maxCalories = maxOf(
        data.maxOfOrNull { it.calories } ?: 0,
        dailyGoal
    )
    val chartHeight = 250.dp
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            // Goal label overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp, top = 4.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Budget:",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$dailyGoal",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val width = size.width
                val height = size.height
                val barSpacing = 8.dp.toPx()
                val barWidth = if (data.isNotEmpty()) {
                    (width - (data.size + 1) * barSpacing) / data.size
                } else {
                    0f
                }
                
                // Draw goal line
                val goalY = height - (dailyGoal.toFloat() / maxCalories * height * 0.9f)
                drawLine(
                    color = androidx.compose.ui.graphics.Color(0xFFFF9800),
                    start = androidx.compose.ui.geometry.Offset(0f, goalY),
                    end = androidx.compose.ui.geometry.Offset(width, goalY),
                    strokeWidth = 3.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                )
                
                // Draw bars
                data.forEachIndexed { index, entry ->
                    val barHeight = if (entry.calories == 0) {
                        // Show a minimal bar for zero values
                        4.dp.toPx()
                    } else {
                        (entry.calories.toFloat() / maxCalories * height * 0.9f)
                    }
                    val x = barSpacing + index * (barWidth + barSpacing)
                    val y = height - barHeight
                    
                // Gradient colors - different colors based on staying within budget
                val (color1, color2, alpha) = when {
                    entry.calories == 0 -> Triple(
                        androidx.compose.ui.graphics.Color(0xFF22B27D),
                        androidx.compose.ui.graphics.Color(0xFF1A8A63),
                        0.3f
                    )
                    entry.calories > dailyGoal -> Triple(
                        // Red/warning gradient when over budget (bad)
                        androidx.compose.ui.graphics.Color(0xFFEF5350),
                        androidx.compose.ui.graphics.Color(0xFFE53935),
                        1f
                    )
                    else -> Triple(
                        // Green gradient when within budget (good)
                        androidx.compose.ui.graphics.Color(0xFF22B27D),
                        androidx.compose.ui.graphics.Color(0xFF1A8A63),
                        1f
                    )
                }
                    
                    val gradient = Brush.verticalGradient(
                        colors = listOf(color1.copy(alpha = alpha), color2.copy(alpha = alpha)),
                        startY = y,
                        endY = height
                    )
                    
                    drawRoundRect(
                        brush = gradient,
                        topLeft = androidx.compose.ui.geometry.Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                }
            }
            
            // Labels - positioned below the chart for Day and Week
            if (selectedPeriod != "Month") {
                // Show labels for all days in Day and Week views
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (entry in data) {
                        val label = when (selectedPeriod) {
                            "Day" -> "Today"
                            "Week" -> {
                                // Show day abbreviation (e.g., "Mon", "Tue", "Wed")
                                try {
                                    val date = java.time.LocalDate.parse(entry.date)
                                    date.dayOfWeek.toString().take(3)
                                } catch (e: Exception) {
                                    entry.date.takeLast(2)
                                }
                            }
                            else -> ""
                        }
                        
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = Color(0xFF999999),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Month labels below the chart (only for Month view)
        if (selectedPeriod == "Month") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val keyDays = listOf(1, 5, 10, 15, 20, 25, 30)
                keyDays.forEach { day ->
                    Text(
                        text = day.toString(),
                        fontSize = 10.sp,
                        color = Color(0xFF999999),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
    }
}

@Composable
fun WaterDetailScreen(
    userId: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val ArrowBack = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State for selected time period
    var selectedPeriod by remember { mutableStateOf("Week") }
    
    // State for chart data
    var chartData by remember { mutableStateOf<List<com.example.forkit.data.models.WaterTrendEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var dailyGoal by remember { mutableStateOf(2000) }
    
    // Function to fetch water trends
    fun fetchWaterTrends(period: String) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Fetch user goals first
                val goalsResponse = RetrofitClient.api.getUserGoals(userId)
                if (goalsResponse.isSuccessful && goalsResponse.body()?.success == true) {
                    dailyGoal = goalsResponse.body()?.data?.dailyWater ?: 2000
                }
                
                // Calculate date range based on period
                val today = java.time.LocalDate.now()
                val (startDate, endDate) = when (period) {
                    "Day" -> today to today
                    "Week" -> {
                        val dayOfWeek = today.dayOfWeek.value
                        val monday = today.minusDays((dayOfWeek - 1).toLong())
                        val sunday = monday.plusDays(6)
                        monday to sunday
                    }
                    "Month" -> {
                        val firstDay = today.withDayOfMonth(1)
                        val lastDay = today.withDayOfMonth(today.lengthOfMonth())
                        firstDay to lastDay
                    }
                    else -> today to today
                }
                
                // Fetch water trends
                val response = RetrofitClient.api.getWaterTrends(
                    userId = userId,
                    startDate = startDate.toString(),
                    endDate = endDate.toString(),
                    groupBy = "day"
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val apiData = response.body()?.data?.trends ?: emptyList()
                    
                    // Create complete list with zeros for missing days
                    val completeData = when (period) {
                        "Day" -> listOf(
                            apiData.firstOrNull { it.date == endDate.toString() }
                                ?: com.example.forkit.data.models.WaterTrendEntry(
                                    date = endDate.toString(),
                                    amount = 0,
                                    entries = 0
                                )
                        )
                        "Week" -> (0..6).map { dayOffset ->
                            val date = startDate.plusDays(dayOffset.toLong())
                            apiData.firstOrNull { it.date == date.toString() }
                                ?: com.example.forkit.data.models.WaterTrendEntry(
                                    date = date.toString(),
                                    amount = 0,
                                    entries = 0
                                )
                        }
                        "Month" -> {
                            val daysInMonth = startDate.until(endDate).days + 1
                            (0 until daysInMonth).map { dayOffset ->
                                val date = startDate.plusDays(dayOffset.toLong())
                                apiData.firstOrNull { it.date == date.toString() }
                                    ?: com.example.forkit.data.models.WaterTrendEntry(
                                        date = date.toString(),
                                        amount = 0,
                                        entries = 0
                                    )
                            }
                        }
                        else -> apiData
                    }
                    
                    chartData = completeData
                } else {
                    errorMessage = "Failed to load data"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred"
                android.util.Log.e("WaterDetailScreen", "Error fetching trends", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    // Load data on screen load and when period changes
    LaunchedEffect(selectedPeriod) {
        fetchWaterTrends(selectedPeriod)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Back button and Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                androidx.compose.material.Icon(
                    imageVector = ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Water Tracking",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        val subtitle = when (selectedPeriod) {
            "Day" -> "Today's water intake"
            "Week" -> "This week's water intake (Mon-Sun)"
            "Month" -> {
                val monthName = java.time.LocalDate.now().month.toString().lowercase().replaceFirstChar { it.uppercase() }
                val daysInMonth = java.time.LocalDate.now().lengthOfMonth()
                "$monthName water intake (1-$daysInMonth)"
            }
            else -> "Track your water intake over time"
        }
        
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Period Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Day", "Week", "Month").forEach { period ->
                    Button(
                        onClick = { selectedPeriod = period },
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPeriod == period) 
                                MaterialTheme.colorScheme.secondary 
                            else 
                                Color(0xFFE8E8E8)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = period,
                            color = if (selectedPeriod == period) Color.White else Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Water Intake",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.secondary,
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Text(
                                text = "Below",
                                fontSize = 9.sp,
                                color = Color(0xFF999999)
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF2196F3),
                                                Color(0xFF1976D2)
                                            )
                                        ),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Text(
                                text = "Goal Met",
                                fontSize = 9.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "⚠️", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage ?: "Error loading data",
                                    color = Color(0xFF999999),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    chartData.isEmpty() || chartData.all { it.amount == 0 } -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "💧", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No data available for this period",
                                    color = Color(0xFF999999),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Start logging your water intake!",
                                    fontSize = 12.sp,
                                    color = Color(0xFFBBBBBB),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        WaterChart(
                            data = chartData,
                            dailyGoal = dailyGoal,
                            selectedPeriod = selectedPeriod
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Statistics Card
        if (chartData.isNotEmpty() && chartData.any { it.amount > 0 }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Statistics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val nonZeroData = chartData.filter { it.amount > 0 }
                    val avgWater = if (nonZeroData.isNotEmpty()) {
                        nonZeroData.map { it.amount }.average().toInt()
                    } else {
                        0
                    }
                    val maxWater = chartData.maxByOrNull { it.amount }?.amount ?: 0
                    val minWater = chartData.filter { it.amount > 0 }.minByOrNull { it.amount }?.amount ?: 0
                    val daysGoalMet = chartData.count { it.amount >= dailyGoal }
                    val totalDaysWithData = chartData.count { it.amount > 0 }
                    
                    StatRow(label = "Average", value = "$avgWater ml", color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "Highest", value = "$maxWater ml", color = Color(0xFF2196F3))
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "Lowest", value = "$minWater ml", color = Color(0xFF03A9F4))
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "Daily Goal", value = "$dailyGoal ml", color = Color(0xFF2196F3))
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "Goal Met", value = "$daysGoalMet / $totalDaysWithData days", color = Color(0xFF64B5F6))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp)) // Space for bottom nav
    }
}

@Composable
fun WorkoutDetailScreen(
    userId: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val ArrowBack = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val workoutColor = Color(0xFF673AB7) // Dark purple
    
    // State for selected time period
    var selectedPeriod by remember { mutableStateOf("Week") }
    
    // State for chart data
    var chartData by remember { mutableStateOf<List<com.example.forkit.data.models.ExerciseTrendEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Function to fetch exercise trends
    fun fetchExerciseTrends(period: String) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Calculate date range based on period
                val today = java.time.LocalDate.now()
                val (startDate, endDate) = when (period) {
                    "Day" -> today to today
                    "Week" -> {
                        val dayOfWeek = today.dayOfWeek.value
                        val monday = today.minusDays((dayOfWeek - 1).toLong())
                        val sunday = monday.plusDays(6)
                        monday to sunday
                    }
                    "Month" -> {
                        val firstDay = today.withDayOfMonth(1)
                        val lastDay = today.withDayOfMonth(today.lengthOfMonth())
                        firstDay to lastDay
                    }
                    else -> today to today
                }
                
                // Fetch exercise trends
                val response = RetrofitClient.api.getExerciseTrends(
                    userId = userId,
                    startDate = startDate.toString(),
                    endDate = endDate.toString(),
                    groupBy = "day"
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val apiData = response.body()?.data?.trends ?: emptyList()
                    
                    // Create complete list with zeros for missing days
                    val completeData = when (period) {
                        "Day" -> listOf(
                            apiData.firstOrNull { it.date == endDate.toString() }
                                ?: com.example.forkit.data.models.ExerciseTrendEntry(
                                    date = endDate.toString(),
                                    calories = 0,
                                    duration = 0,
                                    exercises = 0,
                                    cardioCount = 0,
                                    strengthCount = 0
                                )
                        )
                        "Week" -> (0..6).map { dayOffset ->
                            val date = startDate.plusDays(dayOffset.toLong())
                            apiData.firstOrNull { it.date == date.toString() }
                                ?: com.example.forkit.data.models.ExerciseTrendEntry(
                                    date = date.toString(),
                                    calories = 0,
                                    duration = 0,
                                    exercises = 0,
                                    cardioCount = 0,
                                    strengthCount = 0
                                )
                        }
                        "Month" -> {
                            val daysInMonth = startDate.until(endDate).days + 1
                            (0 until daysInMonth).map { dayOffset ->
                                val date = startDate.plusDays(dayOffset.toLong())
                                apiData.firstOrNull { it.date == date.toString() }
                                    ?: com.example.forkit.data.models.ExerciseTrendEntry(
                                        date = date.toString(),
                                        calories = 0,
                                        duration = 0,
                                        exercises = 0,
                                        cardioCount = 0,
                                        strengthCount = 0
                                    )
                            }
                        }
                        else -> apiData
                    }
                    
                    chartData = completeData
                } else {
                    errorMessage = "Failed to load data"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred"
                android.util.Log.e("WorkoutDetailScreen", "Error fetching trends", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    // Load data on screen load and when period changes
    LaunchedEffect(selectedPeriod) {
        fetchExerciseTrends(selectedPeriod)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Back button and Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                androidx.compose.material.Icon(
                    imageVector = ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Workout Tracking",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = workoutColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        val subtitle = when (selectedPeriod) {
            "Day" -> "Today's calories burned"
            "Week" -> "This week's calories burned (Mon-Sun)"
            "Month" -> {
                val monthName = java.time.LocalDate.now().month.toString().lowercase().replaceFirstChar { it.uppercase() }
                val daysInMonth = java.time.LocalDate.now().lengthOfMonth()
                "$monthName calories burned (1-$daysInMonth)"
            }
            else -> "Track your workout calories over time"
        }
        
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Period Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Day", "Week", "Month").forEach { period ->
                    Button(
                        onClick = { selectedPeriod = period },
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPeriod == period) 
                                workoutColor 
                            else 
                                Color(0xFFE8E8E8)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = period,
                            color = if (selectedPeriod == period) Color.White else Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(2.dp, workoutColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calories Burned",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                workoutColor,
                                                workoutColor.copy(alpha = 0.8f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Text(
                                text = "Workout",
                                fontSize = 9.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = workoutColor
                            )
                        }
                    }
                    errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "⚠️", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage ?: "Error loading data",
                                    color = Color(0xFF999999),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    chartData.isEmpty() || chartData.all { it.calories == 0 } -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "💪", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No data available for this period",
                                    color = Color(0xFF999999),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Start logging your workouts!",
                                    fontSize = 12.sp,
                                    color = Color(0xFFBBBBBB),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        WorkoutChart(
                            data = chartData,
                            selectedPeriod = selectedPeriod
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Statistics Card
        if (chartData.isNotEmpty() && chartData.any { it.calories > 0 }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Statistics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val nonZeroData = chartData.filter { it.calories > 0 }
                    val avgCalories = if (nonZeroData.isNotEmpty()) {
                        nonZeroData.map { it.calories }.average().toInt()
                    } else {
                        0
                    }
                    val maxCalories = chartData.maxByOrNull { it.calories }?.calories ?: 0
                    val minCalories = chartData.filter { it.calories > 0 }.minByOrNull { it.calories }?.calories ?: 0
                    val totalDaysWithData = chartData.count { it.calories > 0 }
                    val totalWorkouts = chartData.sumOf { it.exercises }
                    
                    StatRow(label = "Average", value = "$avgCalories kcal", color = workoutColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "Highest", value = "$maxCalories kcal", color = Color(0xFF9C27B0))
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "Lowest", value = "$minCalories kcal", color = Color(0xFF7B1FA2))
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "Active Days", value = "$totalDaysWithData days", color = Color(0xFF8E24AA))
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "Total Workouts", value = "$totalWorkouts exercises", color = Color(0xFFBA68C8))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp)) // Space for bottom nav
    }
}

@Composable
fun WaterChart(
    data: List<com.example.forkit.data.models.WaterTrendEntry>,
    dailyGoal: Int,
    selectedPeriod: String
) {
    val maxAmount = maxOf(
        data.maxOfOrNull { it.amount } ?: 0,
        dailyGoal
    )
    val chartHeight = 250.dp
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            // Goal label overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp, top = 4.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Goal:",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$dailyGoal ml",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val width = size.width
                val height = size.height
                val barSpacing = 8.dp.toPx()
                val barWidth = if (data.isNotEmpty()) {
                    (width - (data.size + 1) * barSpacing) / data.size
                } else {
                    0f
                }
                
                // Draw goal line
                val goalY = height - (dailyGoal.toFloat() / maxAmount * height * 0.9f)
                drawLine(
                    color = androidx.compose.ui.graphics.Color(0xFF2196F3),
                    start = androidx.compose.ui.geometry.Offset(0f, goalY),
                    end = androidx.compose.ui.geometry.Offset(width, goalY),
                    strokeWidth = 3.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                )
                
                // Draw bars
                data.forEachIndexed { index, entry ->
                    val barHeight = if (entry.amount == 0) {
                        4.dp.toPx()
                    } else {
                        (entry.amount.toFloat() / maxAmount * height * 0.9f)
                    }
                    val x = barSpacing + index * (barWidth + barSpacing)
                    val y = height - barHeight
                    
                    // Color based on goal achievement
                    val (color1, color2, alpha) = when {
                        entry.amount == 0 -> Triple(
                            androidx.compose.ui.graphics.Color(0xFF42A5F5),
                            androidx.compose.ui.graphics.Color(0xFF1E88E5),
                            0.3f
                        )
                        entry.amount >= dailyGoal -> Triple(
                            androidx.compose.ui.graphics.Color(0xFF2196F3),
                            androidx.compose.ui.graphics.Color(0xFF1976D2),
                            1f
                        )
                        else -> Triple(
                            androidx.compose.ui.graphics.Color(0xFF42A5F5),
                            androidx.compose.ui.graphics.Color(0xFF1E88E5),
                            1f
                        )
                    }
                    
                    val gradient = Brush.verticalGradient(
                        colors = listOf(color1.copy(alpha = alpha), color2.copy(alpha = alpha)),
                        startY = y,
                        endY = height
                    )
                    
                    drawRoundRect(
                        brush = gradient,
                        topLeft = androidx.compose.ui.geometry.Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                }
            }
            
            // Labels for Day and Week
            if (selectedPeriod != "Month") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (entry in data) {
                        val label = when (selectedPeriod) {
                            "Day" -> "Today"
                            "Week" -> {
                                try {
                                    val date = java.time.LocalDate.parse(entry.date)
                                    date.dayOfWeek.toString().take(3)
                                } catch (e: Exception) {
                                    entry.date.takeLast(2)
                                }
                            }
                            else -> ""
                        }
                        
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = Color(0xFF999999),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Month labels
        if (selectedPeriod == "Month") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val keyDays = listOf(1, 5, 10, 15, 20, 25, 30)
                keyDays.forEach { day ->
                    Text(
                        text = day.toString(),
                        fontSize = 10.sp,
                        color = Color(0xFF999999),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutChart(
    data: List<com.example.forkit.data.models.ExerciseTrendEntry>,
    selectedPeriod: String
) {
    val workoutColor = Color(0xFF673AB7)
    val maxCalories = data.maxOfOrNull { it.calories } ?: 0
    val chartHeight = 250.dp
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val width = size.width
                val height = size.height
                val barSpacing = 8.dp.toPx()
                val barWidth = if (data.isNotEmpty()) {
                    (width - (data.size + 1) * barSpacing) / data.size
                } else {
                    0f
                }
                
                // Draw bars
                data.forEachIndexed { index, entry ->
                    val barHeight = if (entry.calories == 0) {
                        4.dp.toPx()
                    } else {
                        (entry.calories.toFloat() / maxCalories * height * 0.9f)
                    }
                    val x = barSpacing + index * (barWidth + barSpacing)
                    val y = height - barHeight
                    
                    val alpha = if (entry.calories == 0) 0.3f else 1f
                    val gradient = Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color(0xFF673AB7).copy(alpha = alpha),
                            androidx.compose.ui.graphics.Color(0xFF512DA8).copy(alpha = alpha)
                        ),
                        startY = y,
                        endY = height
                    )
                    
                    drawRoundRect(
                        brush = gradient,
                        topLeft = androidx.compose.ui.geometry.Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                }
            }
            
            // Labels for Day and Week
            if (selectedPeriod != "Month") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (entry in data) {
                        val label = when (selectedPeriod) {
                            "Day" -> "Today"
                            "Week" -> {
                                try {
                                    val date = java.time.LocalDate.parse(entry.date)
                                    date.dayOfWeek.toString().take(3)
                                } catch (e: Exception) {
                                    entry.date.takeLast(2)
                                }
                            }
                            else -> ""
                        }
                        
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = Color(0xFF999999),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Month labels
        if (selectedPeriod == "Month") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val keyDays = listOf(1, 5, 10, 15, 20, 25, 30)
                keyDays.forEach { day ->
                    Text(
                        text = day.toString(),
                        fontSize = 10.sp,
                        color = Color(0xFF999999),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Note: BottomNavigationBar and FloatingIcons are defined in DashboardActivity.kt and reused here

