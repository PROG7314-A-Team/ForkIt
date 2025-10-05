package com.example.forkit.ui.dashboard

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.forkit.AddMealActivity
import com.example.forkit.AddWaterActivity
import com.example.forkit.AddWorkoutActivity
import com.example.forkit.R
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue



/**
 * 🎨 DashboardComponents.kt
 *
 * This file holds reusable UI components:
 * - BottomNavigationBar
 * - FloatingIcons overlay
 * - FloatingIcon buttons
 */

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    showFloatingIcons: Boolean,
    onAddButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF22B27D))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val tabs = listOf("Home", "Meals", "Add", "Habits", "Coach")

            tabs.forEachIndexed { index, name ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        Log.d("BottomNavigation", "🔘 Tab clicked: $name ($index)")
                        if (name == "Add") onAddButtonClick() else onTabSelected(index)
                    }
                ) {
                    Text(
                        text = name,
                        fontSize = 12.sp,
                        color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingIcons(context: Context, userId: String, onDismiss: () -> Unit) {
    val animatedAlpha by animateFloatAsState(targetValue = 1f, animationSpec = tween(300))
    val animatedScale by animateFloatAsState(targetValue = 1f, animationSpec = tween(300))

    Box(
        modifier = Modifier.fillMaxSize().clickable { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp, top = 200.dp)
                .alpha(animatedAlpha),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FloatingIcon(
                icon = R.drawable.ic_meals,
                label = "Meal",
                onClick = {
                    Log.d("FloatingIcon", "🍴 Meal clicked → launching AddMealActivity")
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
                    Log.d("FloatingIcon", "💧 Water clicked → launching AddWaterActivity")
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
                    Log.d("FloatingIcon", "🏋️ Workout clicked → launching AddWorkoutActivity")
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF22B27D)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}
