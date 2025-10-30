package com.example.forkit.ui.meals

import android.app.Activity
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import com.example.forkit.R
import com.example.forkit.data.models.*
import com.example.forkit.ui.theme.ForkItTheme
import com.example.forkit.ui.shared.FoodSearchScreen
import com.example.forkit.data.local.AppDatabase
import com.example.forkit.data.repository.FoodLogRepository
import com.example.forkit.utils.NetworkConnectivityManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.*
import com.example.forkit.data.models.MealIngredient


private const val TAG= "MealsDebug"
class AddIngredientActivity : ComponentActivity() {

    private var scannedFoodState by mutableStateOf<Food?>(null)

    // onCreate: initialize UI and callbacks for returning selected ingredient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Enable modern layout rendering (status & nav bar handling)
        enableEdgeToEdge()
        Log.d(TAG, "🟢 [onCreate] -> AddIngredientActivity started successfully. Edge-to-edge UI enabled.")

        // 🧩 Setup the main Compose content for this activity
        setContent {
            Log.d(TAG, "🎨 [onCreate] -> Setting Compose content for AddIngredientScreen...")

            ForkItTheme {
                val passedUserId = intent.getStringExtra("USER_ID") ?: ""
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
                            putExtra("ingredient", Gson().toJson(ingredient))
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

    // barcodeLauncher: handle result from BarcodeScannerActivity and fetch by barcode
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


    // handleScannedBarcode: fetch ingredient by barcode and update screen state
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
                    Toast.makeText(this, getString(com.example.forkit.R.string.found_food, ingredientData.name), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "📢 [handleScannedBarcode] -> Toast displayed for ingredient: ${ingredientData.name}")

                } else {
                    // ❌ Case: No ingredient data returned
                    Log.w(TAG, "⚠️ [handleScannedBarcode] -> API returned success=true but data=null.")
                    Toast.makeText(this, getString(com.example.forkit.R.string.no_ingredient_found_barcode), Toast.LENGTH_SHORT).show()
                }

            } else {
                // ❌ Case: API call failed or returned error status
                val errorMsg = response.body()?.message ?: "Unknown API error"
                Log.e(TAG, "❌ [handleScannedBarcode] -> Failed API response. Reason: $errorMsg")
                Toast.makeText(this, getString(com.example.forkit.R.string.failed_fetch_ingredient, errorMsg), Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            // 💥 Catch-all for any exceptions (network, parsing, etc.)
            Log.e(TAG, "🔥 [handleScannedBarcode] -> Exception occurred while fetching ingredient.", e)
            Toast.makeText(this, getString(com.example.forkit.R.string.error_message, e.message ?: ""), Toast.LENGTH_SHORT).show()
        }

        Log.d(TAG, "🏁 [handleScannedBarcode] -> Completed execution for barcode: $barcode")
    }

    // startBarcodeScanner: launch the barcode scanner activity
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
    val context = LocalContext.current
    
    // Initialize repositories for food history
    val database = remember { AppDatabase.getInstance(context) }
    val networkManager = remember { NetworkConnectivityManager(context) }
    val repository = remember {
        FoodLogRepository(
            apiService = RetrofitClient.api,
            foodLogDao = database.foodLogDao(),
            networkManager = networkManager
        )
    }
    val isOnline = remember { networkManager.isOnline() }
    
    // State variables
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

    // React to scanned ingredient input
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

    // Recalculate nutrition when serving size changes
    LaunchedEffect(servingSize, measuringUnit) {
        if (servingSize.isNotBlank() && baseCaloriesPer100g > 0) {
            val newServingSize = servingSize.toDoubleOrNull() ?: 100.0
            val scaleFactor = newServingSize / baseServingQuantity
            
            calories = (baseCaloriesPer100g * scaleFactor).toInt().toString()
            carbs = String.format("%.1f", baseCarbsPer100g * scaleFactor)
            fat = String.format("%.1f", baseFatPer100g * scaleFactor)
            protein = String.format("%.1f", baseProteinPer100g * scaleFactor)
            
            Log.d(TAG, "🧮 [AddIngredientScreen] -> Recalculated nutrition: ${servingSize}${measuringUnit} -> ${calories}kcal")
        }
    }

    // React to search selection
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

            // --- Show success message ---
            Toast.makeText(context, context.getString(R.string.food_selected_success, food.name), Toast.LENGTH_SHORT).show()

            // --- Reset selection ---
            selectedSearchFood = null
            Log.d(TAG, "🧹 [AddIngredientScreen] -> Cleared selectedSearchFood state.")
        }
    }

    // Screen flow controller: controls which sub-screen is visible during ingredient creation
    when (currentScreen) {

        // Main screen: ingredient search, history, and scan entry
        "main" -> {
            Log.d(TAG, "🟢 [AddIngredientScreen] -> Displaying MAIN screen with shared FoodSearchScreen.")
            FoodSearchScreen(
                userId = passedUserId,
                screenTitle = stringResource(R.string.add_ingredient),
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
                onSearchFoodSelected = { selectedFood ->
                    Log.d(TAG, "🧾 [MAIN] -> Search result tapped: ${selectedFood.name}")
                    selectedSearchFood = selectedFood
                },
                repository = repository,
                isOnline = isOnline,
                refreshTrigger = 0
            )
        }

        // Adjust screen: portion size and units
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
                    Log.d(TAG, "➡️ [AdjustServingScreen] -> Continue clicked. Navigating to details for calories/macros entry.")
                    currentScreen = "details"
                },
                scannedFood = scannedFood,
                currentCalories = calories,
                currentCarbs = carbs,
                currentFat = fat,
                currentProtein = protein
            )
        }

        // Details screen: enter calories and macros
        "details" -> {
            IngredientDetailsScreen(
                calories = calories,
                carbs = carbs,
                fat = fat,
                protein = protein,
                onCaloriesChange = { calories = it },
                onCarbsChange = { carbs = it },
                onFatChange = { fat = it },
                onProteinChange = { protein = it },
                onBackPressed = { currentScreen = "adjust" },
                onConfirm = {
                    Log.d(TAG, "✅ [IngredientDetailsScreen] -> Finalizing ingredient creation...")
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
                    Log.d(TAG, "📦 [IngredientDetailsScreen] -> Ingredient built: ${ingredient.foodName} | ${ingredient.calories}kcal | ${ingredient.carbs}C | ${ingredient.fat}F | ${ingredient.protein}P")
                    onIngredientReady(ingredient)
                }
            )
        }


    }

    Log.d(TAG, "🧩 [AddIngredientScreen] -> Current active screen: $currentScreen")
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
                    text = stringResource(R.string.adjust_serving),
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
                            placeholder = { Text(stringResource(R.string.food_name_required), color = colorScheme.onSurfaceVariant) },
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
                                placeholder = { Text(stringResource(R.string.serving_size_required), color = colorScheme.onSurfaceVariant) },
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
                            Text(stringResource(R.string.nutritional_info), fontSize = 14.sp, color = colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(stringResource(R.string.calories), fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                                    Text("$currentCalories ${stringResource(R.string.kcal)}", fontSize = 16.sp, color = colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(stringResource(R.string.carbs), fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                                    Text("${currentCarbs}${stringResource(R.string.grams)}", fontSize = 14.sp, color = colorScheme.onBackground, fontWeight = FontWeight.Medium)
                                }
                                Column {
                                    Text(stringResource(R.string.protein), fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                                    Text("${currentProtein}${stringResource(R.string.grams)}", fontSize = 14.sp, color = colorScheme.onBackground, fontWeight = FontWeight.Medium)
                                }
                                Column {
                                    Text(stringResource(R.string.fat), fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                                    Text("${currentFat}${stringResource(R.string.grams)}", fontSize = 14.sp, color = colorScheme.onBackground, fontWeight = FontWeight.Medium)
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
                            text = stringResource(R.string.add_ingredient),
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
                                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = colorScheme.outline.copy(alpha = 0.3f))
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




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDetailsScreen(
    calories: String,
    carbs: String,
    fat: String,
    protein: String,
    onCaloriesChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onBackPressed: () -> Unit,
    onConfirm: () -> Unit
) {
    // Auto-calculate calories when macros change (4/4/9 rule)
    LaunchedEffect(carbs, fat, protein) {
        val carbsValue = carbs.toDoubleOrNull() ?: 0.0
        val fatValue = fat.toDoubleOrNull() ?: 0.0
        val proteinValue = protein.toDoubleOrNull() ?: 0.0
        val calculatedCalories = (carbsValue * 4) + (fatValue * 9) + (proteinValue * 4)
        if (calculatedCalories > 0) {
            onCaloriesChange(calculatedCalories.toInt().toString())
        } else if (carbs.isEmpty() && fat.isEmpty() && protein.isEmpty()) {
            onCaloriesChange("")
        }
    }

    val isValid = (calories.toDoubleOrNull() ?: 0.0) > 0.0

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
                    text = stringResource(R.string.add_details),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Fields stack
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                    // Calories field (styled like Add Food)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(3.dp, colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .background(colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
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
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, color = colorScheme.onBackground),
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

                    // Hint
                    Text(
                        text = stringResource(R.string.forkit_can_calculate),
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Carbs
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
                                .border(3.dp, colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .background(colorScheme.secondary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
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
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, color = colorScheme.onBackground),
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

                    // Fat
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
                                .border(3.dp, colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .background(colorScheme.secondary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = fat,
                                    onValueChange = onFatChange,
                                    placeholder = { Text(stringResource(R.string.enter_amount), color = colorScheme.onSurfaceVariant) },
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
                                Text(
                                    text = "g",
                                    fontSize = 18.sp,
                                    color = colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Protein
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
                                .border(3.dp, colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .background(colorScheme.secondary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = protein,
                                    onValueChange = onProteinChange,
                                    placeholder = { Text(stringResource(R.string.enter_amount), color = colorScheme.onSurfaceVariant) },
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

                Button(
                    onClick = onConfirm,
                    enabled = isValid,
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
                                brush = Brush.horizontalGradient(listOf(colorScheme.primary, colorScheme.secondary)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.confirm),
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

