package com.example.forkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.ui.theme.ForkItTheme

class AddWorkoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            ForkItTheme {
                AddWorkoutScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(
    onBackPressed: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var caloriesBurned by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Add Exercise",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeManager.forkItGreen,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name input field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFB0BEC5),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        text = if (name.isEmpty()) "Name" else name,
                        fontSize = 16.sp,
                        color = if (name.isEmpty()) Color(0xFF90A4AE) else Color.Black,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                }
                
                // Date input field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFB0BEC5),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { /* Handle date picker */ }
                ) {
                    Text(
                        text = if (selectedDate.isEmpty()) "Select Date" else selectedDate,
                        fontSize = 16.sp,
                        color = if (selectedDate.isEmpty()) Color(0xFF90A4AE) else Color.Black,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                }
                
                // Calories burned input field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFB0BEC5),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (caloriesBurned.isEmpty()) "Calories Burned" else caloriesBurned,
                            fontSize = 16.sp,
                            color = if (caloriesBurned.isEmpty()) Color(0xFF90A4AE) else Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "kcal",
                            fontSize = 16.sp,
                            color = Color(0xFF1E9ECD)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Type selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Type",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TypeButton(
                            text = "Cardio",
                            isSelected = selectedType == "Cardio",
                            onClick = { selectedType = "Cardio" }
                        )
                        TypeButton(
                            text = "Strength",
                            isSelected = selectedType == "Strength",
                            onClick = { selectedType = "Strength" }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Add Exercise button
                Button(
                    onClick = { /* Handle add exercise */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
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
                                        ThemeManager.forkItGreen,
                                        ThemeManager.forkItBlue
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add Exercise",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun TypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .width(100.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFB0BEC5),
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF1E9ECD),
            fontWeight = FontWeight.Medium
        )
    }
}
