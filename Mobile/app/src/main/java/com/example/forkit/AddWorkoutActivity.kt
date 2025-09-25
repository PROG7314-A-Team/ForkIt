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
import java.text.SimpleDateFormat
import java.util.*

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
    var caloriesBurned by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("") }
    
    // Initialize with current date
    val currentDate = remember { Date() }
    var selectedDate by remember { mutableStateOf(currentDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Format date for display
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val selectedDateString = remember(selectedDate) { dateFormatter.format(selectedDate) }
    
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
                Text(
                    text = "Add Exercise",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            // Main content - centered
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Input fields section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                
                    // Name input field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Text(
                            text = if (name.isEmpty()) "Name" else name,
                            fontSize = 18.sp,
                            color = if (name.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                                .wrapContentHeight(Alignment.CenterVertically)
                        )
                    }
                
                    // Date input field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { showDatePicker = true }
                    ) {
                        Text(
                            text = selectedDateString,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                                .wrapContentHeight(Alignment.CenterVertically)
                        )
                    }
                
                    // Calories burned input field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (caloriesBurned.isEmpty()) "Calories Burned" else caloriesBurned,
                                fontSize = 18.sp,
                                color = if (caloriesBurned.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "kcal",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Type selection
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Type",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            TypeButton(
                                text = "Cardio",
                                isSelected = selectedType == "Cardio",
                                onClick = { selectedType = "Cardio" }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TypeButton(
                                text = "Strength",
                                isSelected = selectedType == "Strength",
                                onClick = { selectedType = "Strength" }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Add Exercise button
                Button(
                    onClick = { /* Handle add exercise */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp)),
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
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add Exercise",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.background
                        )
                    }
                }
            }
        }
        
        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.time
            )
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = Date(millis)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text(
                            "OK",
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePicker = false }
                    ) {
                        Text(
                            "Cancel",
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = MaterialTheme.colorScheme.secondary,
                        todayDateBorderColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = MaterialTheme.colorScheme.background
                    )
                )
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
            .fillMaxWidth()
            .height(64.dp)
            .border(
                width = 3.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium
        )
    }
}
