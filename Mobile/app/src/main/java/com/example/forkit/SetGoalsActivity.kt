package com.example.forkit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.UpdateUserGoalsRequest
import com.example.forkit.ui.theme.ForkItTheme
import com.example.forkit.R
import kotlinx.coroutines.launch

class SetGoalsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val age = intent.getIntExtra("AGE", 25)
        val height = intent.getDoubleExtra("HEIGHT", 175.5)
        val weight = intent.getDoubleExtra("WEIGHT", 75.0)
        
        setContent {
            ForkItTheme {
                SetGoalsScreen(
                    userId = userId,
                    age = age,
                    height = height,
                    weight = weight,
                    onBackPressed = { finish() },
                    onFinish = { 
                        // Navigate to Dashboard
                        val intent = Intent(this, DashboardActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SetGoalsScreen(
    userId: String,
    age: Int,
    height: Double,
    weight: Double,
    onBackPressed: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var dailyCalories by remember { mutableStateOf("") }
    var weeklyExercises by remember { mutableStateOf("") }
    var dailyWater by remember { mutableStateOf("") }
    var dailySteps by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with back button and progress
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
                    tint = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Progress indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // First step - completed
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF22B27D),
                                    Color(0xFF1E9ECD)
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                // Second step - completed
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF22B27D),
                                    Color(0xFF1E9ECD)
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "2/2",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
        }
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title
            Text(
                text = "Set your goals",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22B27D),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Subtitle
            Text(
                text = "Let's set some targets to help you stay on track",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
            
            // Daily Calorie Goal
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "What's your daily calorie intake goal?",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = dailyCalories,
                            onValueChange = { 
                                if (it.length <= 5 && (it.isEmpty() || it.all { char -> char.isDigit() })) {
                                    dailyCalories = it
                                    errorMessage = null
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "2000",
                                    color = Color(0xFF666666),
                                    fontSize = 16.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFF22B27D)
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        Text(
                            text = "kcal",
                            color = Color(0xFF666666),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Weekly Exercise Goal
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "How many workouts per week?",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = weeklyExercises,
                            onValueChange = { 
                                if (it.length <= 2 && (it.isEmpty() || it.all { char -> char.isDigit() })) {
                                    weeklyExercises = it
                                    errorMessage = null
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "3",
                                    color = Color(0xFF666666),
                                    fontSize = 16.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFF22B27D)
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        Text(
                            text = "sessions",
                            color = Color(0xFF666666),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Daily Water Goal
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "How much water should you drink daily?",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = dailyWater,
                            onValueChange = { 
                                if (it.length <= 5 && (it.isEmpty() || it.all { char -> char.isDigit() })) {
                                    dailyWater = it
                                    errorMessage = null
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "2000",
                                    color = Color(0xFF666666),
                                    fontSize = 16.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFF22B27D)
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        Text(
                            text = "ml",
                            color = Color(0xFF666666),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Daily Steps Goal
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.steps_goal),
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = dailySteps,
                            onValueChange = { 
                                if (it.length <= 5 && (it.isEmpty() || it.all { char -> char.isDigit() })) {
                                    dailySteps = it
                                    errorMessage = null
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "8000",
                                    color = Color(0xFF666666),
                                    fontSize = 16.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFF22B27D)
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        Text(
                            text = stringResource(id = R.string.steps),
                            color = Color(0xFF666666),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
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
            
            // Finish Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF22B27D),
                                Color(0xFF1E9ECD)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { 
                        if (!isLoading) {
                            // Validate inputs
                            val caloriesInt = dailyCalories.toIntOrNull()
                            val exercisesInt = weeklyExercises.toIntOrNull()
                            val waterInt = dailyWater.toIntOrNull()
                            val stepsInt = dailySteps.toIntOrNull()
                            
                            when {
                                dailyCalories.isEmpty() || caloriesInt == null || caloriesInt < 1200 || caloriesInt > 10000 -> {
                                    errorMessage = "Please enter a valid calorie goal (1200-10000 kcal)"
                                }
                                weeklyExercises.isEmpty() || exercisesInt == null || exercisesInt < 0 || exercisesInt > 21 -> {
                                    errorMessage = "Please enter a valid exercise goal (0-21 sessions)"
                                }
                                dailyWater.isEmpty() || waterInt == null || waterInt < 500 || waterInt > 10000 -> {
                                    errorMessage = "Please enter a valid water goal (500-10000 ml)"
                                }
                                dailySteps.isEmpty() || stepsInt == null || stepsInt < 0 || stepsInt > 50000 -> {
                                    errorMessage = "Please enter a valid steps goal (0-50000 steps)"
                                }
                                else -> {
                                    // Update goals via API
                                    scope.launch {
                                        updateUserGoals(
                                            context = context,
                                            userId = userId,
                                            dailyCalories = caloriesInt,
                                            weeklyExercises = exercisesInt,
                                            dailyWater = waterInt,
                                            dailySteps = stepsInt,
                                            isLoading = { isLoading = it },
                                            errorMessage = { errorMessage = it },
                                            onSuccess = onFinish
                                        )
                                    }
                                }
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
                        text = stringResource(id = R.string.finish),
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
}

private suspend fun updateUserGoals(
    context: Context,
    userId: String,
    dailyCalories: Int,
    weeklyExercises: Int,
    dailyWater: Int,
    dailySteps: Int,
    isLoading: (Boolean) -> Unit,
    errorMessage: (String?) -> Unit,
    onSuccess: () -> Unit
) {
    try {
        isLoading(true)
        errorMessage(null)
        
        android.util.Log.d("SetGoalsActivity", "Updating goals for user: $userId")
        android.util.Log.d("SetGoalsActivity", "Goals: calories=$dailyCalories, exercises=$weeklyExercises, water=$dailyWater, steps=$dailySteps")
        
        if (userId.isEmpty()) {
            errorMessage("User ID is missing. Please log in again.")
            return
        }
        
        val goalsRequest = UpdateUserGoalsRequest(
            dailyCalories = dailyCalories,
            weeklyExercises = weeklyExercises,
            dailyWater = dailyWater,
            dailySteps = dailySteps
        )
        
        android.util.Log.d("SetGoalsActivity", "API Request: $goalsRequest")
        
        val response = RetrofitClient.api.updateUserGoals(userId, goalsRequest)
        
        android.util.Log.d("SetGoalsActivity", "Response code: ${response.code()}")
        android.util.Log.d("SetGoalsActivity", "Response body: ${response.body()}")
        
        if (response.isSuccessful && response.body()?.success == true) {
            android.util.Log.d("SetGoalsActivity", "Goals updated successfully")
            Toast.makeText(context, "Goals set successfully! 🎉", Toast.LENGTH_SHORT).show()
            onSuccess()
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("SetGoalsActivity", "Error response body: $errorBody")
            
            val errorMsg = response.body()?.message ?: "Failed to update goals (Code: ${response.code()})"
            android.util.Log.e("SetGoalsActivity", "Failed to update goals: $errorMsg")
            Toast.makeText(context, "Failed to set goals: $errorMsg", Toast.LENGTH_LONG).show()
            errorMessage(errorMsg)
        }
        
    } catch (e: Exception) {
        val errorMsg = "Error setting goals: ${e.message}"
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        errorMessage(errorMsg)
    } finally {
        isLoading(false)
    }
}
