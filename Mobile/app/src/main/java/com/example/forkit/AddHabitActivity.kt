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
import com.example.forkit.data.models.CreateHabitRequest
import com.example.forkit.data.models.HabitCategory
import com.example.forkit.data.models.HabitFrequency
import com.example.forkit.data.RetrofitClient
import com.example.forkit.ui.theme.ForkItTheme
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    
    var habitName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(0) } // 0: Nutrition, 1: Exercise, 2: Health, 3: General
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val categories = listOf(
        HabitCategory.NUTRITION to "Nutrition",
        HabitCategory.EXERCISE to "Exercise", 
        HabitCategory.HEALTH to "Health",
        HabitCategory.GENERAL to "General"
    )
    
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
                color = MaterialTheme.colorScheme.primary // ForkIt Green
            )
        }
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Habit Name Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), // ForkIt Blue with transparency
                        shape = RoundedCornerShape(18.dp)
                    )
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                TextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    placeholder = {
                        Text(
                            text = "Habit Name (e.g., Drink 8 glasses of water)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 20.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.secondary
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
            
            // Category Section
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Category",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEachIndexed { index, (category, name) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .background(
                                    color = if (selectedCategory == index) 
                                        MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (selectedCategory == index) 
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedCategory = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                color = if (selectedCategory == index) Color.White else MaterialTheme.colorScheme.secondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Daily Habit Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📅 Daily Repeating Habit",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "This habit will appear in your Todo list every morning and reset daily. Complete it by tapping the habit item.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Error message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = BorderStroke(1.dp, Color(0xFFE57373))
                ) {
                    Text(
                        text = error,
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Create Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary, // ForkIt Green
                                MaterialTheme.colorScheme.secondary  // ForkIt Blue
                            )
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { 
                        if (habitName.isNotBlank() && !isLoading) {
                            scope.launch {
                                try {
                                    isLoading = true
                                    errorMessage = null
                                    
                                    val habitRequest = CreateHabitRequest(
                                        title = habitName.trim(),
                                        description = "Daily repeating habit",
                                        category = categories[selectedCategory].first,
                                        frequency = HabitFrequency.DAILY
                                    )
                                    
                                    val createRequest = mapOf(
                                        "userId" to userId,
                                        "habit" to habitRequest
                                    )
                                    
                                    val response = RetrofitClient.api.createHabit(createRequest)
                                    
                                    if (response.isSuccessful && response.body()?.success == true) {
                                        // Success - navigate back to habits page
                                        val intent = Intent(context, HabitsActivity::class.java)
                                        intent.putExtra("USER_ID", userId)
                                        context.startActivity(intent)
                                        onBackPressed()
                                    } else {
                                        errorMessage = response.body()?.message ?: "Failed to create habit"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error creating habit: ${e.message}"
                                    android.util.Log.e("AddHabitActivity", "Error creating habit: ${e.message}", e)
                                } finally {
                                    isLoading = false
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
                        text = if (habitName.isBlank()) "Enter Habit Name" else "Create Daily Habit",
                        color = if (habitName.isBlank()) Color.White.copy(alpha = 0.7f) else Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Bottom spacing
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
