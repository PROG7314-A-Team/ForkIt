package com.example.forkit

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
import androidx.compose.material3.*
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
    var isAddMenuExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedPeriod by remember { mutableStateOf("This Month") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    
    // Testing data for different periods
    val periodData = mapOf(
        "This Month" to mapOf("consumed" to 2500, "burned" to 1500, "total" to 1000),
        "This Week" to mapOf("consumed" to 800, "burned" to 400, "total" to 400),
        "Today" to mapOf("consumed" to 1200, "burned" to 300, "total" to 900)
    )
    
    val currentData = periodData[selectedPeriod] ?: periodData["This Month"]!!
    val consumed = currentData["consumed"]!!
    val burned = currentData["burned"]!!
    val total = currentData["total"]!!
    val remaining = consumed - burned
    
    // Macronutrient constants (testing data)
    val carbsCalories = 600
    val proteinCalories = 400
    val fatCalories = 200
    val totalCalories = carbsCalories + proteinCalories + fatCalories
    
    // Animated blur radius
    val animatedBlurRadius by animateFloatAsState(
        targetValue = if (isAddMenuExpanded) 8f else 0f,
        animationSpec = tween(300)
    )
    
    // Animated overlay alpha
    val animatedOverlayAlpha by animateFloatAsState(
        targetValue = if (isAddMenuExpanded) 0.3f else 0f,
        animationSpec = tween(300)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = animatedBlurRadius.dp)
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedPeriod,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Dropdown Arrow
                    Icon(
                        imageVector = if (isDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = Color(0xFF22B27D),
                        modifier = Modifier.clickable { isDropdownExpanded = !isDropdownExpanded }
                    )
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
                            listOf("This Month", "This Week", "Today").forEach { period ->
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
                
                // Remaining to Consume Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Green portion
                        Box(
                            modifier = Modifier
                                .weight(0.7f)
                                .background(Color(0xFF22B27D))
                                .padding(20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Remaining to Consume",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Blue portion
                        Box(
                            modifier = Modifier
                                .weight(0.3f)
                                .background(Color(0xFF1E9ECD))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$remaining kcal",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
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
            }
            
            // Bottom Navigation
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
        
        // Blurred overlay (always present but animated)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(animatedOverlayAlpha)
                .background(Color.Black)
                .clickable { isAddMenuExpanded = false }
        )
        
        // Expandable Add Menu
        if (isAddMenuExpanded) {
            // Add Menu Options
            AddMenu(
                isExpanded = isAddMenuExpanded,
                onDismiss = { isAddMenuExpanded = false }
            )
        }
        
        // Floating Add Button
        FloatingActionButton(
            onClick = { isAddMenuExpanded = !isAddMenuExpanded },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF22B27D), // ForkIt Green
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.rotate(
                    animateFloatAsState(
                        targetValue = if (isAddMenuExpanded) 45f else 0f,
                        animationSpec = tween(300)
                    ).value
                )
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
            
            // Habits Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(2) }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Habits",
                    tint = if (selectedTab == 2) Color.White else Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "Habits",
                    fontSize = 12.sp,
                    color = if (selectedTab == 2) Color.White else Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Coach Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(3) }
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Coach",
                    tint = if (selectedTab == 3) Color.White else Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "Coach",
                    fontSize = 12.sp,
                    color = if (selectedTab == 3) Color.White else Color.White.copy(alpha = 0.7f)
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

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    ForkItTheme {
        DashboardScreen()
    }
}
