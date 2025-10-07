package com.example.forkit.ui.meals

import android.app.Activity
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.BarcodeScannerActivity
import com.example.forkit.TypeButton
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.*
import com.example.forkit.ui.theme.ForkItTheme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.*
import com.example.forkit.data.models.MealIngredient


private const val TAG= "MealsDebug"
class AddIngredientActivity : ComponentActivity() {

    private var scannedFoodState by mutableStateOf<Food?>(null)

    // -------------------------------------------------------------
    // 🧠 Function: onCreate
    // 📍 Purpose: Entry point when AddIngredientActivity is launched.
    // Sets up the Composable UI and defines callback behavior for
    // returning the selected ingredient back to the calling screen.
    // -------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Enable modern layout rendering (status & nav bar handling)
        enableEdgeToEdge()
        Log.d(TAG, "🟢 [onCreate] -> AddIngredientActivity started successfully. Edge-to-edge UI enabled.")

        // 🧩 Setup the main Compose content for this activity
        setContent {
            Log.d(TAG, "🎨 [onCreate] -> Setting Compose content for AddIngredientScreen...")

            ForkItTheme {
                AddIngredientScreen(
                    // 🔙 Handles back button press (activity exit)
                    onBackPressed = {
                        Log.d(TAG, "🔙 [onCreate] -> Back pressed. Finishing AddIngredientActivity.")
                        finish()
                    },

                    // 📷 Launches barcode scanner when user taps scan button
                    onBarcodeScan = {
                        Log.d(TAG, "🔍 [onCreate] -> Barcode scan initiated from AddIngredientScreen.")
                        startBarcodeScanner()
                    },

                    // 📦 Observes if any scanned ingredient has been loaded
                    scannedFood = scannedFoodState,

                    // ♻️ Clears the scanned ingredient once it's processed
                    onScannedFoodProcessed = {
                        Log.d(TAG, "♻️ [onCreate] -> Scanned ingredient processed. Resetting state to null.")
                        scannedFoodState = null
                    },

                    // ✅ Callback when the ingredient has been finalized and is ready to return
                    onIngredientReady = { ingredient ->
                        Log.d(TAG, "✅ [onCreate] -> Ingredient ready to return -> ${ingredient} (${ingredient.calories} kcal)")

                        // 🧾 Prepare result intent to send data back to parent activity
                        val resultIntent = Intent().apply {
                            putExtra("NEW_INGREDIENT", Gson().toJson(ingredient))
                        }

                        // 🎯 Return the ingredient to the calling screen
                        setResult(Activity.RESULT_OK, resultIntent)
                        Log.d(TAG, "📤 [onCreate] -> Ingredient packaged and returning to previous activity.")

                        // 🚪 Close the AddIngredientActivity
                        finish()
                        Log.d(TAG, "🏁 [onCreate] -> AddIngredientActivity finished and closed.")
                    }
                )
            }

            Log.d(TAG, "🎬 [onCreate] -> AddIngredientScreen successfully rendered on screen.")
        }
    }

    // -------------------------------------------------------------
    // 🧩 Function: barcodeLauncher
    // 📍 Purpose: Waits for a result from BarcodeScannerActivity.
    // If successful, retrieves the barcode value and triggers
    // handleScannedBarcode() coroutine for API lookup.
    // -------------------------------------------------------------
    private val barcodeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "🎯 [barcodeLauncher] -> Barcode scanning result received with resultCode=${result.resultCode}")

        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "✅ [barcodeLauncher] -> Barcode scan successful. Extracting barcode data...")

            // 🧾 Retrieve the scanned barcode string from the returning Intent
            val barcodeValue = result.data?.getStringExtra("BARCODE_VALUE")

            // 🔎 If barcodeValue exists, process it
            barcodeValue?.let { barcode ->
                Log.d(TAG, "📦 [barcodeLauncher] -> Received barcode value: $barcode")
                Log.d(TAG, "⚙️ [barcodeLauncher] -> Launching coroutine to handle scanned barcode...")

                // 🚀 Launch coroutine on main thread to call Retrofit API
                CoroutineScope(Dispatchers.Main).launch {
                    handleScannedBarcode(barcode)
                }
            } ?: run {
                Log.w(TAG, "⚠️ [barcodeLauncher] -> No barcode value returned from scanner Intent.")
            }
        } else {
            Log.w(TAG, "❌ [barcodeLauncher] -> Barcode scan canceled or failed. No action taken.")
        }
    }


    // -------------------------------------------------------------
    // 🧠 Function: handleScannedBarcode
    // 📍 Purpose: Takes a scanned barcode string, calls the API to
    // fetch ingredient details, validates the response, and updates
    // the scannedFoodState for the Composable to reactively use.
    // -------------------------------------------------------------
    private suspend fun handleScannedBarcode(barcode: String) {
        Log.d(TAG, "🚀 [handleScannedBarcode] -> Starting ingredient fetch for barcode: $barcode")

        try {
            // 🌐 Step 1: Make the API call via Retrofit
            Log.d(TAG, "🌍 [handleScannedBarcode] -> Sending request to getIngredientFromBarcode endpoint...")
            val response = RetrofitClient.api.getFoodFromBarcode(barcode)
            Log.d(TAG, "📡 [handleScannedBarcode] -> Response received with HTTP code: ${response.code()}")

            // ✅ Step 2: Check if the response succeeded
            if (response.isSuccessful && response.body()?.success == true) {
                val ingredientData = response.body()?.data
                Log.d(TAG, "📦 [handleScannedBarcode] -> API call successful. Data object: $ingredientData")

                // 🧠 Step 3: Validate that data exists
                if (ingredientData != null) {
                    Log.d(TAG, "✅ [handleScannedBarcode] -> Ingredient found: ${ingredientData.name}")

                    // 🧩 Step 4: Update scannedFoodState (triggers Composable re-render)
                    scannedFoodState = ingredientData
                    Log.d(TAG, "🔁 [handleScannedBarcode] -> scannedFoodState updated successfully.")

                    // 🎉 Step 5: Provide UI feedback to user
                    Toast.makeText(this, "Found: ${ingredientData.name}", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "📢 [handleScannedBarcode] -> Toast displayed for ingredient: ${ingredientData.name}")

                } else {
                    // ❌ Case: No ingredient data returned
                    Log.w(TAG, "⚠️ [handleScannedBarcode] -> API returned success=true but data=null.")
                    Toast.makeText(this, "No ingredient found for this barcode", Toast.LENGTH_SHORT).show()
                }

            } else {
                // ❌ Case: API call failed or returned error status
                val errorMsg = response.body()?.message ?: "Unknown API error"
                Log.e(TAG, "❌ [handleScannedBarcode] -> Failed API response. Reason: $errorMsg")
                Toast.makeText(this, "Failed to fetch ingredient: $errorMsg", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            // 💥 Catch-all for any exceptions (network, parsing, etc.)
            Log.e(TAG, "🔥 [handleScannedBarcode] -> Exception occurred while fetching ingredient.", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        Log.d(TAG, "🏁 [handleScannedBarcode] -> Completed execution for barcode: $barcode")
    }

    // -------------------------------------------------------------
    // 🧠 Function: startBarcodeScanner
    // 📍 Purpose: Creates an Intent to launch BarcodeScannerActivity
    // and starts it using the ActivityResultLauncher.
    // -------------------------------------------------------------
    private fun startBarcodeScanner() {
        Log.d(TAG, "🎬 [startBarcodeScanner] -> Preparing to launch BarcodeScannerActivity...")

        // 🎯 Step 1: Create explicit Intent for BarcodeScannerActivity
        val intent = Intent(this, BarcodeScannerActivity::class.java)
        Log.d(TAG, "📦 [startBarcodeScanner] -> Intent created for BarcodeScannerActivity.")

        // 🚀 Step 2: Launch scanner via ActivityResultLauncher
        barcodeLauncher.launch(intent)
        Log.d(TAG, "📸 [startBarcodeScanner] -> BarcodeScannerActivity launched successfully.")
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientScreen(
    onBackPressed: () -> Unit,
    onBarcodeScan: () -> Unit,
    scannedFood: Food?,
    onScannedFoodProcessed: () -> Unit,
    onIngredientReady: (MealIngredient) -> Unit
) {
    // -------------------------------------------------------------
    // 🧠 State Variables — these persist across recompositions
    // -------------------------------------------------------------
    Log.d(TAG, "🧩 [AddIngredientScreen] -> Initializing all UI states and reactive variables...")

    var searchQuery by remember { mutableStateOf("") }
    var currentScreen by remember { mutableStateOf("main") } // 👈 Tracks which sub-screen is visible
    var selectedSearchFood by remember { mutableStateOf<SearchFoodItem?>(null) }

    // Ingredient details captured across screens
    var foodName by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("") }
    var measuringUnit by remember { mutableStateOf("g") }
    var calories by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }

    // Base nutritional data (for scaling)
    var baseCaloriesPer100g by remember { mutableStateOf(0.0) }
    var baseCarbsPer100g by remember { mutableStateOf(0.0) }
    var baseFatPer100g by remember { mutableStateOf(0.0) }
    var baseProteinPer100g by remember { mutableStateOf(0.0) }
    var baseServingQuantity by remember { mutableStateOf(100.0) }

    // Determines unit type for UI filtering (weight/volume)
    var unitCategory by remember { mutableStateOf("all") }

    // -------------------------------------------------------------
    // 🔄 REACTIVE SECTION — Handle scanned ingredient input
    // -------------------------------------------------------------
    LaunchedEffect(scannedFood) {
        if (scannedFood != null) {
            Log.d(TAG, "📡 [AddIngredientScreen] -> Detected new scanned ingredient: ${scannedFood.name}")

            // --- Extract basic ingredient details ---
            foodName = scannedFood.name
            val servingData = scannedFood.servingSize
            val qty = servingData?.quantity ?: 100.0
            val unit = servingData?.unit ?: "g"
            servingSize = qty.toString()
            measuringUnit = unit
            baseServingQuantity = qty

            // --- Determine unit category ---
            unitCategory = when (unit.lowercase()) {
                "g", "kg" -> "weight"
                "ml", "l" -> "volume"
                else -> "all"
            }

            // --- Apply nutrition and macro details ---
            calories = scannedFood.calories.toInt().toString()
            carbs = String.format("%.1f", scannedFood.nutrients.carbs)
            fat = String.format("%.1f", scannedFood.nutrients.fat)
            protein = String.format("%.1f", scannedFood.nutrients.protein)

            // --- Store base values for future scaling ---
            baseCaloriesPer100g = scannedFood.calories
            baseCarbsPer100g = scannedFood.nutrients.carbs
            baseFatPer100g = scannedFood.nutrients.fat
            baseProteinPer100g = scannedFood.nutrients.protein

            // --- Navigate to Adjust screen ---
            currentScreen = "adjust"
            Log.d(TAG, "🔁 [AddIngredientScreen] -> Switched screen: main ➜ adjust")

            // --- Clear scanned state to prevent loop ---
            onScannedFoodProcessed()
            Log.d(TAG, "♻️ [AddIngredientScreen] -> Scanned ingredient processed and cleared from state.")
        } else {
            Log.d(TAG, "⚪ [AddIngredientScreen] -> No scanned ingredient detected this frame.")
        }
    }

    // -------------------------------------------------------------
    // 🔄 REACTIVE SECTION — Handle search selection
    // -------------------------------------------------------------
    LaunchedEffect(selectedSearchFood) {
        selectedSearchFood?.let { food ->
            Log.d(TAG, "🧭 [AddIngredientScreen] -> Search ingredient selected: ${food.name}")

            // --- Extract data same as scanned ---
            foodName = food.name
            val servingData = food.servingSize
            val qty = servingData?.quantity ?: 100.0
            val unit = servingData?.unit ?: "g"
            servingSize = qty.toString()
            measuringUnit = unit
            baseServingQuantity = qty

            unitCategory = when (unit.lowercase()) {
                "g", "kg" -> "weight"
                "ml", "l" -> "volume"
                else -> "all"
            }

            calories = (food.calories ?: 0.0).toInt().toString()
            carbs = String.format("%.1f", food.nutrients.carbs)
            fat = String.format("%.1f", food.nutrients.fat)
            protein = String.format("%.1f", food.nutrients.protein)

            baseCaloriesPer100g = food.calories ?: 0.0
            baseCarbsPer100g = food.nutrients.carbs
            baseFatPer100g = food.nutrients.fat
            baseProteinPer100g = food.nutrients.protein

            // --- Transition to Adjust screen ---
            currentScreen = "adjust"
            Log.d(TAG, "🔁 [AddIngredientScreen] -> Transitioning to adjust screen after search select.")

            // --- Reset selection ---
            selectedSearchFood = null
            Log.d(TAG, "🧹 [AddIngredientScreen] -> Cleared selectedSearchFood state.")
        }
    }

    // -------------------------------------------------------------
    // 🧭 SCREEN FLOW CONTROLLER
    // Controls which sub-screen is visible in the ingredient creation process
    // -------------------------------------------------------------
    when (currentScreen) {

        // ---------------------------------------------------------
        // 🟦 MAIN SCREEN — Ingredient Search, History, and Scan Entry
        // ---------------------------------------------------------
        "main" -> {
            Log.d(TAG, "🟢 [AddIngredientScreen] -> Displaying MAIN screen.")
            AddFoodMainScreen(
                userId = "ingredient_mode",
                onBackPressed = {
                    Log.d(TAG, "🔙 [MAIN] -> Back pressed. Returning to previous activity.")
                    onBackPressed()
                },
                searchQuery = searchQuery,
                onSearchQueryChange = { newQuery ->
                    searchQuery = newQuery
                    Log.d(TAG, "⌨️ [MAIN] -> Search query updated to: $newQuery")
                },
                onNavigateToAdjustServing = {
                    Log.d(TAG, "➡️ [MAIN] -> Manually navigating to Adjust screen.")
                    currentScreen = "adjust"
                },
                onBarcodeScan = {
                    Log.d(TAG, "📸 [MAIN] -> Barcode scan triggered from main screen.")
                    onBarcodeScan()
                },
                onScannedFood = { Log.d(TAG, "⚙️ [MAIN] -> onScannedFood callback invoked (no direct use here).") },
                onSearchFoodSelected = { selectedFood ->
                    Log.d(TAG, "🧾 [MAIN] -> Search result tapped: ${selectedFood.name}")
                    selectedSearchFood = selectedFood
                }
            )
        }

        // ---------------------------------------------------------
        // 🟨 ADJUST SCREEN — Adjust portion size and units
        // ---------------------------------------------------------
        "adjust" -> {
            AdjustServingScreen(
                foodName = foodName,
                servingSize = servingSize,
                measuringUnit = measuringUnit,
                unitCategory = unitCategory,
                selectedDate = Date(),
                mealType = "",
                onFoodNameChange = { foodName = it },
                onServingSizeChange = { servingSize = it },
                onMeasuringUnitChange = { measuringUnit = it },
                onDateChange = { /* ignore for now */ },
                onMealTypeChange = { /* ignore for now */ },
                onBackPressed = onBackPressed,
                onContinue = {
                    Log.d(TAG, "✅ [AdjustServingScreen] -> Finalizing ingredient creation...")

                    val ingredient = MealIngredient(
                        id = UUID.randomUUID().toString(),
                        foodName = foodName,
                        servingSize = servingSize.toDoubleOrNull() ?: 100.0,
                        measuringUnit = measuringUnit,
                        calories = calories.toDoubleOrNull() ?: 0.0,
                        carbs = carbs.toDoubleOrNull() ?: 0.0,
                        fat = fat.toDoubleOrNull() ?: 0.0,
                        protein = protein.toDoubleOrNull() ?: 0.0
                    )

                    Log.d(TAG, "📦 [AdjustServingScreen] -> Ingredient built: ${ingredient.foodName}")
                    onIngredientReady(ingredient)
                },
                scannedFood = scannedFood,
                currentCalories = calories,
                currentCarbs = carbs,
                currentFat = fat,
                currentProtein = protein
            )
        }


    }

    Log.d(TAG, "🧩 [AddIngredientScreen] -> Current active screen: $currentScreen")
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

    // -------------------------------------------------------------
    // 🧠 STATE INITIALIZATION
    // -------------------------------------------------------------
    Log.d(TAG, "🧩 [AddFoodMainScreen] -> Initializing UI + state management for ingredient mode...")

    var historyItems by remember { mutableStateOf<List<RecentActivityEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    var searchResults by remember { mutableStateOf<List<SearchFoodItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showSearchResults by remember { mutableStateOf(false) }

    // -------------------------------------------------------------
    // 🔎 FUNCTION: performSearch
    // -------------------------------------------------------------
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
                    Toast.makeText(context, "No results found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                searchResults = emptyList()
                showSearchResults = false
                Log.d(TAG, "Fething food error ${e.message}")
                Toast.makeText(context, "Search error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isSearching = false
            }
        }
    }


    // -------------------------------------------------------------
    // 🔄 REACTIVE SEARCH QUERY HANDLER (Debounce)
    // -------------------------------------------------------------
    LaunchedEffect(searchQuery) {
        Log.d(TAG, "⌨️ [AddFoodMainScreen] -> Search query changed to: \"$searchQuery\"")
        if (searchQuery.isNotBlank()) {
            kotlinx.coroutines.delay(500) // debounce delay
            performSearch(searchQuery)
        } else {
            Log.d(TAG, "⚪ [AddFoodMainScreen] -> Empty query. Resetting search state.")
            showSearchResults = false
            searchResults = emptyList()
        }
    }

    // -------------------------------------------------------------
    // 🔄 LOAD USER HISTORY ON STARTUP
    // -------------------------------------------------------------
    LaunchedEffect(userId) {
        Log.d(TAG, "📦 [AddFoodMainScreen] -> Fetching food history for userId=$userId")
        isLoading = true

        try {
            val response = RetrofitClient.api.getFoodLogs(userId = userId)
            Log.d(TAG, "📡 [AddFoodMainScreen] -> History HTTP status: ${response.code()}")

            if (response.isSuccessful && response.body()?.success == true) {
                val allFoodLogs = response.body()?.data ?: emptyList()
                Log.d(TAG, "✅ [AddFoodMainScreen] -> Retrieved ${allFoodLogs.size} raw log entries.")

                // Process unique recent foods
                historyItems = allFoodLogs
                    .groupBy { it.foodName.lowercase().trim() }
                    .map { (_, logs) -> logs.maxByOrNull { it.createdAt } ?: logs.first() }
                    .map { log ->
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
                    .sortedByDescending { it.createdAt }

                Log.d(TAG, "📋 [AddFoodMainScreen] -> Processed unique history list (${historyItems.size} entries).")
                errorMessage = ""
            } else {
                errorMessage = "Failed to load food history"
                Log.e(TAG, "❌ [AddFoodMainScreen] -> Failed to load food history from API.")
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
            Log.e(TAG, "🔥 [AddFoodMainScreen] -> Exception during history fetch: ${e.message}")
        } finally {
            isLoading = false
            Log.d(TAG, "🏁 [AddFoodMainScreen] -> History fetch complete.")
        }
    }

    // -------------------------------------------------------------
    // 🧱 MAIN UI LAYOUT STRUCTURE
    // -------------------------------------------------------------
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
            // -----------------------------------------------------
            // HEADER
            // -----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    Log.d(TAG, "🔙 [AddFoodMainScreen] -> Back pressed. Closing screen.")
                    onBackPressed()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.onBackground
                    )
                }
                Text(
                    text = "Add Ingredient",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // -----------------------------------------------------
            // SEARCH BAR
            // -----------------------------------------------------
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
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
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
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
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, color = colorScheme.onBackground)
                        )
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.secondary, strokeWidth = 2.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // -----------------------------------------------------
                // CONDITIONAL: SEARCH RESULTS OR HISTORY
                // -----------------------------------------------------
                if (searchQuery.isNotBlank()) {
                    Log.d(TAG, "🔎 [AddFoodMainScreen] -> Displaying search results UI.")
                    if (showSearchResults && searchResults.isNotEmpty()) {
                        Text("Search Results", fontSize = 16.sp, color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 16.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            searchResults.forEach { foodItem ->
                                SearchResultCard(foodItem = foodItem, onClick = {
                                    Log.d(TAG, "🧾 [AddFoodMainScreen] -> Search item clicked: ${foodItem.name}")
                                    onSearchFoodSelected(foodItem)
                                })
                            }
                        }
                    } else if (isSearching) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Text("No results found for \"$searchQuery\"", fontSize = 14.sp, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
                    }
                } else {
                    // -----------------------------------------------------
                    // HISTORY SECTION
                    // -----------------------------------------------------
                    Log.d(TAG, "📚 [AddFoodMainScreen] -> Displaying My Foods (history).")
                    Text("My Foods", fontSize = 16.sp, color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 16.dp))

                    when {
                        isLoading -> {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        errorMessage.isNotEmpty() -> {
                            Text(text = errorMessage, fontSize = 14.sp, color = colorScheme.error, modifier = Modifier.padding(vertical = 16.dp))
                        }
                        historyItems.isEmpty() -> {
                            Text("No foods logged yet. Start by adding your first ingredient!", fontSize = 14.sp, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
                        }
                        else -> {
                            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                historyItems.forEach { item ->
                                    FoodHistoryCard(
                                        foodName = item.foodName,
                                        servingInfo = "${item.servingSize} ${item.measuringUnit}",
                                        date = item.date,
                                        mealType = item.mealType,
                                        calories = "${item.calories} kcal",
                                        onClick = {
                                            Log.d(TAG, "📥 [AddFoodMainScreen] -> Quick add clicked for ${item.foodName}")
                                            onNavigateToAdjustServing(item)
                                        },
                                        onDelete = {
                                            Log.d(TAG, "🗑️ [AddFoodMainScreen] -> Delete clicked for ${item.foodName}")
                                            scope.launch {
                                                try {
                                                    val response = RetrofitClient.api.deleteFoodLog(item.id)
                                                    if (response.isSuccessful && response.body()?.success == true) {
                                                        historyItems = historyItems.filter { it.id != item.id }
                                                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                                                        Log.d(TAG, "✅ [AddFoodMainScreen] -> Deleted ${item.foodName} from history.")
                                                    } else {
                                                        Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "🔥 [AddFoodMainScreen] -> Error deleting: ${e.message}")
                                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // -----------------------------------------------------
                // ACTION BUTTONS
                // -----------------------------------------------------
                Column(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Scan button
                    Button(
                        onClick = {
                            Log.d(TAG, "📸 [AddFoodMainScreen] -> Barcode scan button pressed.")
                            onBarcodeScan()
                        },
                        modifier = Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(16.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                brush = Brush.horizontalGradient(listOf(colorScheme.primary, colorScheme.secondary)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Scan Barcode", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = colorScheme.background)
                        }
                    }

                    // Add Ingredient button
                    Button(
                        onClick = {
                            Log.d(TAG, "🧩 [AddFoodMainScreen] -> Add Ingredient button pressed. Navigating to adjust screen.")
                            onNavigateToAdjustServing(null)
                        },
                        modifier = Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(16.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                brush = Brush.horizontalGradient(listOf(colorScheme.primary, colorScheme.secondary)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = colorScheme.background, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Add Ingredient", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = colorScheme.background)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Log.d(TAG, "🏁 [AddFoodMainScreen] -> Render complete for ingredient mode.")
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
    // Log creation of each card (for traceability during scrolling / recomposition)
    Log.d(TAG, "🧩 [SearchResultCard] -> Rendering card for: ${foodItem.name}")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.background, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = colorScheme.outline, shape = RoundedCornerShape(12.dp))
            .clickable {
                Log.d(TAG, "🖱️ [SearchResultCard] -> Card clicked for: ${foodItem.name}")
                onClick()
            }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ---------------------------------------------------------
            // LEFT COLUMN — Food name, brand, calories, macros
            // ---------------------------------------------------------
            Column(modifier = Modifier.weight(1f)) {

                // --- Title ---
                Text(
                    text = foodItem.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )

                // --- Brand ---
                if (!foodItem.brand.isNullOrBlank()) {
                    Text(
                        text = foodItem.brand,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // --- Serving Size / Calories ---
                foodItem.servingSize?.let { serving ->
                    val servingText = when {
                        serving.quantity != null && serving.unit != null -> "${serving.quantity.toInt()} ${serving.unit}"
                        serving.original != null -> serving.original
                        else -> "per 100g"
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
                        } else {
                            Log.w(TAG, "⚠️ [SearchResultCard] -> Missing calorie data for: ${foodItem.name}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // --- Macronutrients ---
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

            // ---------------------------------------------------------
            // RIGHT COLUMN — Optional image placeholder
            // ---------------------------------------------------------
            if (foodItem.image != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = colorScheme.secondary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷", fontSize = 20.sp)
                }
            } else {
                Log.d(TAG, "🖼️ [SearchResultCard] -> No image for ${foodItem.name}, using placeholder.")
            }
        }
    }

    Log.d(TAG, "🏁 [SearchResultCard] -> Render complete for: ${foodItem.name}")
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

    Log.d(TAG, "🧱 [FoodHistoryCard] -> Rendering history card for: $foodName ($calories)")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.background, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = colorScheme.outline, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ---------------------------------------------------------
            // LEFT COLUMN — Food info
            // ---------------------------------------------------------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        Log.d(TAG, "🖱️ [FoodHistoryCard] -> Card clicked for: $foodName")
                        onClick()
                    }
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
                    Text(date, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    Text("•", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    Text(mealType, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                }
            }

            // ---------------------------------------------------------
            // RIGHT COLUMN — Calories + Delete
            // ---------------------------------------------------------
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

                IconButton(
                    onClick = {
                        Log.d(TAG, "🗑️ [FoodHistoryCard] -> Delete icon tapped for: $foodName")
                        showDeleteDialog = true
                    },
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

    // ---------------------------------------------------------
    // DELETE CONFIRMATION DIALOG
    // ---------------------------------------------------------
    if (showDeleteDialog) {
        Log.d(TAG, "⚠️ [FoodHistoryCard] -> Showing delete confirmation dialog for: $foodName")

        AlertDialog(
            onDismissRequest = {
                Log.d(TAG, "❌ [FoodHistoryCard] -> Delete dialog dismissed for: $foodName")
                showDeleteDialog = false
            },
            title = {
                Text("Delete Food Log?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to delete \"$foodName\" from your log?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d(TAG, "✅ [FoodHistoryCard] -> Delete confirmed for: $foodName")
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    Log.d(TAG, "↩️ [FoodHistoryCard] -> Delete cancelled for: $foodName")
                    showDeleteDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Log.d(TAG, "🏁 [FoodHistoryCard] -> Render complete for: $foodName")
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
    scannedFood: Food?,
    currentCalories: String = "",
    currentCarbs: String = "",
    currentFat: String = "",
    currentProtein: String = ""
) {
    // -------------------------------------------------------------
    // 🧠 STATE & FORMATTING SETUP
    // -------------------------------------------------------------
    var showDatePicker by remember { mutableStateOf(false) }
    var showMeasuringUnitDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val selectedDateString = remember(selectedDate) { dateFormatter.format(selectedDate) }

    Log.d(TAG, "🧩 [AdjustServingScreen] -> Opened with: $foodName | $servingSize $measuringUnit | Type=$mealType")

    // --- Define and filter measuring units ---
    val allMeasuringUnits = mapOf(
        "weight" to listOf("g", "kg"),
        "volume" to listOf("ml", "l"),
        "other" to listOf("serving", "portion", "piece", "slice", "whole", "scoop", "handful", "cup")
    )

    val measuringUnits = when (unitCategory) {
        "weight" -> allMeasuringUnits["weight"]!! + listOf("─────") + allMeasuringUnits["other"]!!
        "volume" -> allMeasuringUnits["volume"]!! + listOf("─────") + allMeasuringUnits["other"]!!
        else -> allMeasuringUnits["weight"]!! + listOf("─────") +
                allMeasuringUnits["volume"]!! + listOf("─────") +
                allMeasuringUnits["other"]!!
    }

    Log.d(TAG, "⚙️ [AdjustServingScreen] -> Unit category: $unitCategory | Available units: $measuringUnits")

    // -------------------------------------------------------------
    // 🧱 LAYOUT STRUCTURE
    // -------------------------------------------------------------
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
            // ---------------------------------------------------------
            // 🔙 HEADER
            // ---------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    Log.d(TAG, "🔙 [AdjustServingScreen] -> Back pressed.")
                    onBackPressed()
                }) {
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

            // ---------------------------------------------------------
            // 🧾 INPUT FIELDS SECTION
            // ---------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                    // -----------------------------
                    // 🍽️ Food Name Input
                    // -----------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(3.dp, colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .background(colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    ) {
                        TextField(
                            value = foodName,
                            onValueChange = {
                                Log.d(TAG, "✏️ [AdjustServingScreen] -> Food name updated: $it")
                                onFoodNameChange(it)
                            },
                            placeholder = { Text("Food name*", color = colorScheme.onSurfaceVariant) },
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = colorScheme.primary
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, color = colorScheme.onBackground)
                        )
                    }

                    // -----------------------------
                    // ⚖️ Serving Size Input
                    // -----------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(3.dp, colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .background(colorScheme.secondary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = servingSize,
                                onValueChange = {
                                    Log.d(TAG, "📏 [AdjustServingScreen] -> Serving size changed: $it")
                                    onServingSizeChange(it)
                                },
                                placeholder = { Text("Serving size*", color = colorScheme.onSurfaceVariant) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = colorScheme.secondary
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, color = colorScheme.onBackground),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )

                            // Unit dropdown button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colorScheme.secondary.copy(alpha = 0.15f))
                                    .clickable {
                                        Log.d(TAG, "📐 [AdjustServingScreen] -> Measuring unit dropdown opened.")
                                        showMeasuringUnitDialog = true
                                    }
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
                                    Text("▼", fontSize = 12.sp, color = colorScheme.secondary)
                                }
                            }
                        }
                    }

                    // -----------------------------
                    // 📅 Date Picker
                    // -----------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(3.dp, colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .background(colorScheme.secondary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .clickable {
                                Log.d(TAG, "📅 [AdjustServingScreen] -> Date picker opened.")
                                showDatePicker = true
                            }
                    ) {
                        Text(
                            text = selectedDateString,
                            fontSize = 18.sp,
                            color = colorScheme.onBackground,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).wrapContentHeight(Alignment.CenterVertically)
                        )
                    }
                }

                // ---------------------------------------------------------
                // 🔍 Nutritional Preview
                // ---------------------------------------------------------
                Spacer(modifier = Modifier.height(16.dp))
                if (currentCalories.isNotBlank()) {
                    Log.d(TAG, "🍎 [AdjustServingScreen] -> Showing nutritional preview: ${currentCalories}kcal | ${currentCarbs}C | ${currentProtein}P | ${currentFat}F")
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.primary.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Nutritional Info", fontSize = 14.sp, color = colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Calories", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                                    Text("$currentCalories kcal", fontSize = 16.sp, color = colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Carbs", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                                    Text("${currentCarbs}g", fontSize = 14.sp, color = colorScheme.onBackground, fontWeight = FontWeight.Medium)
                                }
                                Column {
                                    Text("Protein", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                                    Text("${currentProtein}g", fontSize = 14.sp, color = colorScheme.onBackground, fontWeight = FontWeight.Medium)
                                }
                                Column {
                                    Text("Fat", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                                    Text("${currentFat}g", fontSize = 14.sp, color = colorScheme.onBackground, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }


                // ---------------------------------------------------------
                // ✅ Submit Ingredient Button
                // ---------------------------------------------------------
                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        Log.d(TAG, "✅ [AdjustServingScreen] -> Add Ingredient clicked.")
                        onContinue() // ⬅️ will finalize the MealIngredient in parent AddIngredientActivity
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(colorScheme.primary, colorScheme.secondary)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add Ingredient",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.background
                        )
                    }
                }

            }
        }

        // ---------------------------------------------------------
        // 📅 Date Picker Dialog
        // ---------------------------------------------------------
        if (showDatePicker) {
            Log.d(TAG, "📆 [AdjustServingScreen] -> DatePicker dialog opened.")
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)
            DatePickerDialog(
                onDismissRequest = {
                    showDatePicker = false
                    Log.d(TAG, "❌ [AdjustServingScreen] -> DatePicker dismissed.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val newDate = Date(it)
                            Log.d(TAG, "✅ [AdjustServingScreen] -> Date selected: $newDate")
                            onDateChange(newDate)
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        Log.d(TAG, "↩️ [AdjustServingScreen] -> Date selection canceled.")
                        showDatePicker = false
                    }) { Text("Cancel") }
                }
            ) { DatePicker(state = datePickerState) }
        }

        // ---------------------------------------------------------
        // ⚖️ Measuring Unit Dialog
        // ---------------------------------------------------------
        if (showMeasuringUnitDialog) {
            Log.d(TAG, "📏 [AdjustServingScreen] -> Measuring unit dialog opened.")
            AlertDialog(
                onDismissRequest = {
                    showMeasuringUnitDialog = false
                    Log.d(TAG, "❌ [AdjustServingScreen] -> Measuring unit dialog dismissed.")
                },
                title = { Text("Select Measuring Unit", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                        measuringUnits.forEach { unit ->
                            if (unit == "─────") {
                                Divider(Modifier.padding(vertical = 8.dp), color = colorScheme.outline.copy(alpha = 0.3f))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (unit == measuringUnit) colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable {
                                            Log.d(TAG, "✅ [AdjustServingScreen] -> Measuring unit selected: $unit")
                                            onMeasuringUnitChange(unit)
                                            showMeasuringUnitDialog = false
                                        }
                                        .padding(vertical = 14.dp, horizontal = 16.dp)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = unit,
                                                fontSize = 16.sp,
                                                color = if (unit == measuringUnit) colorScheme.primary else colorScheme.onBackground,
                                                fontWeight = if (unit == measuringUnit) FontWeight.Bold else FontWeight.Normal
                                            )
                                            val hint = when (unit) {
                                                "kg" -> "1 kg = 1000 g"
                                                "g" -> "1000 g = 1 kg"
                                                "l" -> "1 l = 1000 ml"
                                                "ml" -> "1000 ml = 1 l"
                                                else -> null
                                            }
                                            hint?.let {
                                                Text(it, fontSize = 11.sp, color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                            }
                                        }
                                        if (unit == measuringUnit) {
                                            Text("✓", fontSize = 18.sp, color = colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        Log.d(TAG, "🛑 [AdjustServingScreen] -> Closing measuring unit dialog manually.")
                        showMeasuringUnitDialog = false
                    }) { Text("Close", fontSize = 16.sp, fontWeight = FontWeight.Medium) }
                }
            )
        }
    }

    Log.d(TAG, "🏁 [AdjustServingScreen] -> Render complete. Current: ${servingSize}${measuringUnit}, ${currentCalories} kcal")
}



