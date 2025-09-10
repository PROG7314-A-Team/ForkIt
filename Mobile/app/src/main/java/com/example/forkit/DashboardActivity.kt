package com.example.forkit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.ui.theme.ForkItTheme

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForkItTheme {
                DashboardScreen()
            }
        }
    }
}

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var selectedPeriod by remember { mutableStateOf("Today") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    
    // Daily calorie goal (user will set this later)
    val dailyGoal = 2000
    
    // Testing data for different periods (consumed calories for progress bar)
    val periodData = mapOf(
        "Today" to mapOf("consumed" to 1650, "burned" to 300, "total" to 1350), // 82.5% of daily goal
        "This Month" to mapOf("consumed" to 2500, "burned" to 1500, "total" to 1000), // 125% - Goal exceeded
        "Last Month" to mapOf("consumed" to 1800, "burned" to 1200, "total" to 600) // 90% of daily goal
    )
    
    val currentData = periodData[selectedPeriod] ?: periodData["Today"]!!
    val consumed = currentData["consumed"]!!
    val burned = currentData["burned"]!!
    val total = currentData["total"]!!
    val remaining = consumed - burned
    
    // Progress bar calculations
    val progressPercentage = (consumed.toFloat() / dailyGoal).coerceIn(0f, 1f)
    val caloriesRemaining = dailyGoal - consumed
    val isGoalReached = consumed >= dailyGoal
    
    // Animated progress for smooth transitions
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercentage,
        animationSpec = tween(1000)
    )
    
    // Macronutrient constants (testing data)
    val carbsCalories = 600
    val proteinCalories = 400
    val fatCalories = 200
    val totalCalories = carbsCalories + proteinCalories + fatCalories
    
    // Animated blur radius
    val animatedBlurRadius by animateFloatAsState(
        targetValue = if (selectedTab == 2) 8f else 0f,
        animationSpec = tween(300)
    )
    
    // Animated overlay alpha
    val animatedOverlayAlpha by animateFloatAsState(
        targetValue = if (selectedTab == 2) 0.3f else 0f,
        animationSpec = tween(300)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Screen Content based on selected tab
            when (selectedTab) {
                0 -> {
                    // Home/Dashboard content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .blur(radius = animatedBlurRadius.dp)
                    ) {
                // Profile Button at the top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Card(
                        modifier = Modifier.clickable { 
                            val intent = Intent(context, ProfileActivity::class.java)
                            context.startActivity(intent)
                        },
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF22B27D))
                    ) {
                        Box(
                            modifier = Modifier.padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                // Total Caloric Intake Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                   Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF22B27D), // ForkIt Green
                                        Color(0xFF1E9ECD)  // ForkIt Blue
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total Caloric Intake",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$total kcal",
                                fontSize = 32.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Period Selection
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { isDropdownExpanded = !isDropdownExpanded },
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedPeriod,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333333)
                        )
                        
                        // Dropdown Arrow
                        Icon(
                            imageVector = if (isDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = Color(0xFF22B27D)
                        )
                    }
                }
                
                // Dropdown Menu
                if (isDropdownExpanded) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column {
                            listOf("Today", "This Month", "Last Month").forEach { period ->
                                Text(
                                    text = period,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedPeriod = period
                                            isDropdownExpanded = false
                                        }
                                        .padding(16.dp),
                                    fontSize = 16.sp,
                                    color = if (selectedPeriod == period) Color(0xFF22B27D) else Color(0xFF333333)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF22B27D))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Consumed",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$consumed kcal",
                                fontSize = 24.sp,
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E9ECD))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Burned",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$burned kcal",
                                fontSize = 24.sp,
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                text = "Daily Goal Progress",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = "$consumed / $dailyGoal kcal",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGoalReached) Color(0xFF22B27D) else Color(0xFF666666)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Progress Bar
                        LinearProgressIndicator(
                            progress = animatedProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = if (isGoalReached) Color(0xFF22B27D) else Color(0xFF1E9ECD),
                            trackColor = Color(0xFFE0E0E0)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Progress Text
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(animatedProgress * 100).toInt()}% Complete",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = if (isGoalReached) "Goal Reached! 🎉" else "$caloriesRemaining kcal remaining",
                                fontSize = 12.sp,
                                color = if (isGoalReached) Color(0xFF22B27D) else Color(0xFF666666),
                                fontWeight = if (isGoalReached) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Calorie Wheel Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Calorie Wheel
                        CalorieWheel(
                            carbsCalories = carbsCalories,
                            proteinCalories = proteinCalories,
                            fatCalories = fatCalories,
                            totalCalories = totalCalories
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Macronutrient Breakdown
                        MacronutrientBreakdown(
                            carbsCalories = carbsCalories,
                            proteinCalories = proteinCalories,
                            fatCalories = fatCalories,
                            totalCalories = totalCalories
                        )
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF87CEEB))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
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
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "6/8 glasses",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Steps Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF22B27D))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
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
                            Text(
                                text = "8,247",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Recent Meals",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333333)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Meal items
                        listOf(
                            "🍳 Breakfast - 450 kcal",
                            "🥗 Lunch - 320 kcal", 
                            "🍎 Snack - 150 kcal"
                        ).forEach { meal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = meal,
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                                Text(
                                    text = "2h ago",
                                    fontSize = 12.sp,
                                    color = Color(0xFF999999)
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
                            .blur(radius = animatedBlurRadius.dp)
                    ) {
                        MealsScreen()
                    }
                }
                2 -> {
                    // Add functionality is handled below
                    Spacer(modifier = Modifier.weight(1f))
                }
                3 -> {
                    // Habits Screen
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .blur(radius = animatedBlurRadius.dp)
                    ) {
                        HabitsScreen()
                    }
                }
                4 -> {
                    // Coach Screen
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .blur(radius = animatedBlurRadius.dp)
                    ) {
                        CoachScreen()
                    }
                }
            }
            
            // Bottom Navigation (always clickable)
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
        
        // Blurred overlay (only when Add tab is selected)
        if (selectedTab == 2) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(animatedOverlayAlpha)
                    .background(Color.Black)
                    .clickable { selectedTab = 0 } // Return to Home tab
            )
            
            // Add Menu Options
            AddMenu(
                isExpanded = true,
                onDismiss = { selectedTab = 0 } // Return to Home tab
            )
        }
        
    }
}

@Composable
fun CalorieWheel(
    carbsCalories: Int,
    proteinCalories: Int,
    fatCalories: Int,
    totalCalories: Int
) {
    val carbsProgress = carbsCalories.toFloat() / totalCalories
    val proteinProgress = proteinCalories.toFloat() / totalCalories
    val fatProgress = fatCalories.toFloat() / totalCalories
    
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
    
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = 20.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            // Background circle
            drawCircle(
                color = Color(0xFFE0E0E0),
                radius = radius,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            var currentAngle = -90f
            
            // Carbs segment (Dark Blue)
            drawArc(
                color = Color(0xFF1565C0), // Dark Blue
                startAngle = currentAngle,
                sweepAngle = 360f * animatedCarbs,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            currentAngle += 360f * animatedCarbs
            
            // Protein segment (Blue)
            drawArc(
                color = Color(0xFF1E9ECD), // ForkIt Blue
                startAngle = currentAngle,
                sweepAngle = 360f * animatedProtein,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            currentAngle += 360f * animatedProtein
            
            // Fat segment (Light Blue)
            drawArc(
                color = Color(0xFF87CEEB), // Light Blue
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
            Text(
                text = totalCalories.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22B27D) // ForkIt Green
            )
            Text(
                text = "Total Calories",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Carbs
        MacronutrientItem(
            name = "Carbs",
            calories = carbsCalories,
            color = Color(0xFF1565C0), // Dark Blue
            percentage = (carbsCalories * 100 / totalCalories)
        )
        
        // Protein
        MacronutrientItem(
            name = "Protein",
            calories = proteinCalories,
            color = Color(0xFF1E9ECD), // ForkIt Blue
            percentage = (proteinCalories * 100 / totalCalories)
        )
        
        // Fat
        MacronutrientItem(
            name = "Fat",
            calories = fatCalories,
            color = Color(0xFF87CEEB), // Light Blue
            percentage = (fatCalories * 100 / totalCalories)
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )
        
        Text(
            text = "${calories}cal",
            fontSize = 10.sp,
            color = Color(0xFF666666)
        )
        
        Text(
            text = "${percentage}%",
            fontSize = 10.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Meals",
                    tint = if (selectedTab == 1) Color.White else Color.White.copy(alpha = 0.7f)
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
                modifier = Modifier.clickable { onTabSelected(2) }
            ) {
                Card(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color(0xFF22B27D),
                            modifier = Modifier.rotate(
                                animateFloatAsState(
                                    targetValue = if (selectedTab == 2) 45f else 0f,
                                    animationSpec = tween(300)
                                ).value
                            )
                        )
                    }
                }
                Text(
                    text = "Add",
                    fontSize = 12.sp,
                    color = if (selectedTab == 2) Color.White else Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Habits Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(3) }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Habits",
                    tint = if (selectedTab == 3) Color.White else Color.White.copy(alpha = 0.7f)
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
fun AddMenu(
    isExpanded: Boolean,
    onDismiss: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(300)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Add Exercise
            AddMenuItem(
                text = "Add Exercise",
                icon = "🏃", // Exercise emoji
                onClick = { /* TODO: Add exercise functionality */ },
                alpha = animatedAlpha
            )
            
            // Add Water
            AddMenuItem(
                text = "Add Water",
                icon = "💧", // Water emoji
                onClick = { /* TODO: Add water functionality */ },
                alpha = animatedAlpha
            )
            
            // Add Food
            AddMenuItem(
                text = "Add Food",
                icon = "🍎", // Food emoji
                onClick = { /* TODO: Add food functionality */ },
                alpha = animatedAlpha
            )
        }
    }
}

@Composable
fun AddMenuItem(
    text: String,
    icon: String,
    onClick: () -> Unit,
    alpha: Float
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .alpha(alpha),
        shape = RoundedCornerShape(25.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
        }
    }
}

@Composable
fun MealsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🍽️",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Meals",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF22B27D)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Track your meals and nutrition",
            fontSize = 16.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Coming Soon",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "Meal tracking features will be available here",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun HabitsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚙️",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Habits",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF22B27D)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Build and track healthy habits",
            fontSize = 16.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Coming Soon",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "Habit tracking features will be available here",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CoachScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "👨‍🏫",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Coach",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF22B27D)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Get personalized health guidance",
            fontSize = 16.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Coming Soon",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "AI coaching features will be available here",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
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

