package com.example.forkit

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.ui.theme.ForkItTheme

class AddHabitActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            ForkItTheme {
                AddHabitScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@Composable
fun AddHabitScreen(
    onBackPressed: () -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var selectedRepeat by remember { mutableStateOf(1) } // 0: Daily, 1: Weekly, 2: Monthly
    var selectedDays by remember { mutableStateOf(setOf<Int>()) } // 0-6 for Sunday-Saturday
    var reminderTime by remember { mutableStateOf("") }
    
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
                            text = "Habit Name",
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
            
            // Repeat Section
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Repeat",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val repeatOptions = listOf("Daily", "Weekly", "Monthly")
                    repeatOptions.forEachIndexed { index, option ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .background(
                                    color = if (selectedRepeat == index) 
                                        MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (selectedRepeat == index) 
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedRepeat = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                color = if (selectedRepeat == index) Color.White else MaterialTheme.colorScheme.secondary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // On These Days Section (only show for Weekly)
            if (selectedRepeat == 1) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "On these days:",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val days = listOf("S", "M", "T", "W", "T", "F", "S")
                        days.forEachIndexed { index, day ->
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        color = if (selectedDays.contains(index)) 
                                            MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = if (selectedDays.contains(index)) 
                                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(10.dp)
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
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            // Set Custom Reminder Section
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Set Custom Reminder",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary, // ForkIt Green
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { 
                            // TODO: Open date/time picker
                        }
                ) {
                    Text(
                        text = if (reminderTime.isEmpty()) "Choose Day & Time" else reminderTime,
                        color = if (reminderTime.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 28.dp)
                            .wrapContentHeight(Alignment.CenterVertically)
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
                        // TODO: Create habit functionality
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Bottom spacing
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
