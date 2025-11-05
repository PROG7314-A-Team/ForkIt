package com.example.forkit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.UpdateUserGoalsRequest
import com.example.forkit.ui.theme.ForkItTheme
import kotlinx.coroutines.launch

class GoalsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Apply saved language
        LanguageManager.applyLanguage(this)
        
        val userId = intent.getStringExtra("USER_ID") ?: ""
        
        setContent {
            ForkItTheme {
                GoalsScreen(
                    userId = userId,
                    onBackPressed = { finish() },
                    onSuccess = {
                        Toast.makeText(this, "Goals updated successfully! 🎯", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check if language changed while in another activity
        val savedLanguageCode = LanguageManager.getCurrentLanguageCode(this)
        val currentLocale = resources.configuration.locales[0].language
        
        // If language changed, recreate the activity to apply new language
        if (savedLanguageCode != currentLocale) {
            recreate()
        }
    }
}

@Composable
fun GoalsScreen(
    userId: String,
    onBackPressed: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State for goal values
    var dailyCalories by remember { mutableStateOf("") }
    var dailyWater by remember { mutableStateOf("") }
    var dailySteps by remember { mutableStateOf("") }
    var weeklyExercises by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    
    // Fetch current goals when screen loads
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            try {
                isLoading = true
                android.util.Log.d("GoalsActivity", "Fetching goals for user: $userId")
                
                val response = RetrofitClient.api.getUserGoals(userId)
                
                if (response.isSuccessful) {
                    val goals = response.body()?.data
                    dailyCalories = goals?.dailyCalories?.toString() ?: "2000"
                    dailyWater = goals?.dailyWater?.toString() ?: "2000"
                    dailySteps = goals?.dailySteps?.toString() ?: "8000"
                    weeklyExercises = goals?.weeklyExercises?.toString() ?: "3"
                    android.util.Log.d("GoalsActivity", "Goals loaded successfully")
                } else {
                    errorMessage = "Failed to load goals"
                    android.util.Log.e("GoalsActivity", "Failed to load goals: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage = "Error loading goals: ${e.localizedMessage}"
                android.util.Log.e("GoalsActivity", "Error loading goals: ${e.message}", e)
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
            errorMessage = "No user ID provided"
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
            // Header
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
                Text(
                    text = "Daily Goals",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            // Main content
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Description
                    Text(
                        text = "Set your daily health and fitness goals",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Calorie Goal
                    GoalInputCard(
                        icon = "🍽️",
                        title = "Daily Calorie Goal",
                        value = dailyCalories,
                        onValueChange = { 
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                dailyCalories = it
                                errorMessage = ""
                                successMessage = ""
                            }
                        },
                        unit = "kcal",
                        hint = "Recommended: 1500-3000",
                        range = "Range: 1200-10000"
                    )
                    
                    // Water Goal
                    GoalInputCard(
                        icon = "💧",
                        title = "Daily Water Goal",
                        value = dailyWater,
                        onValueChange = { 
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                dailyWater = it
                                errorMessage = ""
                                successMessage = ""
                            }
                        },
                        unit = "ml",
                        hint = "Recommended: 2000-3000",
                        range = "Range: 500-10000"
                    )
                    
                    // Steps Goal
                    GoalInputCard(
                        icon = "👟",
                        title = "Daily Steps Goal",
                        value = dailySteps,
                        onValueChange = { 
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                dailySteps = it
                                errorMessage = ""
                                successMessage = ""
                            }
                        },
                        unit = "steps",
                        hint = "Recommended: 5000-15000",
                        range = "Range: 0-50000"
                    )
                    
                    // Weekly Exercises Goal
                    GoalInputCard(
                        icon = "🏋️",
                        title = "Weekly Exercise Sessions",
                        value = weeklyExercises,
                        onValueChange = { 
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                weeklyExercises = it
                                errorMessage = ""
                                successMessage = ""
                            }
                        },
                        unit = "sessions",
                        hint = "Recommended: 3-5",
                        range = "Range: 0-21"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Error message
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Success message
                    if (successMessage.isNotEmpty()) {
                        Text(
                            text = successMessage,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Save Button
                    Button(
                        onClick = {
                            // Validate inputs
                            val caloriesValue = dailyCalories.toIntOrNull()
                            val waterValue = dailyWater.toIntOrNull()
                            val stepsValue = dailySteps.toIntOrNull()
                            val exercisesValue = weeklyExercises.toIntOrNull()
                            
                            if (caloriesValue == null || caloriesValue < 1200 || caloriesValue > 10000) {
                                errorMessage = "Daily calories must be between 1200 and 10000"
                                return@Button
                            }
                            
                            if (waterValue == null || waterValue < 500 || waterValue > 10000) {
                                errorMessage = "Daily water must be between 500 and 10000 ml"
                                return@Button
                            }
                            
                            if (stepsValue == null || stepsValue < 0 || stepsValue > 50000) {
                                errorMessage = "Daily steps must be between 0 and 50000"
                                return@Button
                            }
                            
                            if (exercisesValue == null || exercisesValue < 0 || exercisesValue > 21) {
                                errorMessage = "Weekly exercises must be between 0 and 21"
                                return@Button
                            }
                            
                            if (userId.isEmpty()) {
                                errorMessage = "User ID not found. Please log in again."
                                return@Button
                            }
                            
                            // Clear messages and start saving
                            errorMessage = ""
                            successMessage = ""
                            isSaving = true
                            
                            // Make API call
                            scope.launch {
                                try {
                                    android.util.Log.d("GoalsActivity", "Updating goals for user: $userId")
                                    
                                    val response = RetrofitClient.api.updateUserGoals(
                                        userId = userId,
                                        request = UpdateUserGoalsRequest(
                                            dailyCalories = caloriesValue,
                                            dailyWater = waterValue,
                                            dailySteps = stepsValue,
                                            weeklyExercises = exercisesValue
                                        )
                                    )
                                    
                                    if (response.isSuccessful) {
                                        android.util.Log.d("GoalsActivity", "Goals updated successfully")
                                        onSuccess()
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        android.util.Log.e("GoalsActivity", "Failed to update goals: $errorBody")
                                        errorMessage = "Failed to save goals: ${response.code()}"
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("GoalsActivity", "Error updating goals: ${e.message}", e)
                                    errorMessage = "Error: ${e.localizedMessage}"
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "Save Goals",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalInputCard(
    icon: String,
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    hint: String,
    range: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = icon,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = hint,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(unit) },
                placeholder = { Text("Enter $unit") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = range,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

