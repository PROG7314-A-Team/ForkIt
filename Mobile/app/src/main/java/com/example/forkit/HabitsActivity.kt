package com.example.forkit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.ui.theme.ForkItTheme
import com.example.forkit.data.models.Habit
import com.example.forkit.data.models.MockHabits
import java.time.LocalDateTime

class HabitsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            ForkItTheme {
                HabitsScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@Composable
fun HabitsScreen(
    onBackPressed: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTimeFilter by remember { mutableStateOf(0) } // 0: Today, 1: Weekly, 2: Monthly
    var habits by remember { mutableStateOf(MockHabits.getTodayHabits()) }
    
    // Update habits when time filter changes
    LaunchedEffect(selectedTimeFilter) {
        habits = when (selectedTimeFilter) {
            0 -> MockHabits.getTodayHabits()
            1 -> MockHabits.getWeeklyHabits()
            2 -> MockHabits.getMonthlyHabits()
            else -> MockHabits.getTodayHabits()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with back button and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // "Your Habits" title with gradient
            Text(
                text = "Your Habits",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // New Habit button
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { 
                        val intent = Intent(context, AddHabitActivity::class.java)
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "New Habit",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Time Filter Tabs - Segmented Control
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(68.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                val timeFilters = listOf("Today", "Weekly", "Monthly")
                timeFilters.forEachIndexed { index, filter ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if (selectedTimeFilter == index) 
                                    MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = when (index) {
                                    0 -> RoundedCornerShape(
                                        topStart = 16.dp,
                                        bottomStart = 16.dp,
                                        topEnd = 0.dp,
                                        bottomEnd = 0.dp
                                    )
                                    timeFilters.size - 1 -> RoundedCornerShape(
                                        topStart = 0.dp,
                                        bottomStart = 0.dp,
                                        topEnd = 16.dp,
                                        bottomEnd = 16.dp
                                    )
                                    else -> RoundedCornerShape(0.dp)
                                }
                            )
                            .clickable { selectedTimeFilter = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            color = if (selectedTimeFilter == index) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Habits Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Todo Section
            item {
                Text(
                    text = "Todo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            val todoHabits = habits.filter { !it.isCompleted }
            items(todoHabits) { habit ->
                HabitItem(
                    habit = habit,
                    isCompleted = false,
                    onToggleComplete = { habitId ->
                        habits = habits.map { h ->
                            if (h.id == habitId) {
                                h.copy(
                                    isCompleted = !h.isCompleted,
                                    completedAt = if (!h.isCompleted) LocalDateTime.now() else null
                                )
                            } else h
                        }
                    }
                )
            }
            
            // Completed Section
            if (habits.any { it.isCompleted }) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Completed",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                val completedHabits = habits.filter { it.isCompleted }
                items(completedHabits) { habit ->
                    HabitItem(
                        habit = habit,
                        isCompleted = true,
                        onToggleComplete = { habitId ->
                            habits = habits.map { h ->
                                if (h.id == habitId) {
                                    h.copy(
                                        isCompleted = !h.isCompleted,
                                        completedAt = if (!h.isCompleted) LocalDateTime.now() else null
                                    )
                                } else h
                            }
                        }
                    )
                }
            }
            
            // Bottom padding for navigation
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    isCompleted: Boolean,
    onToggleComplete: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .border(
                width = 3.dp,
                color = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onToggleComplete(habit.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = habit.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            // Completion indicator
            Card(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
