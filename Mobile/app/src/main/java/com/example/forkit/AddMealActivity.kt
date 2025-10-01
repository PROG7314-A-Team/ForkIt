package com.example.forkit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
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
import com.example.forkit.data.models.*
import com.example.forkit.ui.theme.ForkItTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddMealActivity : ComponentActivity() {
    companion object {
        private const val TAG = "AddMealActivity"
    }
    
    // Use a mutable state to hold the scanned food
    private var scannedFoodState by mutableStateOf<Food?>(null)
    
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
                    },
                    onBarcodeScan = { startBarcodeScanner() },
                    scannedFood = scannedFoodState, // Pass the scanned food state
                    onScannedFoodProcessed = { scannedFoodState = null } // Clear after processing
                )
            }
        }
    }

    private val barcodeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val barcodeValue = result.data?.getStringExtra("BARCODE_VALUE")
            barcodeValue?.let { barcode ->
                // Handle the scanned barcode in a coroutine scope
                CoroutineScope(Dispatchers.Main).launch {
                    handleScannedBarcode(barcode)
                }
            }
        }
    }

    private suspend fun handleScannedBarcode(barcode: String) {
        try {
            val response = RetrofitClient.api.getFoodFromBarcode(barcode)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val foodData = response.body()?.data
                
                if (foodData != null) {
                    // Create a new Food object from the API response
                    val scannedFood = Food(
                        id = foodData.id,
                        name = foodData.name,
                        brand = foodData.brand,
                        barcode = foodData.barcode,
                        calories = foodData.calories,
                        nutrients = Nutrients(
                            carbs = foodData.nutrients.carbs,
                            protein = foodData.nutrients.protein,
                            fat = foodData.nutrients.fat,
                            fiber = foodData.nutrients.fiber,
                            sugar = foodData.nutrients.sugar
                        ),
                        image = foodData.image ?: "",
                        ingredients = foodData.ingredients ?: ""
                    )
                    
                    Log.d(TAG, "Scanned food: ${scannedFood}")

                    
                    // Show success message
                    Toast.makeText(this, "Found: ${scannedFood.name}", Toast.LENGTH_SHORT).show()
                    
                    // Update the state - this will trigger the UI to update
                    scannedFoodState = scannedFood
                    
                } else {
                    Toast.makeText(this, "No food data found for this barcode", Toast.LENGTH_LONG).show()
                }
            } else {
                val errorMessage = response.body()?.message ?: "Unknown error"
                Toast.makeText(this, "Failed to get food: $errorMessage", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting food from barcode", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun startBarcodeScanner() {
        val intent = Intent(this, BarcodeScannerActivity::class.java)
        barcodeLauncher.launch(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    userId: String,
    onBackPressed: () -> Unit,
    onSuccess: () -> Unit,
    onBarcodeScan: () -> Unit,
    scannedFood: Food?,
    onScannedFoodProcessed: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentScreen by remember { mutableStateOf("main") }
    var selectedFood by remember { mutableStateOf<RecentActivityEntry?>(null) }
    var selectedSearchFood by remember { mutableStateOf<Food?>(null) }
    
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
    
    // Handle scanned food when it changes
    LaunchedEffect(scannedFood) {
        scannedFood?.let { food ->
            Log.d("AddMealActivity", "switching current screen to adjust")
            // Pre-populate form fields with scanned food data
            foodName = food.name
            calories = food.calories.toString()
            carbs = food.nutrients.carbs.toString()
            fat = food.nutrients.fat.toString()
            protein = food.nutrients.protein.toString()
            // Navigate to adjust screen
            currentScreen = "adjust"
            // Clear the scanned food state
            onScannedFoodProcessed()
        }
    }
    
    // Handle selected search food when it changes
    LaunchedEffect(selectedSearchFood) {
        selectedSearchFood?.let { food ->
            Log.d("AddMealActivity", "switching current screen to adjust for search food")
            // Pre-populate form fields with selected search food data
            foodName = food.name
            calories = food.calories.toString()
            carbs = food.nutrients.carbs.toString()
            fat = food.nutrients.fat.toString()
            protein = food.nutrients.protein.toString()
            // Navigate to adjust screen
            currentScreen = "adjust"
            // Clear the selected search food state
            selectedSearchFood = null
        }
    }
    
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
            },
            onBarcodeScan = onBarcodeScan,
            onScannedFood = { food -> 
                // Handle scanned food - this will be called from the Activity
                Log.d("AddMealActivity", "Received scanned food in main screen: ${food.name}")
            },
            onSearchFoodSelected = { food ->
                selectedSearchFood = food
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
            onContinue = { currentScreen = "details" },
            scannedFood = scannedFood
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
    onNavigateToAdjustServing: (RecentActivityEntry?) -> Unit,
    onBarcodeScan: () -> Unit,
    onScannedFood: (Food) -> Unit,
    onSearchFoodSelected: (Food) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State for all unique foods user has ever logged
    var historyItems by remember { mutableStateOf<List<RecentActivityEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Search functionality state
    var searchResults by remember { mutableStateOf<List<SearchFoodItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showSearchResults by remember { mutableStateOf(false) }
    
    // Search function
    fun performSearch(query: String) {
        if (query.isBlank()) {
            showSearchResults = false
            searchResults = emptyList()
            return
        }
        
        scope.launch {
            isSearching = true
            try {
                val response = RetrofitClient.api.getFoodFromName(query)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        // Convert map to list of SearchFoodItem
                        searchResults = data.values.toList()
                        showSearchResults = true
                    } else {
                        searchResults = emptyList()
                        showSearchResults = false
                    }
                } else {
                    searchResults = emptyList()
                    showSearchResults = false
                    Toast.makeText(context, "No results found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                searchResults = emptyList()
                showSearchResults = false
                Toast.makeText(context, "Search error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isSearching = false
            }
        }
    }
    
    // Handle search query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            // Debounce search - wait 500ms after user stops typing
            kotlinx.coroutines.delay(500)
            performSearch(searchQuery)
        } else {
            showSearchResults = false
            searchResults = emptyList()
        }
    }
    
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
            .background(colorScheme.background)
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.onBackground
                    )
                }
                Text(
                    text = "Add Food",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.primary,
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
                            color = colorScheme.secondary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(
                            color = colorScheme.secondary.copy(alpha = 0.05f),
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
                            tint = colorScheme.secondary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search food", color = colorScheme.onSurfaceVariant) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = colorScheme.secondary
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 18.sp,
                                color = colorScheme.onBackground
                            )
                        )
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colorScheme.secondary,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Search Results
                if (showSearchResults && searchResults.isNotEmpty()) {
                    Text(
                        text = "Search Results",
                        fontSize = 16.sp,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Make search results scrollable with a maximum height
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp) // Limit height to prevent taking too much space
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            searchResults.forEach { foodItem ->
                                SearchResultCard(
                                    foodItem = foodItem,
                                    onClick = {
                                        // Convert SearchFoodItem to Food and navigate to adjust screen
                                        val selectedFood = Food(
                                            id = "",
                                            name = foodItem.name,
                                            brand = "",
                                            barcode = "",
                                            calories = foodItem.calories ?: 0.0,
                                            nutrients = foodItem.nutrients,
                                            image = foodItem.image ?: "",
                                            ingredients = ""
                                        )
                                        // Call the search food selected callback
                                        onSearchFoodSelected(selectedFood)
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // My Foods section
                Text(
                    text = "My Foods",
                    fontSize = 16.sp,
                    color = colorScheme.onSurfaceVariant,
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
                            color = colorScheme.error,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    historyItems.isEmpty() -> {
                        Text(
                            text = "No foods logged yet. Start by adding your first meal!",
                            fontSize = 14.sp,
                            color = colorScheme.onSurfaceVariant,
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
                                                // Estimate macronutrients based on calories (rough approximation)
                                                val totalCalories = item.calories.toDouble()
                                                val estimatedCarbs = (totalCalories * 0.5) / 4.0 // 50% carbs, 4 cal/g
                                                val estimatedProtein = (totalCalories * 0.2) / 4.0 // 20% protein, 4 cal/g  
                                                val estimatedFat = (totalCalories * 0.3) / 9.0 // 30% fat, 9 cal/g
                                                
                                                val request = CreateFoodLogRequest(
                                                    userId = userId,
                                                    foodName = item.foodName,
                                                    servingSize = item.servingSize,
                                                    measuringUnit = item.measuringUnit,
                                                    date = todayDate,
                                                    mealType = item.mealType,
                                                    calories = totalCalories,
                                                    carbs = estimatedCarbs,
                                                    fat = estimatedFat,
                                                    protein = estimatedProtein,
                                                    foodId = null
                                                )
                                                
                                                Log.d("AddMealActivity", "Creating food log for My Foods: ${item.foodName}, Calories: $totalCalories")
                                                val response = RetrofitClient.api.createFoodLog(request)
                                                Log.d("AddMealActivity", "Food log response: ${response.code()}, Success: ${response.body()?.success}")
                                                
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
                                    color = colorScheme.secondary.copy(alpha = 0.3f),
                                )
                                .background(
                                    color = colorScheme.secondary.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onBarcodeScan() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Scan Barcode",
                                fontSize = 18.sp,
                                color = colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
                                            colorScheme.primary,
                                            colorScheme.secondary
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = colorScheme.background,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "Add Food",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.background
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
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
fun SearchResultCard(
    foodItem: SearchFoodItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorScheme.background,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = colorScheme.outline,
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
                    text = foodItem.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                if (foodItem.calories != null) {
                    Text(
                        text = "${foodItem.calories.toInt()} kcal",
                        fontSize = 14.sp,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "C: ${foodItem.nutrients.carbs.toInt()}g",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "P: ${foodItem.nutrients.protein.toInt()}g",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "F: ${foodItem.nutrients.fat.toInt()}g",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (foodItem.image != null) {
                // You could add an image here if needed
                // For now, just show a placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = colorScheme.secondary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📷",
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

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
                color = colorScheme.background,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = colorScheme.outline,
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
                    color = colorScheme.onBackground
                )
                Text(
                    text = servingInfo,
                    fontSize = 13.sp,
                    color = colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = date,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = mealType,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
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
                    color = colorScheme.primary
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
    onContinue: () -> Unit,
    scannedFood: Food? // This will contain the scanned food data
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
            .background(colorScheme.background)
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.onBackground
                    )
                }
                Text(
                    text = "Adjust Serving",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.primary,
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
                                color = colorScheme.primary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = colorScheme.primary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        TextField(
                            value = foodName,
                            onValueChange = onFoodNameChange,
                            placeholder = { Text("Food name*", color = colorScheme.onSurfaceVariant) },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = colorScheme.primary
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 18.sp,
                                color = colorScheme.onBackground
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
                                color = colorScheme.secondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = colorScheme.secondary.copy(alpha = 0.05f),
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
                                placeholder = { Text("Serving size*", color = colorScheme.onSurfaceVariant) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = colorScheme.secondary
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 18.sp,
                                    color = colorScheme.onBackground
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colorScheme.secondary.copy(alpha = 0.15f))
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
                                        color = colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "▼",
                                        fontSize = 12.sp,
                                        color = colorScheme.secondary
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
                                color = colorScheme.secondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = colorScheme.secondary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { showDatePicker = true }
                    ) {
                        Text(
                            text = selectedDateString,
                            fontSize = 18.sp,
                            color = colorScheme.onBackground,
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
                        color = colorScheme.onSurfaceVariant,
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
                                        colorScheme.primary,
                                        colorScheme.secondary
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
                            color = colorScheme.background
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
                                            colorScheme.primary.copy(alpha = 0.1f)
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
                                        color = if (unit == measuringUnit) colorScheme.primary else colorScheme.onBackground,
                                        fontWeight = if (unit == measuringUnit) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (unit == measuringUnit) {
                                        Text(
                                            text = "✓",
                                            fontSize = 18.sp,
                                            color = colorScheme.primary,
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
            .background(colorScheme.background)
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.onBackground
                    )
                }
                Text(
                    text = "Add Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.primary,
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
                                color = colorScheme.primary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = colorScheme.primary.copy(alpha = 0.05f),
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
                                placeholder = { Text("Calories*", color = colorScheme.onSurfaceVariant) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = colorScheme.primary
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 18.sp,
                                    color = colorScheme.onBackground
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            Text(
                                text = "kcal",
                                fontSize = 18.sp,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Calculate calories hint
                    Text(
                        text = "ForkIt can calculate this for you!",
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    // Carbs input field
                    Column {
                        Text(
                            text = "Carbs",
                            fontSize = 16.sp,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .border(
                                    width = 3.dp,
                                    color = colorScheme.secondary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(
                                    color = colorScheme.secondary.copy(alpha = 0.05f),
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
                                    placeholder = { Text("Enter amount", color = colorScheme.onSurfaceVariant) },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = colorScheme.secondary
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 18.sp,
                                        color = colorScheme.onBackground
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                Text(
                                    text = "g",
                                    fontSize = 18.sp,
                                    color = colorScheme.secondary,
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
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .border(
                                    width = 3.dp,
                                    color = colorScheme.secondary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(
                                    color = colorScheme.secondary.copy(alpha = 0.05f),
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
                                    placeholder = { Text("Enter amount", color = colorScheme.onSurfaceVariant) },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = colorScheme.secondary
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 18.sp,
                                        color = colorScheme.onBackground
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                Text(
                                    text = "g",
                                    fontSize = 18.sp,
                                    color = colorScheme.secondary,
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
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .border(
                                    width = 3.dp,
                                    color = colorScheme.secondary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(
                                    color = colorScheme.secondary.copy(alpha = 0.05f),
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
                                    placeholder = { Text("Enter amount", color = colorScheme.onSurfaceVariant) },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = colorScheme.secondary
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 18.sp,
                                        color = colorScheme.onBackground
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                Text(
                                    text = "g",
                                    fontSize = 18.sp,
                                    color = colorScheme.secondary,
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
                                        colorScheme.primary,
                                        colorScheme.secondary
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
                            color = colorScheme.background
                        )
                    }
                }
            }
        }
    }
}
