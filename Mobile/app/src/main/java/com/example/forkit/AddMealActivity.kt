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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.ui.theme.ForkItTheme

class AddMealActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            ForkItTheme {
                AddMealScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    onBackPressed: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentScreen by remember { mutableStateOf("main") }
    
    when (currentScreen) {
        "main" -> AddFoodMainScreen(
            onBackPressed = onBackPressed,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onNavigateToAdjustServing = { currentScreen = "adjust" }
        )
        "adjust" -> AdjustServingScreen(
            onBackPressed = { currentScreen = "main" },
            onContinue = { currentScreen = "details" }
        )
        "details" -> AddDetailsScreen(
            onBackPressed = { currentScreen = "adjust" },
            onAddFood = { currentScreen = "main" }
        )
    }
}

@Composable
fun AddFoodMainScreen(
    onBackPressed: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToAdjustServing: () -> Unit
) {
    // Sample history data
    val historyItems = remember {
        listOf(
            FoodHistoryItem("3 Slices Bacon", "Aug 10, 2025", "Breakfast", "250 kcal"),
            FoodHistoryItem("2 Eggs", "Aug 10, 2025", "Lunch", "143 kcal"),
            FoodHistoryItem("Beef Burger", "Aug 10, 2025", "Lunch", "800 kcal")
        )
    }
    
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
                    text = "Add Food",
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
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .border(
                            width = 3.dp,
                            color = ThemeManager.forkItBlue.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(
                            color = ThemeManager.forkItBlue.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = ThemeManager.forkItBlue,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) "Search food" else searchQuery,
                            fontSize = 18.sp,
                            color = if (searchQuery.isEmpty()) Color(0xFF90A4AE) else Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // History section
                Text(
                    text = "History",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // History items
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    historyItems.forEach { item ->
                        FoodHistoryCard(
                            foodName = item.name,
                            date = item.date,
                            mealType = item.mealType,
                            calories = item.calories,
                            onClick = { /* Handle history item click */ }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Scan Barcode button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .border(
                                width = 3.dp,
                                color = ThemeManager.forkItBlue.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = ThemeManager.forkItBlue.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { /* Handle scan barcode */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Scan Barcode",
                            fontSize = 18.sp,
                            color = ThemeManager.forkItBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Add Food button
                    Button(
                        onClick = onNavigateToAdjustServing,
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
                                            ThemeManager.forkItGreen,
                                            ThemeManager.forkItBlue
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Add Food",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

data class FoodHistoryItem(
    val name: String,
    val date: String,
    val mealType: String,
    val calories: String
)

@Composable
fun FoodHistoryCard(
    foodName: String,
    date: String,
    mealType: String,
    calories: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = foodName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = date,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = mealType,
                    fontSize = 14.sp,
                    color = Color(0xFF999999)
                )
            }
            Text(
                text = calories,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ThemeManager.forkItGreen
            )
        }
    }
}

@Composable
fun AdjustServingScreen(
    onBackPressed: () -> Unit,
    onContinue: () -> Unit
) {
    var servingSize by remember { mutableStateOf("") }
    var numberOfServings by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
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
                    text = "Adjust Serving",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeManager.forkItGreen,
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
                    // Serving size input field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(
                                width = 3.dp,
                                color = ThemeManager.forkItBlue.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = ThemeManager.forkItBlue.copy(alpha = 0.05f),
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
                                text = if (servingSize.isEmpty()) "Serving size" else servingSize,
                                fontSize = 18.sp,
                                color = if (servingSize.isEmpty()) Color(0xFF90A4AE) else Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "cup",
                                fontSize = 18.sp,
                                color = ThemeManager.forkItBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Number of servings input field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(
                                width = 3.dp,
                                color = ThemeManager.forkItBlue.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = ThemeManager.forkItBlue.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Text(
                            text = if (numberOfServings.isEmpty()) "Number of servings" else numberOfServings,
                            fontSize = 18.sp,
                            color = if (numberOfServings.isEmpty()) Color(0xFF90A4AE) else Color.Black,
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
                                color = ThemeManager.forkItBlue.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = ThemeManager.forkItBlue.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { /* Handle date picker */ }
                    ) {
                        Text(
                            text = if (selectedDate.isEmpty()) "Select Date" else selectedDate,
                            fontSize = 18.sp,
                            color = if (selectedDate.isEmpty()) Color(0xFF90A4AE) else Color.Black,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                                .wrapContentHeight(Alignment.CenterVertically)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Select Type section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Type",
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Type buttons grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                TypeButton(
                                    text = "Breakfast",
                                    isSelected = selectedType == "Breakfast",
                                    onClick = { selectedType = "Breakfast" }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                TypeButton(
                                    text = "Lunch",
                                    isSelected = selectedType == "Lunch",
                                    onClick = { selectedType = "Lunch" }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                TypeButton(
                                    text = "Dinner",
                                    isSelected = selectedType == "Dinner",
                                    onClick = { selectedType = "Dinner" }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                TypeButton(
                                    text = "Snacks",
                                    isSelected = selectedType == "Snacks",
                                    onClick = { selectedType = "Snacks" }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Continue button
                Button(
                    onClick = onContinue,
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
                                        ThemeManager.forkItGreen,
                                        ThemeManager.forkItBlue
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Continue",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddDetailsScreen(
    onBackPressed: () -> Unit,
    onAddFood: () -> Unit
) {
    var calories by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    
    // Calculate calories automatically when macros change
    LaunchedEffect(carbs, fat, protein) {
        val carbsValue = carbs.toDoubleOrNull() ?: 0.0
        val fatValue = fat.toDoubleOrNull() ?: 0.0
        val proteinValue = protein.toDoubleOrNull() ?: 0.0
        
        // Standard calorie calculation: 4 kcal per gram of carbs/protein, 9 kcal per gram of fat
        val calculatedCalories = (carbsValue * 4) + (fatValue * 9) + (proteinValue * 4)
        
        if (calculatedCalories > 0) {
            calories = calculatedCalories.toInt().toString()
        } else if (carbs.isEmpty() && fat.isEmpty() && protein.isEmpty()) {
            calories = ""
        }
    }
    
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
                    text = "Add Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeManager.forkItGreen,
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
                    // Calories input field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(
                                width = 3.dp,
                                color = ThemeManager.forkItGreen.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = ThemeManager.forkItGreen.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = calories,
                                onValueChange = { calories = it },
                                placeholder = { Text("Calories*", color = Color(0xFF90A4AE)) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = ThemeManager.forkItGreen
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                            )
                            Text(
                                text = "kcal",
                                fontSize = 18.sp,
                                color = ThemeManager.forkItGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Calculate calories hint
                    Text(
                        text = "ForkIt can calculate this for you!",
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    // Carbs input field
                    Column {
                        Text(
                            text = "Carbs",
                            fontSize = 16.sp,
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .border(
                                    width = 3.dp,
                                    color = ThemeManager.forkItBlue.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(
                                    color = ThemeManager.forkItBlue.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = carbs,
                                    onValueChange = { carbs = it },
                                    placeholder = { Text("Enter amount", color = Color(0xFF90A4AE)) },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = ThemeManager.forkItBlue
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 18.sp,
                                        color = Color.Black
                                    )
                                )
                                Text(
                                    text = "g",
                                    fontSize = 18.sp,
                                    color = ThemeManager.forkItBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // Fat input field
                    Column {
                        Text(
                            text = "Fat",
                            fontSize = 16.sp,
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .border(
                                    width = 3.dp,
                                    color = ThemeManager.forkItBlue.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(
                                    color = ThemeManager.forkItBlue.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = fat,
                                    onValueChange = { fat = it },
                                    placeholder = { Text("Enter amount", color = Color(0xFF90A4AE)) },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = ThemeManager.forkItBlue
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 18.sp,
                                        color = Color.Black
                                    )
                                )
                                Text(
                                    text = "g",
                                    fontSize = 18.sp,
                                    color = ThemeManager.forkItBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // Protein input field
                    Column {
                        Text(
                            text = "Protein",
                            fontSize = 16.sp,
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .border(
                                    width = 3.dp,
                                    color = ThemeManager.forkItBlue.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(
                                    color = ThemeManager.forkItBlue.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = protein,
                                    onValueChange = { protein = it },
                                    placeholder = { Text("Enter amount", color = Color(0xFF90A4AE)) },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = ThemeManager.forkItBlue
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 18.sp,
                                        color = Color.Black
                                    )
                                )
                                Text(
                                    text = "g",
                                    fontSize = 18.sp,
                                    color = ThemeManager.forkItBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Add Food button
                Button(
                    onClick = onAddFood,
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
                                        ThemeManager.forkItGreen,
                                        ThemeManager.forkItBlue
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add Food",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
