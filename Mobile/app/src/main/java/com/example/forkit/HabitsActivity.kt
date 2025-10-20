package com.example.forkit

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
import com.example.forkit.data.RetrofitClient
import androidx.compose.ui.res.stringResource
import com.example.forkit.data.models.HabitsResponse
import com.example.forkit.data.models.UpdateHabitRequest
import com.example.forkit.services.HabitNotificationScheduler
import com.example.forkit.services.HabitNotificationHelper
import kotlinx.coroutines.launch

class HabitsActivity : ComponentActivity() {
    private var refreshTrigger by mutableStateOf(0)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val userId = intent.getStringExtra("USER_ID") ?: ""
        
        setContent {
            ForkItTheme {
                HabitsScreen(
                    userId = userId,
                    refreshTrigger = refreshTrigger,
                    onBackPressed = { finish() }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Trigger refresh when returning to this activity
        refreshTrigger++
        
        // Schedule notifications if enabled
        val userId = intent.getStringExtra("USER_ID") ?: ""
        if (userId.isNotEmpty()) {
            val notificationHelper = HabitNotificationHelper(this)
            if (notificationHelper.areNotificationsEnabled()) {
                val scheduler = HabitNotificationScheduler(this)
                scheduler.scheduleAllNotifications(userId)
            }
        }
    }
}

@Composable
fun HabitsScreen(
    userId: String,
    refreshTrigger: Int,
    onBackPressed: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTimeFilter by remember { mutableStateOf(0) } // 0: Today, 1: Weekly, 2: Monthly
    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var showFloatingIcons by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    val scope = rememberCoroutineScope()
    
    // Function to fetch habits from API
    val fetchHabits: () -> Unit = {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                android.util.Log.d("HabitsActivity", "Fetching habits for userId: $userId, filter: $selectedTimeFilter")
                
                val response = when (selectedTimeFilter) {
                    0 -> RetrofitClient.api.getDailyHabits(userId)
                    1 -> RetrofitClient.api.getWeeklyHabits(userId)
                    2 -> RetrofitClient.api.getMonthlyHabits(userId)
                    else -> RetrofitClient.api.getDailyHabits(userId)
                }

                Log.d(TAG, "Habits reponse ${response}")
                
                android.util.Log.d("HabitsActivity", "Response code: ${response.code()}")
                android.util.Log.d("HabitsActivity", "Response body: ${response.body()}")
                
                if (response.isSuccessful) {
                    val habitsResponse = response.body()
                    android.util.Log.d("HabitsActivity", "Habits response: $habitsResponse")
                    if (habitsResponse?.success == true) {
                        habits = habitsResponse.data
                        android.util.Log.d("HabitsActivity", "Successfully loaded ${habits.size} habits")
                    } else {
                        errorMessage = habitsResponse?.message ?: "Failed to load habits"
                        android.util.Log.e("HabitsActivity", "API error: ${habitsResponse?.message}")
                        // Fallback to mock data for now
                        habits = when (selectedTimeFilter) {
                            0 -> MockHabits.getTodayHabits()
                            1 -> MockHabits.getWeeklyHabits()
                            2 -> MockHabits.getMonthlyHabits()
                            else -> MockHabits.getTodayHabits()
                        }
                    }
                } else {
                    errorMessage = "Network error: ${response.code()}"
                    android.util.Log.e("HabitsActivity", "Network error: ${response.code()}")
                    // Fallback to mock data for now
                    habits = when (selectedTimeFilter) {
                        0 -> MockHabits.getTodayHabits()
                        1 -> MockHabits.getWeeklyHabits()
                        2 -> MockHabits.getMonthlyHabits()
                        else -> MockHabits.getTodayHabits()
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                android.util.Log.e("HabitsActivity", "Exception loading habits: ${e.message}", e)
                // Fallback to mock data for now
                habits = when (selectedTimeFilter) {
                    0 -> MockHabits.getTodayHabits()
                    1 -> MockHabits.getWeeklyHabits()
                    2 -> MockHabits.getMonthlyHabits()
                    else -> MockHabits.getTodayHabits()
                }
            } finally {
                isLoading = false
            }
        }
    }
    
    // Function to delete a habit
    val deleteHabit: (String) -> Unit = { habitId ->
        scope.launch {
            try {
                val response = RetrofitClient.api.deleteHabit(habitId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Remove from local state
                    habits = habits.filter { it.id != habitId }
                    android.util.Log.d("HabitsActivity", "Successfully deleted habit: $habitId")
                } else {
                    android.util.Log.e("HabitsActivity", "Failed to delete habit: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("HabitsActivity", "Error deleting habit: ${e.message}", e)
            }
        }
    }
    
    // Update habits when time filter changes, component loads, or refresh is triggered
    LaunchedEffect(selectedTimeFilter, userId, refreshTrigger) {
        if (userId.isNotEmpty()) {
            fetchHabits()
        }
    }
    
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
            // Header with title (no back button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Your Habits" title with gradient
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
                        contentDescription = "Refresh",
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
                                    val updateRequest = UpdateHabitRequest(
                                        isCompleted = !habitToUpdate.isCompleted
                                    )
                                    val response = RetrofitClient.api.updateHabit(habitId, updateRequest)
                                    
                                    if (response.isSuccessful && response.body()?.success == true) {
                                        // Update local state
                                        habits = habits.map { h ->
                                            if (h.id == habitId) {
                                                h.copy(
                                                    isCompleted = !h.isCompleted,
                                                    completedAt = if (!h.isCompleted) java.time.Instant.now().toString() else null
                                                )
                                            } else h
                                        }
                                        android.util.Log.d("HabitsActivity", "Successfully updated habit: $habitId")
                                    } else {
                                        android.util.Log.e("HabitsActivity", "Failed to update habit: ${response.body()?.message}")
                                        // Still update locally for better UX
                                        habits = habits.map { h ->
                                            if (h.id == habitId) {
                                                h.copy(
                                                    isCompleted = !h.isCompleted,
                                                    completedAt = if (!h.isCompleted) java.time.Instant.now().toString() else null
                                                )
                                            } else h
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("HabitsActivity", "Error updating habit: ${e.message}", e)
                                // Still update locally for better UX
                                habits = habits.map { h ->
                                    if (h.id == habitId) {
                                        h.copy(
                                            isCompleted = !h.isCompleted,
                                            completedAt = if (!h.isCompleted) java.time.Instant.now().toString() else null
                                        )
                                    } else h
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
                                        val updateRequest = UpdateHabitRequest(
                                            isCompleted = !habitToUpdate.isCompleted
                                        )
                                        val response = RetrofitClient.api.updateHabit(habitId, updateRequest)
                                        
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            // Update local state
                                            habits = habits.map { h ->
                                                if (h.id == habitId) {
                                                    h.copy(
                                                        isCompleted = !h.isCompleted,
                                                        completedAt = if (!h.isCompleted) java.time.Instant.now().toString() else null
                                                    )
                                                } else h
                                            }
                                            android.util.Log.d("HabitsActivity", "Successfully updated habit: $habitId")
                                        } else {
                                            android.util.Log.e("HabitsActivity", "Failed to update habit: ${response.body()?.message}")
                                            // Still update locally for better UX
                                            habits = habits.map { h ->
                                                if (h.id == habitId) {
                                                    h.copy(
                                                        isCompleted = !h.isCompleted,
                                                        completedAt = if (!h.isCompleted) java.time.Instant.now().toString() else null
                                                    )
                                                } else h
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("HabitsActivity", "Error updating habit: ${e.message}", e)
                                    // Still update locally for better UX
                                    habits = habits.map { h ->
                                        if (h.id == habitId) {
                                            h.copy(
                                                isCompleted = !h.isCompleted,
                                                completedAt = if (!h.isCompleted) java.time.Instant.now().toString() else null
                                            )
                                        } else h
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
        
        // Bottom Navigation Bar
        BottomNavigationBar(
            selectedTab = 3, // Habits tab is index 3
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
                        // Already on Habits - do nothing
                    }
                    4 -> {
                        // Coach - Navigate to CoachActivity
                        val intent = Intent(context, CoachActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        context.startActivity(intent)
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

// Note: BottomNavigationBar, FloatingIcons, and FloatingIcon 
// are defined in DashboardActivity.kt and reused here

@Composable
fun HabitItem(
    habit: Habit,
    isCompleted: Boolean,
    isLoading: Boolean = false,
    onToggleComplete: (String) -> Unit,
    onDelete: (String) -> Unit
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
            .clickable { 
                if (!isLoading) {
                    onToggleComplete(habit.id) 
                }
            }
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
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button
                IconButton(
                    onClick = { onDelete(habit.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
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
}

