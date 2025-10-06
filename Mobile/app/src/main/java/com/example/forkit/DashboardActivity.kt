package com.example.forkit
import com.example.forkit.*
import com.example.forkit.ui.meals.MealsScreen


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
import kotlinx.coroutines.awaitAll
import com.example.forkit.ui.dashboard.DashboardScreen


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
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300)
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() } // Dismiss when clicking outside
    ) {
        // Floating icons positioned on the right side, similar to the image
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp, top = 200.dp)
                .alpha(animatedAlpha),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Meal Icon
            FloatingIcon(
                icon = R.drawable.ic_meals,
                label = "Meal",
                onClick = { 
                    val intent = Intent(context, AddMealActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    context.startActivity(intent)
                    onDismiss()
                },
                scale = animatedScale
            )
            
            // Water Icon
            FloatingIcon(
                icon = R.drawable.ic_water,
                label = "Water",
                onClick = { 
                    val intent = Intent(context, AddWaterActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    context.startActivity(intent)
                    onDismiss()
                },
                scale = animatedScale
            )
            
            // Workout Icon
            FloatingIcon(
                icon = R.drawable.ic_workout,
                label = "Workout",
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



    @Preview(showBackground = true)
    @Composable
    fun DashboardScreenPreview() {
        ForkItTheme {
            DashboardScreen()
        }
    }

