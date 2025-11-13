package com.example.forkit.ui.screens

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.AddHabitActivity
import com.example.forkit.data.models.Habit
import com.example.forkit.data.models.MockHabits
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.UpdateHabitRequest
import com.example.forkit.data.local.AppDatabase
import com.example.forkit.data.local.entities.HabitEntity
import com.example.forkit.data.repository.HabitRepository
import com.example.forkit.utils.NetworkConnectivityManager
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.example.forkit.R

@Composable
fun HabitsScreen(userId: String) {
    val context = LocalContext.current
    var selectedTimeFilter by remember { mutableStateOf(0) } // 0: Today, 1: Weekly, 2: Monthly
    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    val scope = rememberCoroutineScope()
    
    // Initialize database and repository
    val database = remember { AppDatabase.getInstance(context) }
    val networkManager = remember { NetworkConnectivityManager(context) }
    val repository = remember { 
        HabitRepository(
            apiService = RetrofitClient.api,
            habitDao = database.habitDao(),
            networkManager = networkManager
        )
    }
    
    // Function to filter habits based on current day
    fun filterHabitsForToday(habits: List<HabitEntity>, filterType: Int): List<HabitEntity> {
        val today = java.util.Calendar.getInstance()
        val dayOfWeek = today.get(java.util.Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday, 1 = Monday, etc.
        val dayOfMonth = today.get(java.util.Calendar.DAY_OF_MONTH)
        
        return habits.filter { habit ->
            when (filterType) {
                0 -> true // Daily habits always show
                1 -> { // Weekly habits - check if today is in selected days
                    val selectedDays = habit.selectedDays?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
                    selectedDays.contains(dayOfWeek)
                }
                2 -> { // Monthly habits - check if today matches the day of month
                    habit.dayOfMonth == dayOfMonth
                }
                else -> true
            }
        }
    }
    
    // Function to fetch habits from local database with proper filtering
    val fetchHabits: () -> Unit = {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                android.util.Log.d("HabitsScreen", "Fetching habits for userId: $userId, filter: $selectedTimeFilter")
                
                val allHabits = repository.getHabitsByFrequency(userId, when (selectedTimeFilter) {
                    0 -> "DAILY"
                    1 -> "WEEKLY" 
                    2 -> "MONTHLY"
                    else -> "DAILY"
                })
                
                // Filter habits based on current day
                val filteredHabits = filterHabitsForToday(allHabits, selectedTimeFilter)
                
                // Convert to Habit model
                habits = filteredHabits.map { entity ->
                    Habit(
                        id = entity.localId,
                        title = entity.title,
                        description = entity.description,
                        category = "GENERAL", // Default category
                        frequency = entity.frequency,
                        isCompleted = entity.isCompleted,
                        completedAt = entity.completedAt,
                        createdAt = entity.createdAt.toString()
                    )
                }
                
                android.util.Log.d("HabitsScreen", "Successfully loaded ${habits.size} filtered habits")
                
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                android.util.Log.e("HabitsScreen", "Exception loading habits: ${e.message}", e)
                habits = emptyList()
            } finally {
                isLoading = false
            }
        }
    }
    
    // Function to delete a habit
    val deleteHabit: (String) -> Unit = { habitId ->
        scope.launch {
            try {
                val result = repository.deleteHabit(habitId)
                result.onSuccess {
                    habits = habits.filter { it.id != habitId }
                    android.util.Log.d("HabitsScreen", "Successfully deleted habit: $habitId")
                }.onFailure { e ->
                    android.util.Log.e("HabitsScreen", "Failed to delete habit: ${e.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("HabitsScreen", "Error deleting habit: ${e.message}", e)
            }
        }
    }
    
    // Update habits when time filter changes or component loads
    LaunchedEffect(selectedTimeFilter, userId) {
        if (userId.isNotEmpty()) {
            fetchHabits()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.your_habits),
                fontSize = 32.sp,
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
            
            // Refresh button
            IconButton(
                onClick = { fetchHabits() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (isLoading) 360f else 0f)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
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
                        intent.putExtra("USER_ID", userId)
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.new_habit),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    
        // Time Filter Tabs
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
                val timeFilters = listOf(
                    stringResource(R.string.today),
                    stringResource(R.string.weekly),
                    stringResource(R.string.monthly)
                )
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
                                        bottomStart = 16.dp
                                    )
                                    timeFilters.size - 1 -> RoundedCornerShape(
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
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loading state
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            // Error message
            else if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = BorderStroke(1.dp, Color(0xFFE57373))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Error",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Using offline data: $errorMessage",
                                color = Color(0xFFD32F2F),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
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
                    isLoading = isLoading,
                    onToggleComplete = { habitId ->
                        scope.launch {
                            try {
                                val habitToUpdate = habits.find { it.id == habitId }
                                if (habitToUpdate != null) {
                                    val isCompleted = !habitToUpdate.isCompleted
                                    val completedAt = if (isCompleted) java.time.Instant.now().toString() else null
                                    
                                    val result = repository.updateHabit(
                                        localId = habitId,
                                        isCompleted = isCompleted,
                                        completedAt = completedAt
                                    )
                                    
                                    result.onSuccess {
                                        habits = habits.map { h ->
                                            if (h.id == habitId) {
                                                h.copy(
                                                    isCompleted = isCompleted,
                                                    completedAt = completedAt
                                                )
                                            } else h
                                        }
                                        android.util.Log.d("HabitsScreen", "Successfully updated habit: $habitId")
                                    }.onFailure { e ->
                                        android.util.Log.e("HabitsScreen", "Failed to update habit: ${e.message}")
                                        // Still update locally for better UX
                                        habits = habits.map { h ->
                                            if (h.id == habitId) {
                                                h.copy(
                                                    isCompleted = isCompleted,
                                                    completedAt = completedAt
                                                )
                                            } else h
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("HabitsScreen", "Error updating habit: ${e.message}", e)
                                // Still update locally for better UX
                                val habitToUpdate = habits.find { it.id == habitId }
                                if (habitToUpdate != null) {
                                    val isCompleted = !habitToUpdate.isCompleted
                                    val completedAt = if (isCompleted) java.time.Instant.now().toString() else null
                                    habits = habits.map { h ->
                                        if (h.id == habitId) {
                                            h.copy(
                                                isCompleted = isCompleted,
                                                completedAt = completedAt
                                            )
                                        } else h
                                    }
                                }
                            }
                        }
                    },
                    onDelete = { habitId ->
                        habitToDelete = habit
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
                        isLoading = isLoading,
                        onToggleComplete = { habitId ->
                            scope.launch {
                                try {
                                    val habitToUpdate = habits.find { it.id == habitId }
                                    if (habitToUpdate != null) {
                                        val isCompleted = !habitToUpdate.isCompleted
                                        val completedAt = if (isCompleted) java.time.Instant.now().toString() else null
                                        
                                        val result = repository.updateHabit(
                                            localId = habitId,
                                            isCompleted = isCompleted,
                                            completedAt = completedAt
                                        )
                                        
                                        result.onSuccess {
                                            habits = habits.map { h ->
                                                if (h.id == habitId) {
                                                    h.copy(
                                                        isCompleted = isCompleted,
                                                        completedAt = completedAt
                                                    )
                                                } else h
                                            }
                                            android.util.Log.d("HabitsScreen", "Successfully updated habit: $habitId")
                                        }.onFailure { e ->
                                            android.util.Log.e("HabitsScreen", "Failed to update habit: ${e.message}")
                                            // Still update locally for better UX
                                            habits = habits.map { h ->
                                                if (h.id == habitId) {
                                                    h.copy(
                                                        isCompleted = isCompleted,
                                                        completedAt = completedAt
                                                    )
                                                } else h
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("HabitsScreen", "Error updating habit: ${e.message}", e)
                                    // Still update locally for better UX
                                    val habitToUpdate = habits.find { it.id == habitId }
                                    if (habitToUpdate != null) {
                                        val isCompleted = !habitToUpdate.isCompleted
                                        val completedAt = if (isCompleted) java.time.Instant.now().toString() else null
                                        habits = habits.map { h ->
                                            if (h.id == habitId) {
                                                h.copy(
                                                    isCompleted = isCompleted,
                                                    completedAt = completedAt
                                                )
                                            } else h
                                        }
                                    }
                                }
                            }
                        },
                        onDelete = { habitId ->
                            habitToDelete = habit
                        }
                    )
                }
            }
            
            // Bottom padding for navigation
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        
        // Delete confirmation dialog
        habitToDelete?.let { habit ->
            AlertDialog(
                onDismissRequest = { habitToDelete = null },
                title = { 
                    Text(
                        "Delete Habit?",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = { 
                    Text("Are you sure you want to delete \"${habit.title}\"? This action cannot be undone.") 
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            deleteHabit(habit.id)
                            habitToDelete = null
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { habitToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    isCompleted: Boolean,
    isLoading: Boolean,
    onToggleComplete: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (!isCompleted) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (isCompleted) Color.Transparent else MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .clickable(enabled = !isLoading) { onToggleComplete(habit.id) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Habit details
                Column {
                    Text(
                        text = habit.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isCompleted) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        modifier = if (isCompleted) Modifier.alpha(0.6f) else Modifier
                    )
                    
                    if (!habit.description.isNullOrBlank()) {
                        Text(
                            text = habit.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.alpha(if (isCompleted) 0.5f else 0.8f)
                        )
                    }
                    
                    Text(
                        text = "${habit.frequency} • ${habit.category}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.alpha(if (isCompleted) 0.5f else 1f)
                    )
                }
            }
            
            // Delete button
            IconButton(
                onClick = { onDelete(habit.id) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

