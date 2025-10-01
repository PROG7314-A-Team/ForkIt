package com.example.forkit

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.CreateFoodLogRequest
import com.example.forkit.data.models.RecentActivityEntry
import com.example.forkit.ui.theme.ForkItTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddMealActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val userId = intent.getStringExtra("USER_ID") ?: ""
        
        setContent {
            ForkItTheme {
                AddMealScreen(
                    userId = userId,
                    onBackPressed = { finish() },
                    onSuccess = {
                        Toast.makeText(this, "Food logged successfully! 🍽️", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    userId: String,
    onBackPressed: () -> Unit,
    onSuccess: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentScreen by remember { mutableStateOf("main") }
    var selectedFood by remember { mutableStateOf<RecentActivityEntry?>(null) }
    
    // Food data to be collected across screens
    var foodName by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("") }
    var measuringUnit by remember { mutableStateOf("Cup") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var mealType by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    
    when (currentScreen) {
        "main" -> AddFoodMainScreen(
            userId = userId,
            onBackPressed = onBackPressed,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onNavigateToAdjustServing = { food ->
                if (food != null) {
                    // User selected from history
                    foodName = food.foodName
                    // Pre-fill with historical data if needed
                }
                currentScreen = "adjust"
            }
        )
        "adjust" -> AdjustServingScreen(
            foodName = foodName,
            servingSize = servingSize,
            measuringUnit = measuringUnit,
            selectedDate = selectedDate,
            mealType = mealType,
            onFoodNameChange = { foodName = it },
            onServingSizeChange = { servingSize = it },
            onMeasuringUnitChange = { measuringUnit = it },
            onDateChange = { selectedDate = it },
            onMealTypeChange = { mealType = it },
            onBackPressed = { currentScreen = "main" },
            onContinue = { currentScreen = "details" }
        )
        "details" -> AddDetailsScreen(
            calories = calories,
            carbs = carbs,
            fat = fat,
            protein = protein,
            onCaloriesChange = { calories = it },
            onCarbsChange = { carbs = it },
            onFatChange = { fat = it },
            onProteinChange = { protein = it },
            onBackPressed = { currentScreen = "adjust" },
            onAddFood = {
                // This will be handled inside AddDetailsScreen
            },
            userId = userId,
            foodName = foodName,
            servingSize = servingSize,
            measuringUnit = measuringUnit,
            selectedDate = selectedDate,
            mealType = mealType,
            onSuccess = onSuccess
        )
    }
}

@Composable
fun AddFoodMainScreen(
    userId: String,
    onBackPressed: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToAdjustServing: (RecentActivityEntry?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State for all unique foods user has ever logged
    var historyItems by remember { mutableStateOf<List<RecentActivityEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Fetch all food logs and create unique list when screen loads
    LaunchedEffect(userId) {
        isLoading = true
        try {
            // Get ALL food logs for this user (no date filter)
            val response = RetrofitClient.api.getFoodLogs(userId = userId)
            if (response.isSuccessful && response.body()?.success == true) {
                val allFoodLogs = response.body()?.data ?: emptyList()
                
                // Group by food name (case-insensitive) and take the most recent entry for each unique food
                historyItems = allFoodLogs
                    .groupBy { it.foodName.lowercase().trim() }
                    .map { (_, logs) -> 
                        // Get the most recent log for this food name
                        logs.maxByOrNull { it.createdAt } ?: logs.first()
                    }
                    .map { log ->
                        // Convert to RecentActivityEntry
                        RecentActivityEntry(
                            id = log.id,
                            foodName = log.foodName,
                            servingSize = log.servingSize,
                            measuringUnit = log.measuringUnit,
                            calories = log.calories.toInt(),
                            mealType = log.mealType,
                            date = log.date,
                            createdAt = log.createdAt,
                            time = log.createdAt.substring(11, 16)
                        )
                    }
                    .sortedByDescending { it.createdAt } // Most recently logged foods first
                
                errorMessage = ""
            } else {
                errorMessage = "Failed to load food history"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
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
                    text = "Add Food",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
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
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) "Search food" else searchQuery,
                            fontSize = 18.sp,
                            color = if (searchQuery.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // My Foods section
                Text(
                    text = "My Foods",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Loading, Error, or History items
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    errorMessage.isNotEmpty() -> {
                        Text(
                            text = errorMessage,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    historyItems.isEmpty() -> {
                        Text(
                            text = "No foods logged yet. Start by adding your first meal!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    else -> {
                        // History items
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            historyItems.forEach { item ->
                                FoodHistoryCard(
                                    foodName = item.foodName,
                                    servingInfo = "${item.servingSize} ${item.measuringUnit}",
                                    date = item.date,
                                    mealType = item.mealType,
                                    calories = "${item.calories} kcal",
                                    onClick = {
                                        // Quick add the food to today's intake
                                        scope.launch {
                                            try {
                                                // Get today's date
                                                val apiDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                val todayDate = apiDateFormatter.format(Date())
                                                
                                                // Create food log request with the same details
                                                val request = CreateFoodLogRequest(
                                                    userId = userId,
                                                    foodName = item.foodName,
                                                    servingSize = item.servingSize,
                                                    measuringUnit = item.measuringUnit,
                                                    date = todayDate,
                                                    mealType = item.mealType,
                                                    calories = item.calories.toDouble(),
                                                    carbs = 0.0, // Historical data doesn't have macros
                                                    fat = 0.0,
                                                    protein = 0.0,
                                                    foodId = null
                                                )
                                                
                                                val response = RetrofitClient.api.createFoodLog(request)
                                                
                                                if (response.isSuccessful && response.body()?.success == true) {
                                                    Toast.makeText(context, "✓ ${item.foodName} added to today!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Failed to add food", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onDelete = {
                                        scope.launch {
                                            try {
                                                val response = RetrofitClient.api.deleteFoodLog(item.id)
                                                if (response.isSuccessful && response.body()?.success == true) {
                                                    // Remove from local list
                                                    historyItems = historyItems.filter { it.id != item.id }
                                                    Toast.makeText(context, "Food deleted successfully", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Failed to delete food", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
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
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { /* Handle scan barcode */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Scan Barcode",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Add Food button
                    Button(
                        onClick = { onNavigateToAdjustServing(null) },
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
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.background,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "Add Food",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.background
                                )
                            }
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
    servingInfo: String,
    date: String,
    mealType: String,
    calories: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick() }
            ) {
                Text(
                    text = foodName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = servingInfo,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = date,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = mealType,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = calories,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Delete button
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
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
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { 
                Text(
                    "Delete Food Log?",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text("Are you sure you want to delete \"$foodName\" from your log?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustServingScreen(
    foodName: String,
    servingSize: String,
    measuringUnit: String,
    selectedDate: Date,
    mealType: String,
    onFoodNameChange: (String) -> Unit,
    onServingSizeChange: (String) -> Unit,
    onMeasuringUnitChange: (String) -> Unit,
    onDateChange: (Date) -> Unit,
    onMealTypeChange: (String) -> Unit,
    onBackPressed: () -> Unit,
    onContinue: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showMeasuringUnitDialog by remember { mutableStateOf(false) }
    
    // Format date for display
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val selectedDateString = remember(selectedDate) { dateFormatter.format(selectedDate) }
    
    // Available measuring units
    val measuringUnits = listOf(
        "Cup", 
        "ML", 
        "Grams", 
        "Ounces", 
        "Tablespoons", 
        "Teaspoons", 
        "Serving",
        "Portion",
        "Slice",
        "Piece",
        "Bowl",
        "Plate",
        "Glass",
        "Liter",
        "Kilogram",
        "Pound"
    )
    
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
                    text = "Adjust Serving",
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
                    // Food name input field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        TextField(
                            value = foodName,
                            onValueChange = onFoodNameChange,
                            placeholder = { Text("Food name*", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                    
                    // Serving size input field
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
                            TextField(
                                value = servingSize,
                                onValueChange = onServingSizeChange,
                                placeholder = { Text("Serving size*", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.secondary
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                                    .clickable { showMeasuringUnitDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = measuringUnit.lowercase(),
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "▼",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    isSelected = mealType == "Breakfast",
                                    onClick = { onMealTypeChange("Breakfast") }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                TypeButton(
                                    text = "Lunch",
                                    isSelected = mealType == "Lunch",
                                    onClick = { onMealTypeChange("Lunch") }
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
                                    isSelected = mealType == "Dinner",
                                    onClick = { onMealTypeChange("Dinner") }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                TypeButton(
                                    text = "Snacks",
                                    isSelected = mealType == "Snacks",
                                    onClick = { onMealTypeChange("Snacks") }
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
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
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
                            datePickerState.selectedDateMillis?.let {
                                onDateChange(Date(it))
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        
        // Measuring Unit Dialog
        if (showMeasuringUnitDialog) {
            AlertDialog(
                onDismissRequest = { showMeasuringUnitDialog = false },
                title = { 
                    Text(
                        "Select Measuring Unit",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        measuringUnits.forEach { unit ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (unit == measuringUnit) 
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                                        else 
                                            Color.Transparent
                                    )
                                    .clickable {
                                        onMeasuringUnitChange(unit)
                                        showMeasuringUnitDialog = false
                                    }
                                    .padding(vertical = 14.dp, horizontal = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = unit,
                                        fontSize = 16.sp,
                                        color = if (unit == measuringUnit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                        fontWeight = if (unit == measuringUnit) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (unit == measuringUnit) {
                                        Text(
                                            text = "✓",
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMeasuringUnitDialog = false }) {
                        Text("Close", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            )
        }
    }
}

@Composable
fun AddDetailsScreen(
    calories: String,
    carbs: String,
    fat: String,
    protein: String,
    onCaloriesChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    userId: String,
    foodName: String,
    servingSize: String,
    measuringUnit: String,
    selectedDate: Date,
    mealType: String,
    onSuccess: () -> Unit,
    onBackPressed: () -> Unit,
    onAddFood: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Calculate calories automatically when macros change
    LaunchedEffect(carbs, fat, protein) {
        val carbsValue = carbs.toDoubleOrNull() ?: 0.0
        val fatValue = fat.toDoubleOrNull() ?: 0.0
        val proteinValue = protein.toDoubleOrNull() ?: 0.0
        
        // Standard calorie calculation: 4 kcal per gram of carbs/protein, 9 kcal per gram of fat
        val calculatedCalories = (carbsValue * 4) + (fatValue * 9) + (proteinValue * 4)
        
        if (calculatedCalories > 0) {
            onCaloriesChange(calculatedCalories.toInt().toString())
        } else if (carbs.isEmpty() && fat.isEmpty() && protein.isEmpty()) {
            onCaloriesChange("")
        }
    }
    
    // Function to save food log
    fun saveFoodLog() {
        // Validate inputs
        if (foodName.isBlank()) {
            Toast.makeText(context, "Please enter food name", Toast.LENGTH_SHORT).show()
            return
        }
        if (servingSize.isBlank() || servingSize.toDoubleOrNull() == null) {
            Toast.makeText(context, "Please enter valid serving size", Toast.LENGTH_SHORT).show()
            return
        }
        if (mealType.isBlank()) {
            Toast.makeText(context, "Please select meal type", Toast.LENGTH_SHORT).show()
            return
        }
        if (calories.isBlank() || calories.toDoubleOrNull() == null) {
            Toast.makeText(context, "Please enter valid calories", Toast.LENGTH_SHORT).show()
            return
        }
        
        scope.launch {
            isLoading = true
            errorMessage = ""
            
            try {
                // Format date for API
                val apiDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateString = apiDateFormatter.format(selectedDate)
                
                // Create food log request
                val request = CreateFoodLogRequest(
                    userId = userId,
                    foodName = foodName,
                    servingSize = servingSize.toDouble(),
                    measuringUnit = measuringUnit,
                    date = dateString,
                    mealType = mealType,
                    calories = calories.toDoubleOrNull() ?: 0.0,
                    carbs = carbs.toDoubleOrNull() ?: 0.0,
                    fat = fat.toDoubleOrNull() ?: 0.0,
                    protein = protein.toDoubleOrNull() ?: 0.0,
                    foodId = null
                )
                
                val response = RetrofitClient.api.createFoodLog(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess()
                } else {
                    errorMessage = response.body()?.message ?: "Failed to save food log"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
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
                    text = "Add Details",
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
                    // Calories input field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
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
                                onValueChange = onCaloriesChange,
                                placeholder = { Text("Calories*", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            Text(
                                text = "kcal",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Calculate calories hint
                    Text(
                        text = "ForkIt can calculate this for you!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    // Carbs input field
                    Column {
                        Text(
                            text = "Carbs",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                        )
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
                                TextField(
                                    value = carbs,
                                    onValueChange = onCarbsChange,
                                    placeholder = { Text("Enter amount", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                Text(
                                    text = "g",
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.secondary,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                        )
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
                                TextField(
                                    value = fat,
                                    onValueChange = onFatChange,
                                    placeholder = { Text("Enter amount", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                Text(
                                    text = "g",
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.secondary,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                        )
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
                                TextField(
                                    value = protein,
                                    onValueChange = onProteinChange,
                                    placeholder = { Text("Enter amount", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                Text(
                                    text = "g",
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Add Food button
                Button(
                    onClick = { saveFoodLog() },
                    enabled = !isLoading,
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
                            text = "Add Food",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.background
                        )
                    }
                }
            }
        }
    }
}
