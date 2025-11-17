package com.example.forkit.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.ProfileActivity
import com.example.forkit.R
import com.example.forkit.StepTracker
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.RecentActivityEntry
import com.example.forkit.data.models.RecentExerciseActivityEntry
import com.example.forkit.data.models.RecentWaterActivityEntry
import com.example.forkit.data.models.StreakData
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    userId: String,
    consumed: Double,
    burned: Double,
    carbsCalories: Double,
    proteinCalories: Double,
    fatCalories: Double,
    waterAmount: Double,
    waterEntries: Int,
    recentMeals: List<RecentActivityEntry>,
    recentWorkouts: List<RecentExerciseActivityEntry>,
    recentWaterLogs: List<RecentWaterActivityEntry>,
    isLoading: Boolean,
    dailyGoal: Int,
    dailyWaterGoal: Int,
    dailyStepsGoal: Int,
    currentSteps: Int,
    isStepTrackingAvailable: Boolean,
    total: Int,
    progressPercentage: Float,
    caloriesRemaining: Int,
    isOverBudget: Boolean,
    isWithinBudget: Boolean,
    animatedProgress: Float,
    streakData: StreakData?,
    isStreakLoading: Boolean,
    streakErrorMessage: String?,
    refreshData: () -> Unit,
    onMealDelete: (RecentActivityEntry) -> Unit,
    onWorkoutDelete: (RecentExerciseActivityEntry) -> Unit,
    onWaterLogDelete: (RecentWaterActivityEntry) -> Unit
)  {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.profile),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.profile).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
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
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.welcome_dashboard),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.track_health_journey),
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
                        text = stringResource(R.string.todays_caloric_intake),
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
                        text = stringResource(R.string.consumed),
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
                        text = stringResource(R.string.burned),
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
                        text = stringResource(R.string.todays_calorie_budget),
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
                
                // Progress Bar
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
                        text = stringResource(R.string.percent_used, (animatedProgress * 100).toInt()),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isOverBudget) stringResource(R.string.over_budget) else if (caloriesRemaining >= 0) stringResource(R.string.kcal_remaining, caloriesRemaining) else stringResource(R.string.kcal_remaining, 0),
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
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "No meals",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_food_logged_today),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.start_logging_meals),
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
                    com.example.forkit.CalorieWheel(
                        carbsCalories = carbsCalories.toInt(),
                        proteinCalories = proteinCalories.toInt(),
                        fatCalories = fatCalories.toInt(),
                        totalCalories = consumed.toInt(),
                        isLoading = isLoading
                    )
                    
                    // Macronutrient Breakdown (Right side)
                    com.example.forkit.MacronutrientBreakdown(
                        carbsCalories = carbsCalories.toInt(),
                        proteinCalories = proteinCalories.toInt(),
                        fatCalories = fatCalories.toInt(),
                        totalCalories = consumed.toInt()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        StreakSummaryCard(
            streakData = streakData,
            isStreakLoading = isStreakLoading,
            streakErrorMessage = streakErrorMessage,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
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
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Water",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF1E9ECD)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.water),
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
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
                            color = Color(0xFF333333),
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
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Steps",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.steps),
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
                            text = stringResource(R.string.step_tracking_unavailable),
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
                    text = stringResource(R.string.recent_meals),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
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
                        text = stringResource(R.string.no_meals_today),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    // Display real meal items from API
                    var mealToDelete by remember { mutableStateOf<RecentActivityEntry?>(null) }
                    
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
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
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
                                    stringResource(R.string.delete_food_log_title),
                                    fontWeight = FontWeight.Bold
                                ) 
                            },
                            text = { 
                                Text(stringResource(R.string.delete_food_log_message, meal.foodName)) 
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        mealToDelete = null
                                        onMealDelete(meal)
                                        refreshData()
                                        Toast.makeText(context, context.getString(R.string.food_deleted_successfully), Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text(stringResource(R.string.delete), color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { mealToDelete = null }) {
                                    Text(stringResource(R.string.cancel))
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
            border = BorderStroke(2.dp, Color(0xFF673AB7))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.recent_workouts),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (recentWorkouts.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_workouts_today),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    // Display real workout items from API
                    var workoutToDelete by remember { mutableStateOf<RecentExerciseActivityEntry?>(null) }
                    
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
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
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
                                    stringResource(R.string.delete_workout_title),
                                    fontWeight = FontWeight.Bold
                                ) 
                            },
                            text = { 
                                Text(stringResource(R.string.delete_workout_message, workout.name)) 
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        workoutToDelete = null
                                        onWorkoutDelete(workout)
                                        refreshData()
                                        Toast.makeText(context, context.getString(R.string.deleted_successfully), Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text(stringResource(R.string.delete), color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { workoutToDelete = null }) {
                                    Text(stringResource(R.string.cancel))
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
            border = BorderStroke(2.dp, Color(0xFF1E9ECD))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.todays_water_intake),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (recentWaterLogs.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_water_logged_today),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    // Display real water log items from API
                    var waterLogToDelete by remember { mutableStateOf<RecentWaterActivityEntry?>(null) }
                    
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
                                        waterLogToDelete = null
                                        onWaterLogDelete(waterLog)
                                        refreshData()
                                        Toast.makeText(context, context.getString(R.string.deleted_successfully), Toast.LENGTH_SHORT).show()
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

@Composable
private fun StreakSummaryCard(
    streakData: StreakData?,
    isStreakLoading: Boolean,
    streakErrorMessage: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.daily_streak_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (!isStreakLoading && streakErrorMessage == null && streakData != null) {
                    val isActive = streakData.isActive
                    val statusText = if (isActive) {
                        stringResource(R.string.streak_status_active)
                    } else {
                        stringResource(R.string.streak_status_paused)
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = statusText.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            when {
                isStreakLoading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                streakErrorMessage != null -> {
                    Text(
                        text = streakErrorMessage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                streakData == null || streakData.currentStreak <= 0 -> {
                    Text(
                        text = stringResource(R.string.streak_not_started),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                else -> {
                    val formattedStartDate = formatStreakDate(streakData.streakStartDate)
                    val formattedLastLogDate = formatStreakDate(streakData.lastLogDate)
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.streak_days_label, streakData.currentStreak),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = stringResource(R.string.streak_longest_format, streakData.longestStreak),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    if (formattedLastLogDate != null || formattedStartDate != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            formattedLastLogDate?.let {
                                Text(
                                    text = stringResource(R.string.streak_last_logged_format, it),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            if (formattedLastLogDate != null && formattedStartDate != null) {
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            
                            formattedStartDate?.let {
                                Text(
                                    text = stringResource(R.string.streak_since_format, it),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatStreakDate(dateString: String?): String? {
    if (dateString.isNullOrBlank()) return null
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val parsedDate = parser.parse(dateString)
        parsedDate?.let { formatter.format(it) }
    } catch (e: Exception) {
        null
    }
}