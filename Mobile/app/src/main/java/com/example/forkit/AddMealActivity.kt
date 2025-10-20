package com.example.forkit

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
                        Toast.makeText(this, getString(R.string.food_logged_success), Toast.LENGTH_SHORT).show()
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
                    // Food object is already complete from API - just use it directly
                    scannedFoodState = foodData

                    Log.d(TAG, "Scanned food: ${foodData.name}, serving: ${foodData.servingSize?.quantity} ${foodData.servingSize?.unit}")

                    // Show success message
                    Toast.makeText(this, getString(R.string.found_food, foodData.name), Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, getString(R.string.no_food_found_barcode), Toast.LENGTH_LONG).show()
                }
            } else {
                val errorMessage = response.body()?.message ?: "Unknown error"
                Toast.makeText(this, getString(R.string.failed_get_food, errorMessage), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting food from barcode", e)
            Toast.makeText(this, getString(R.string.error_message, e.message ?: ""), Toast.LENGTH_LONG).show()
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
    var selectedSearchFood by remember { mutableStateOf<SearchFoodItem?>(null) }

    // Food data to be collected across screens
    var foodName by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("") }
    var measuringUnit by remember { mutableStateOf("g") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var mealType by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }

    // Store base nutritional values (per 100g or per serving) for scaling
    var baseCaloriesPer100g by remember { mutableStateOf(0.0) }
    var baseCarbsPer100g by remember { mutableStateOf(0.0) }
    var baseFatPer100g by remember { mutableStateOf(0.0) }
    var baseProteinPer100g by remember { mutableStateOf(0.0) }
    var baseServingQuantity by remember { mutableStateOf(0.0) }

    // Store unit category to filter available units
    var unitCategory by remember { mutableStateOf("all") } // "weight", "volume", or "all"

    // Handle scanned food when it changes
    LaunchedEffect(scannedFood) {
        scannedFood?.let { food ->
            Log.d("AddMealActivity", "Scanned food: ${food.name}, serving: ${food.servingSize?.quantity} ${food.servingSize?.unit}")

            // Pre-populate form fields with scanned food data
            foodName = food.name

            // Use serving size from API if available
            val servingSizeData = food.servingSize
            if (servingSizeData != null) {
                val quantity = servingSizeData.quantity ?:
                (servingSizeData.apiQuantity?.toString()?.toDoubleOrNull()) ?: 100.0
                val unit = servingSizeData.unit ?: servingSizeData.apiUnit ?: "g"

                servingSize = quantity.toString()
                measuringUnit = unit
                baseServingQuantity = quantity

                // Determine unit category based on API response
                unitCategory = when (unit.lowercase()) {
                    "g", "kg" -> "weight"
                    "ml", "l" -> "volume"
                    else -> "all"
                }

                // Use per-serving nutrients if available, otherwise scale from per 100g
                if (food.nutrientsPerServing != null && food.caloriesPerServing != null) {
                    calories = food.caloriesPerServing.toInt().toString()
                    carbs = String.format("%.1f", food.nutrientsPerServing.carbs)
                    fat = String.format("%.1f", food.nutrientsPerServing.fat)
                    protein = String.format("%.1f", food.nutrientsPerServing.protein)
                } else {
                    // Scale from per 100g values
                    val scale = quantity / 100.0
                    calories = (food.calories * scale).toInt().toString()
                    carbs = String.format("%.1f", food.nutrients.carbs * scale)
                    fat = String.format("%.1f", food.nutrients.fat * scale)
                    protein = String.format("%.1f", food.nutrients.protein * scale)
                }
            } else {
                // No serving size info - use per 100g as default
                servingSize = "100"
                measuringUnit = "g"
                baseServingQuantity = 100.0
                unitCategory = "weight" // Default to weight
                calories = food.calories.toInt().toString()
                carbs = String.format("%.1f", food.nutrients.carbs)
                fat = String.format("%.1f", food.nutrients.fat)
                protein = String.format("%.1f", food.nutrients.protein)
            }

            // Store base values for scaling
            baseCaloriesPer100g = food.calories
            baseCarbsPer100g = food.nutrients.carbs
            baseFatPer100g = food.nutrients.fat
            baseProteinPer100g = food.nutrients.protein

            // Navigate to adjust screen
            currentScreen = "adjust"
            // Clear the scanned food state
            onScannedFoodProcessed()
        }
    }

    // Handle selected search food when it changes
    LaunchedEffect(selectedSearchFood) {
        selectedSearchFood?.let { food ->
            Log.d("AddMealActivity", "Search food selected: ${food.name}, serving: ${food.servingSize?.quantity} ${food.servingSize?.unit}")

            // Pre-populate form fields with selected search food data
            foodName = food.name

            // Use serving size from API if available
            val servingSizeData = food.servingSize
            if (servingSizeData != null) {
                val quantity = servingSizeData.quantity ?:
                (servingSizeData.apiQuantity?.toString()?.toDoubleOrNull()) ?: 100.0
                val unit = servingSizeData.unit ?: servingSizeData.apiUnit ?: "g"

                servingSize = quantity.toString()
                measuringUnit = unit
                baseServingQuantity = quantity

                // Determine unit category based on API response
                unitCategory = when (unit.lowercase()) {
                    "g", "kg" -> "weight"
                    "ml", "l" -> "volume"
                    else -> "all"
                }

                // Use per-serving nutrients if available
                if (food.nutrientsPerServing != null && food.caloriesPerServing != null) {
                    calories = food.caloriesPerServing.toInt().toString()
                    carbs = String.format("%.1f", food.nutrientsPerServing.carbs)
                    fat = String.format("%.1f", food.nutrientsPerServing.fat)
                    protein = String.format("%.1f", food.nutrientsPerServing.protein)
                } else {
                    // Scale from per 100g values
                    val scale = quantity / 100.0
                    calories = ((food.calories ?: 0.0) * scale).toInt().toString()
                    carbs = String.format("%.1f", food.nutrients.carbs * scale)
                    fat = String.format("%.1f", food.nutrients.fat * scale)
                    protein = String.format("%.1f", food.nutrients.protein * scale)
                }
            } else {
                // No serving size info - use per 100g as default
                servingSize = "100"
                measuringUnit = "g"
                baseServingQuantity = 100.0
                unitCategory = "weight" // Default to weight
                calories = (food.calories ?: 0.0).toInt().toString()
                carbs = String.format("%.1f", food.nutrients.carbs)
                fat = String.format("%.1f", food.nutrients.fat)
                protein = String.format("%.1f", food.nutrients.protein)
            }

            // Store base values for scaling
            baseCaloriesPer100g = food.calories ?: 0.0
            baseCarbsPer100g = food.nutrients.carbs
            baseFatPer100g = food.nutrients.fat
            baseProteinPer100g = food.nutrients.protein

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
            onSearchFoodSelected = { foodItem ->
                selectedSearchFood = foodItem
            }
        )
        "adjust" -> AdjustServingScreen(
            foodName = foodName,
            servingSize = servingSize,
            measuringUnit = measuringUnit,
            unitCategory = unitCategory,
            selectedDate = selectedDate,
            mealType = mealType,
            onFoodNameChange = { foodName = it },
            onServingSizeChange = { newSize ->
                servingSize = newSize
                // Dynamically scale nutrients based on new quantity
                val newQuantity = newSize.toDoubleOrNull() ?: 0.0
                if (newQuantity > 0 && baseCaloriesPer100g > 0) {
                    // Scale from per 100g values
                    val scale = when (measuringUnit.lowercase()) {
                        "kg" -> newQuantity * 1000 / 100.0  // Convert kg to g, then scale
                        "l" -> newQuantity * 1000 / 100.0   // Convert l to ml, then scale
                        else -> newQuantity / 100.0          // Already in base unit (g or ml)
                    }
                    calories = (baseCaloriesPer100g * scale).toInt().toString()
                    carbs = String.format("%.1f", baseCarbsPer100g * scale)
                    fat = String.format("%.1f", baseFatPer100g * scale)
                    protein = String.format("%.1f", baseProteinPer100g * scale)
                }
            },
            onMeasuringUnitChange = { newUnit ->
                val oldUnit = measuringUnit
                val currentValue = servingSize.toDoubleOrNull() ?: 0.0

                // Convert between related units
                val convertedValue = when {
                    // g ↔ kg conversions
                    oldUnit == "g" && newUnit == "kg" -> currentValue / 1000.0
                    oldUnit == "kg" && newUnit == "g" -> currentValue * 1000.0

                    // ml ↔ l conversions
                    oldUnit == "ml" && newUnit == "l" -> currentValue / 1000.0
                    oldUnit == "l" && newUnit == "ml" -> currentValue * 1000.0

                    // No conversion needed
                    else -> currentValue
                }

                // Update unit first
                measuringUnit = newUnit

                // Update serving size with converted value if conversion happened
                if (convertedValue != currentValue && convertedValue > 0) {
                    servingSize = String.format("%.2f", convertedValue).trimEnd('0').trimEnd('.')

                    // Recalculate nutrients with the converted value
                    if (baseCaloriesPer100g > 0) {
                        val scale = when (newUnit.lowercase()) {
                            "kg" -> convertedValue * 1000 / 100.0
                            "l" -> convertedValue * 1000 / 100.0
                            else -> convertedValue / 100.0
                        }
                        calories = (baseCaloriesPer100g * scale).toInt().toString()
                        carbs = String.format("%.1f", baseCarbsPer100g * scale)
                        fat = String.format("%.1f", baseFatPer100g * scale)
                        protein = String.format("%.1f", baseProteinPer100g * scale)
                    }
                }
            },
            onDateChange = { selectedDate = it },
            onMealTypeChange = { mealType = it },
            onBackPressed = { currentScreen = "main" },
            onContinue = { currentScreen = "details" },
            scannedFood = scannedFood,
            currentCalories = calories,
            currentCarbs = carbs,
            currentFat = fat,
            currentProtein = protein
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
    onSearchFoodSelected: (SearchFoodItem) -> Unit
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

    // -------------------------------------------------------------
    // 🔄 FUNCTION: performSearchWithRetry
    // -------------------------------------------------------------
    suspend fun performSearchWithRetry(query: String, maxRetries: Int = 2): retrofit2.Response<com.example.forkit.data.models.GetFoodFromNameResponse>? {
        repeat(maxRetries) { attempt ->
            try {
                Log.d(TAG, "Search attempt ${attempt + 1} for query: $query")
                return RetrofitClient.api.getFoodFromName(query)
            } catch (e: Exception) {
                Log.d(TAG, "Search attempt ${attempt + 1} failed: ${e.message}")
                if (attempt == maxRetries - 1) throw e
                delay(1000L * (attempt + 1)) // Exponential backoff: 1s, 2s
            }
        }
        return null
    }

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
                val response = performSearchWithRetry(query)
                if (response != null) {
                    Log.d(TAG, "Food response ${response.body()?.data}")
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data
                        if (data != null) {
                            // Convert map to list of SearchFoodItem
                            searchResults = data.toList()
                            showSearchResults = true
                        } else {
                            searchResults = emptyList()
                            showSearchResults = false
                        }
                } else {
                    searchResults = emptyList()
                    showSearchResults = false
                    Toast.makeText(context, context.getString(R.string.no_results_found_api), Toast.LENGTH_SHORT).show()
                }
            } else {
                searchResults = emptyList()
                showSearchResults = false
                Toast.makeText(context, context.getString(R.string.search_failed_retries), Toast.LENGTH_SHORT).show()
            }
        } catch (e: java.net.SocketTimeoutException) {
            searchResults = emptyList()
            showSearchResults = false
            Log.d(TAG, "Search timeout: ${e.message}")
            Toast.makeText(context, context.getString(R.string.search_timeout_try_again), Toast.LENGTH_SHORT).show()
        } catch (e: java.net.UnknownHostException) {
            searchResults = emptyList()
            showSearchResults = false
            Log.d(TAG, "Network error: ${e.message}")
            Toast.makeText(context, context.getString(R.string.network_error_check_connection), Toast.LENGTH_SHORT).show()
        } catch (e: java.net.ConnectException) {
            searchResults = emptyList()
            showSearchResults = false
            Log.d(TAG, "Connection error: ${e.message}")
            Toast.makeText(context, context.getString(R.string.connection_error_server_unavailable), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            searchResults = emptyList()
            showSearchResults = false
            Log.d(TAG, "Search error: ${e.message}")
            Toast.makeText(context, context.getString(R.string.search_error, e.message ?: ""), Toast.LENGTH_SHORT).show()
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
                    contentDescription = stringResource(R.string.back),
                    tint = colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.add_food),
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
                            contentDescription = stringResource(R.string.search_food),
                            tint = colorScheme.secondary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text(stringResource(R.string.search_food), color = colorScheme.onSurfaceVariant) },
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

                // Show Search Results OR My Foods section, but not both
                if (searchQuery.isNotBlank()) {
                    // Search Results section
                    if (showSearchResults && searchResults.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.search_results),
                            fontSize = 16.sp,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Make search results scrollable with a maximum height
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp)
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
                                            // Pass SearchFoodItem directly to handler
                                            onSearchFoodSelected(foodItem)
                                        }
                                    )
                                }
                            }
                        }
                    } else if (isSearching) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.no_results_found, searchQuery),
                            fontSize = 14.sp,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    // My Foods section - only show when not searching
                    Text(
                        text = stringResource(R.string.my_foods),
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
                                text = stringResource(R.string.no_foods_logged),
                                fontSize = 14.sp,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                        else -> {
                            // History items - make scrollable with max height
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 500.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
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
                                            Toast.makeText(context, context.getString(R.string.added_to_today, item.foodName), Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, context.getString(R.string.failed_to_add_food), Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, context.getString(R.string.error_message, e.message ?: ""), Toast.LENGTH_SHORT).show()
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
                                            Toast.makeText(context, context.getString(R.string.food_deleted_successfully), Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, context.getString(R.string.failed_to_delete_food), Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, context.getString(R.string.error_message, e.message ?: ""), Toast.LENGTH_SHORT).show()
                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Scan Barcode button
                    Button(
                        onClick = { onBarcodeScan() },
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
                                text = stringResource(R.string.scan_barcode),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.background
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
                                    text = stringResource(R.string.add_food),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.background
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
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

                // Display brand if available
                if (!foodItem.brand.isNullOrBlank()) {
                    Text(
                        text = foodItem.brand,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Display serving size if available
                foodItem.servingSize?.let { serving ->
                    val servingText = if (serving.quantity != null && serving.unit != null) {
                        "${serving.quantity.toInt()} ${serving.unit}"
                    } else if (serving.original != null) {
                        serving.original
                    } else {
                        "per 100g"
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (foodItem.caloriesPerServing != null) {
                            Text(
                                text = "${foodItem.caloriesPerServing.toInt()} kcal",
                                fontSize = 14.sp,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "• $servingText",
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        } else if (foodItem.calories != null) {
                            Text(
                                text = "${foodItem.calories.toInt()} kcal/100g",
                                fontSize = 14.sp,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Display macros per serving or per 100g
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
                    stringResource(R.string.delete_food_log_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(stringResource(R.string.delete_food_log_message, foodName))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(stringResource(R.string.delete), color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
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
    unitCategory: String = "all",
    selectedDate: Date,
    mealType: String,
    onFoodNameChange: (String) -> Unit,
    onServingSizeChange: (String) -> Unit,
    onMeasuringUnitChange: (String) -> Unit,
    onDateChange: (Date) -> Unit,
    onMealTypeChange: (String) -> Unit,
    onBackPressed: () -> Unit,
    onContinue: () -> Unit,
    scannedFood: Food?, // This will contain the scanned food data
    currentCalories: String = "",
    currentCarbs: String = "",
    currentFat: String = "",
    currentProtein: String = ""
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showMeasuringUnitDialog by remember { mutableStateOf(false) }

    // Format date for display
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val selectedDateString = remember(selectedDate) { dateFormatter.format(selectedDate) }

    // Define all available measuring units by category
    val allMeasuringUnits = mapOf(
        "weight" to listOf("g", "kg"),
        "volume" to listOf("ml", "l"),
        "other" to listOf("serving", "portion", "piece", "slice", "whole", "scoop", "handful", "cup")
    )

    // Filter units based on category
    val measuringUnits = when (unitCategory) {
        "weight" -> allMeasuringUnits["weight"]!! + listOf("─────") + allMeasuringUnits["other"]!!
        "volume" -> allMeasuringUnits["volume"]!! + listOf("─────") + allMeasuringUnits["other"]!!
        else -> allMeasuringUnits["weight"]!! + listOf("─────") +
                allMeasuringUnits["volume"]!! + listOf("─────") +
                allMeasuringUnits["other"]!!
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
                        contentDescription = stringResource(R.string.back),
                        tint = colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.adjust_serving),
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
                            placeholder = { Text(stringResource(R.string.food_name_required), color = colorScheme.onSurfaceVariant) },
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
                                placeholder = { Text(stringResource(R.string.serving_size_required), color = colorScheme.onSurfaceVariant) },
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

                Spacer(modifier = Modifier.height(16.dp))

                // Show nutritional preview if data is available
                if (currentCalories.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Nutritional Info",
                                fontSize = 14.sp,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Calories",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$currentCalories kcal",
                                        fontSize = 16.sp,
                                        color = colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Carbs",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${currentCarbs}g",
                                        fontSize = 14.sp,
                                        color = colorScheme.onBackground,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Protein",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${currentProtein}g",
                                        fontSize = 14.sp,
                                        color = colorScheme.onBackground,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Fat",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${currentFat}g",
                                        fontSize = 14.sp,
                                        color = colorScheme.onBackground,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Type section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.select_type),
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
                                text = stringResource(R.string.breakfast),
                                isSelected = mealType == "Breakfast",
                                onClick = { onMealTypeChange("Breakfast") }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TypeButton(
                                text = stringResource(R.string.lunch),
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
                                text = stringResource(R.string.dinner),
                                isSelected = mealType == "Dinner",
                                onClick = { onMealTypeChange("Dinner") }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TypeButton(
                                text = stringResource(R.string.snacks),
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
                            text = stringResource(R.string.continue_btn),
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
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.cancel))
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
                        stringResource(R.string.select_measuring_unit),
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
                            // Show separator as non-clickable divider
                            if (unit == "─────") {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = colorScheme.outline.copy(alpha = 0.3f)
                                )
                            } else {
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
                                        // Show conversion hint for related units
                                        Column {
                                            Text(
                                                text = unit,
                                                fontSize = 16.sp,
                                                color = if (unit == measuringUnit) colorScheme.primary else colorScheme.onBackground,
                                                fontWeight = if (unit == measuringUnit) FontWeight.Bold else FontWeight.Normal
                                            )
                                            // Show conversion hint
                                            val hint = when (unit) {
                                                "kg" -> stringResource(R.string.conversion_kg_g)
                                                "g" -> stringResource(R.string.conversion_g_kg)
                                                "l" -> stringResource(R.string.conversion_l_ml)
                                                "ml" -> stringResource(R.string.conversion_ml_l)
                                                else -> null
                                            }
                                            if (hint != null) {
                                                Text(
                                                    text = hint,
                                                    fontSize = 11.sp,
                                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                )
                                            }
                                        }
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
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMeasuringUnitDialog = false }) {
                        Text(stringResource(R.string.close), fontSize = 16.sp, fontWeight = FontWeight.Medium)
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
            Toast.makeText(context, context.getString(R.string.please_enter_food_name), Toast.LENGTH_SHORT).show()
            return
        }
        if (servingSize.isBlank() || servingSize.toDoubleOrNull() == null) {
            Toast.makeText(context, context.getString(R.string.please_enter_valid_serving_size), Toast.LENGTH_SHORT).show()
            return
        }
        if (mealType.isBlank()) {
            Toast.makeText(context, context.getString(R.string.please_select_meal_type), Toast.LENGTH_SHORT).show()
            return
        }
        if (calories.isBlank() || calories.toDoubleOrNull() == null) {
            Toast.makeText(context, context.getString(R.string.please_enter_valid_calories), Toast.LENGTH_SHORT).show()
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
                        contentDescription = stringResource(R.string.back),
                        tint = colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.add_details),
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
                                placeholder = { Text(stringResource(R.string.calories_required), color = colorScheme.onSurfaceVariant) },
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
                                text = stringResource(R.string.kcal),
                                fontSize = 18.sp,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Calculate calories hint
                    Text(
                        text = stringResource(R.string.forkit_can_calculate),
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Carbs input field
                    Column {
                        Text(
                            text = stringResource(R.string.carbs),
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
                                    placeholder = { Text(stringResource(R.string.enter_amount), color = colorScheme.onSurfaceVariant) },
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
                            text = stringResource(R.string.fat),
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
                            text = stringResource(R.string.protein),
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