package com.example.forkit

import android.content.Context
import android.content.Intent
import android.widget.Toast
 import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.ui.theme.ForkItTheme
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.CreateHabitRequest
import com.example.forkit.data.models.CreateHabitApiRequest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Extension function to find the activity
@Composable
fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

class AddHabitActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val userId = intent.getStringExtra("USER_ID") ?: ""
        
        setContent {
            ForkItTheme {
                AddHabitScreen(
                    userId = userId,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@Composable
fun AddHabitScreen(
    userId: String,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    var habitName by remember { mutableStateOf("") }
    var selectedRepeat by remember { mutableStateOf(0) } // 0: Daily, 1: Weekly, 2: Monthly
    var selectedDays by remember { mutableStateOf(setOf<Int>()) } // 0-6 for Sunday-Saturday
    var selectedDayOfMonth by remember { mutableStateOf<LocalDate?>(null) }
    var selectedCategory by remember { mutableStateOf(0) } // 0: General, 1: Nutrition, 2: Exercise, 3: Health
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Add Habit",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Habit Name Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Habit Name",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    TextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        placeholder = {
                            Text(
                                text = "Enter habit name...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.secondary
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
            
            // Category Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Category",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf("General", "Nutrition", "Exercise", "Health")
                    categories.forEachIndexed { index, category ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(
                                    color = if (selectedCategory == index) 
                                        MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selectedCategory == index) 
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCategory = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                color = if (selectedCategory == index) Color.White else MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Repeat Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Repeat",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val repeatOptions = listOf("Daily", "Weekly", "Monthly")
                    repeatOptions.forEachIndexed { index, option ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .background(
                                    color = if (selectedRepeat == index) 
                                        MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (selectedRepeat == index) 
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedRepeat = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                color = if (selectedRepeat == index) Color.White else MaterialTheme.colorScheme.secondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Weekly Day Selection (only show for Weekly)
            if (selectedRepeat == 1) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select days of the week:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                        days.forEachIndexed { index, day ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = if (selectedDays.contains(index)) 
                                            MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (selectedDays.contains(index)) 
                                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { 
                                        selectedDays = if (selectedDays.contains(index)) {
                                            selectedDays - index
                                        } else {
                                            selectedDays + index
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    color = if (selectedDays.contains(index)) Color.White else MaterialTheme.colorScheme.secondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            // Monthly Day Selection (only show for Monthly)
            if (selectedRepeat == 2) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select day of the month:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { showDatePicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedDayOfMonth?.format(DateTimeFormatter.ofPattern("dd MMMM")) ?: "Select day of month",
                            color = if (selectedDayOfMonth == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            // Error message
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Create Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { 
                        if (!isLoading) {
                            scope.launch {
                                createHabit(
                                    context = context,
                                    userId = userId,
                                    habitName = habitName,
                                    selectedRepeat = selectedRepeat,
                                    selectedDays = selectedDays,
                                    selectedDayOfMonth = selectedDayOfMonth,
                                    selectedCategory = selectedCategory,
                                    isLoading = { isLoading = it },
                                    errorMessage = { errorMessage = it },
                                    onSuccess = onBackPressed
                                )
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Create Habit",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Bottom spacing
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Date picker for monthly habits
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                selectedDayOfMonth = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun DatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Day of Month") },
        text = {
            Column {
                Text("Select the day of the month for this habit to repeat:")
                Spacer(modifier = Modifier.height(16.dp))
                
                // Simple day picker (1-31)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(31) { day ->
                        val dayNumber = day + 1
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = if (selectedDate.dayOfMonth == dayNumber) 
                                        MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selectedDate.dayOfMonth == dayNumber) 
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { 
                                    selectedDate = selectedDate.withDayOfMonth(dayNumber)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNumber.toString(),
                                color = if (selectedDate.dayOfMonth == dayNumber) Color.White else MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(selectedDate)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private suspend fun createHabit(
    context: Context,
    userId: String,
    habitName: String,
    selectedRepeat: Int,
    selectedDays: Set<Int>,
    selectedDayOfMonth: LocalDate?,
    selectedCategory: Int,
    isLoading: (Boolean) -> Unit,
    errorMessage: (String?) -> Unit,
    onSuccess: () -> Unit
) {
    try {
        isLoading(true)
        errorMessage(null)
        
        if (habitName.isBlank()) {
            errorMessage("Please enter a habit name")
            return
        }
        
        if (selectedRepeat == 1 && selectedDays.isEmpty()) {
            errorMessage("Please select at least one day for weekly habits")
            return
        }
        
        if (selectedRepeat == 2 && selectedDayOfMonth == null) {
            errorMessage("Please select a day of the month for monthly habits")
            return
        }
        
        val categories = listOf("GENERAL", "NUTRITION", "EXERCISE", "HEALTH")
        val frequencies = listOf("DAILY", "WEEKLY", "MONTHLY")
        
        val habitRequest = CreateHabitRequest(
            title = habitName.trim(),
            description = when (selectedRepeat) {
                0 -> "Daily habit that repeats every day"
                1 -> "Weekly habit that repeats on selected days"
                2 -> "Monthly habit that repeats on day ${selectedDayOfMonth?.dayOfMonth}"
                else -> ""
            },
            category = categories[selectedCategory],
            frequency = frequencies[selectedRepeat],
            selectedDays = if (selectedRepeat == 1) selectedDays.toList() else null,
            dayOfMonth = if (selectedRepeat == 2) selectedDayOfMonth?.dayOfMonth else null
        )
        
        android.util.Log.d("AddHabitActivity", "Creating habit: $habitRequest")
        android.util.Log.d("AddHabitActivity", "User ID: $userId")
        
        if (userId.isEmpty()) {
            errorMessage("User ID is missing. Please log in again.")
            return
        }
        
        val apiRequest = CreateHabitApiRequest(
            userId = userId,
            habit = habitRequest
        )
        
        android.util.Log.d("AddHabitActivity", "API Request: $apiRequest")
        
        val response = RetrofitClient.api.createHabit(apiRequest)
        
        android.util.Log.d("AddHabitActivity", "Response code: ${response.code()}")
        android.util.Log.d("AddHabitActivity", "Response body: ${response.body()}")
        
        if (response.isSuccessful && response.body()?.success == true) {
            android.util.Log.d("AddHabitActivity", "Habit created successfully")
            Toast.makeText(context, "Habit created successfully! 🎉", Toast.LENGTH_SHORT).show()
            onSuccess()
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("AddHabitActivity", "Error response body: $errorBody")
            
            val errorMsg = response.body()?.message ?: "Failed to create habit (Code: ${response.code()})"
            android.util.Log.e("AddHabitActivity", "Failed to create habit: $errorMsg")
            Toast.makeText(context, "Failed to create habit: $errorMsg", Toast.LENGTH_LONG).show()
            errorMessage(errorMsg)
        }
        
    } catch (e: Exception) {
        val errorMsg = "Error creating habit: ${e.message}"
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        errorMessage(errorMsg)
    } finally {
        isLoading(false)
    }
}