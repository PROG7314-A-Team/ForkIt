package com.example.forkit.ui.dashboard

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.R
import com.example.forkit.AddMealActivity
import com.example.forkit.AddWaterActivity
import com.example.forkit.AddWorkoutActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person

/**
 * ✅ DashboardComponents.kt
 *
 * Contains all the smaller composables used by the DashboardScreen.
 * These handle:
 *  - Bottom navigation bar
 *  - Calorie wheel visualization
 *  - Macronutrient breakdown
 *  - Floating action icons for Add Meal, Add Water, Add Workout
 * Everything here is stateless and reusable.
 */

// ------------------------------------------------------------
// 🍩 CALORIE WHEEL
// ------------------------------------------------------------
@Composable
fun CalorieWheel(
    carbsCalories: Int,
    proteinCalories: Int,
    fatCalories: Int,
    totalCalories: Int,
    isLoading: Boolean = false
) {
    // Calculate proportions for each macronutrient
    val carbsProgress = if (totalCalories > 0) carbsCalories.toFloat() / totalCalories else 0f
    val proteinProgress = if (totalCalories > 0) proteinCalories.toFloat() / totalCalories else 0f
    val fatProgress = if (totalCalories > 0) fatCalories.toFloat() / totalCalories else 0f

    // Animate arc transitions smoothly
    val animatedCarbs by animateFloatAsState(targetValue = carbsProgress, animationSpec = tween(1000))
    val animatedProtein by animateFloatAsState(targetValue = proteinProgress, animationSpec = tween(1000))
    val animatedFat by animateFloatAsState(targetValue = fatProgress, animationSpec = tween(1000))

    // Brand colors
    val outlineColor = MaterialTheme.colorScheme.outline
    val carbsColor = Color(0xFF1E9ECD) // ForkIt Blue
    val proteinColor = Color(0xFF22B27D) // ForkIt Green
    val fatColor = Color(0xFF6B4FA0) // ForkIt Purple

    Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            // Background circle
            drawCircle(color = outlineColor, radius = radius, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

            var currentAngle = -90f

            // Carbs arc
            drawArc(
                color = carbsColor,
                startAngle = currentAngle,
                sweepAngle = 360f * animatedCarbs,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            currentAngle += 360f * animatedCarbs

            // Protein arc
            drawArc(
                color = proteinColor,
                startAngle = currentAngle,
                sweepAngle = 360f * animatedProtein,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            currentAngle += 360f * animatedProtein

            // Fat arc
            drawArc(
                color = fatColor,
                startAngle = currentAngle,
                sweepAngle = 360f * animatedFat,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center content inside wheel
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), color = MaterialTheme.colorScheme.primary)
            } else {
                Text(
                    text = totalCalories.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
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

// ------------------------------------------------------------
// 🧬 MACRONUTRIENT BREAKDOWN
// ------------------------------------------------------------
@Composable
fun MacronutrientBreakdown(
    carbsCalories: Int,
    proteinCalories: Int,
    fatCalories: Int,
    totalCalories: Int
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MacronutrientItem(
            name = "Carbs",
            calories = carbsCalories,
            color = Color(0xFF1E9ECD),
            percentage = if (totalCalories > 0) (carbsCalories * 100 / totalCalories) else 0
        )
        MacronutrientItem(
            name = "Protein",
            calories = proteinCalories,
            color = Color(0xFF22B27D),
            percentage = if (totalCalories > 0) (proteinCalories * 100 / totalCalories) else 0
        )
        MacronutrientItem(
            name = "Fat",
            calories = fatCalories,
            color = Color(0xFF6B4FA0),
            percentage = if (totalCalories > 0) (fatCalories * 100 / totalCalories) else 0
        )
    }
}

@Composable
fun MacronutrientItem(name: String, calories: Int, color: Color, percentage: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Column {
            Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "${calories} cal (${percentage}%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

// ------------------------------------------------------------
// 🧭 BOTTOM NAVIGATION BAR
// ------------------------------------------------------------
@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    showFloatingIcons: Boolean,
    onAddButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF22B27D)) // ForkIt Green
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Home
            NavItem(icon = Icons.Default.Home, label = "Home", isSelected = selectedTab == 0) { onTabSelected(0) }

            // Meals
            NavImageItem(
                iconRes = R.drawable.ic_meals,
                label = "Meals",
                isSelected = selectedTab == 1
            ) { onTabSelected(1) }

            // Add
            AddButton(isActive = showFloatingIcons, onAddButtonClick = onAddButtonClick)

            // Habits
            NavImageItem(
                iconRes = R.drawable.ic_habits,
                label = "Habits",
                isSelected = selectedTab == 3
            ) { onTabSelected(3) }

            // Coach
            NavItem(icon = Icons.Default.Info, label = "Coach", isSelected = selectedTab == 4) { onTabSelected(4) }
        }
    }
}

@Composable
private fun NavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(imageVector = icon, contentDescription = label, tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f))
        Text(text = label, fontSize = 12.sp, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun NavImageItem(iconRes: Int, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(if (isSelected) Color.White else Color.White.copy(alpha = 0.7f))
        )
        Text(text = label, fontSize = 12.sp, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun AddButton(isActive: Boolean, onAddButtonClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onAddButtonClick() }) {
        Card(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.rotate(
                        animateFloatAsState(targetValue = if (isActive) 45f else 0f, animationSpec = tween(300)).value
                    )
                )
            }
        }
        Text(
            text = "Add",
            fontSize = 12.sp,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.7f)
        )
    }
}

// ------------------------------------------------------------
// 🟢 FLOATING ACTION ICONS
// ------------------------------------------------------------
@Composable
fun FloatingIcons(
    context: Context,
    userId: String,
    onDismiss: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(targetValue = 1f, animationSpec = tween(300))
    val animatedScale by animateFloatAsState(targetValue = 1f, animationSpec = tween(300))

    Box(modifier = Modifier.fillMaxSize().clickable { onDismiss() }) {
        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp, top = 200.dp).alpha(animatedAlpha),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
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
fun FloatingIcon(icon: Int, label: String, onClick: () -> Unit, scale: Float) {
    Card(
        modifier = Modifier.clickable { onClick() }.scale(scale).size(56.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}
