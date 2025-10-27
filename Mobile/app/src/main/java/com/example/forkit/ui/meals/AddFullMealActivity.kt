package com.example.forkit.ui.meals

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.forkit.ui.theme.ForkItTheme
import com.example.forkit.data.RetrofitClient


import android.util.Log
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import com.example.forkit.data.models.MealIngredient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.forkit.data.models.CreateMealLogRequest
import com.example.forkit.data.models.Ingredient
import kotlinx.coroutines.launch
import com.example.forkit.data.repository.MealLogRepository
import com.example.forkit.data.local.AppDatabase
import com.example.forkit.utils.NetworkConnectivityManager
import com.example.forkit.data.local.entities.MealIngredient as LocalMealIngredient

private const val DEBUG_TAG = "MealsDebug"
private const val TAG = "MealsDebug"
// ✅ Shared list reference so launcher can access composable state
private var ingredientsListState: MutableList<MealIngredient>? = null

class AddFullMealActivity : ComponentActivity() {

    // ✅ ActivityResultLauncher must be declared inside the class, not outside
// -------------------------------------------------------------
// 🧩 Ingredient Launcher
// 📍 Purpose: Receives the MealIngredient object returned from
// AddIngredientActivity and appends it to the ingredients list.
// -------------------------------------------------------------
    private val ingredientLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val ingredientJson = data?.getStringExtra("NEW_INGREDIENT")

            if (ingredientJson != null) {
                Log.d(DEBUG_TAG, "✅ Ingredient JSON received -> $ingredientJson")

                try {
                    // Deserialize the JSON into a MealIngredient object
                    val newIngredient =
                        Gson().fromJson(ingredientJson, MealIngredient::class.java)

                    Log.d(
                        DEBUG_TAG,
                        "🍴 Parsed MealIngredient: ${newIngredient.foodName} | ${newIngredient.calories} kcal"
                    )

                    // Add the ingredient to the list in AddFullMealScreen via a shared state
                    ingredientsListState?.add(newIngredient)

                    Toast.makeText(
                        this,
                        "Added ${newIngredient.foodName} (${newIngredient.calories} kcal)",
                        Toast.LENGTH_SHORT
                    ).show()

                } catch (e: Exception) {
                    Log.e(DEBUG_TAG, "🔥 Failed to parse MealIngredient JSON: ${e.message}", e)
                    Toast.makeText(this, "Error adding ingredient", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.w(DEBUG_TAG, "⚠️ No ingredient data returned from AddIngredientActivity")
            }
        } else {
            Log.d(DEBUG_TAG, "ℹ️ AddIngredientActivity was cancelled")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(DEBUG_TAG, "AddFullMealActivity: ✅ onCreate called — AddFullMealActivity launched.")

        // ✅ Get userId from Intent (passed from Dashboard or previous screen)
        val userId = intent.getStringExtra("USER_ID") ?: ""

        setContent {
            ForkItTheme {
                AddFullMealScreen(
                    userId = userId, // 👈 pass userId directly to composable
                    onBackPressed = { finish() }, // ✅ handle back navigation
                    onAddIngredientClick = {
                        Log.d(DEBUG_TAG, "🍴 Launching AddIngredientActivity...")
                        val intent = Intent(this, AddIngredientActivity::class.java)
                        ingredientLauncher.launch(intent)
                    }
                )
            }
        }
    }
}


    @OptIn(ExperimentalMaterial3Api::class)
@Composable
    fun AddFullMealScreen(
        userId: String,                      // 👈 add this line
        onBackPressed: () -> Unit,
        onAddIngredientClick: () -> Unit
    ) {
    val context = LocalContext.current
    // 🔹 Ingredient list (temporary mock list)
    // Shared mutable list accessible from ingredientLauncher
    val ingredients = remember { mutableStateListOf<MealIngredient>() }
    ingredientsListState = ingredients
    val scope = rememberCoroutineScope()
    
    // Initialize repository for offline support
    val database = remember { AppDatabase.getInstance(context) }
    val networkManager = remember { NetworkConnectivityManager(context) }
    val repository = remember {
        MealLogRepository(
            apiService = RetrofitClient.api,
            mealLogDao = database.mealLogDao(),
            networkManager = networkManager
        )
    }
    val isOnline = remember { networkManager.isOnline() }



    // 🔹 Local states for editable sections
    var mealName by remember { mutableStateOf("") }
    var isEditingName by remember { mutableStateOf(false) }

    var mealDescription by remember { mutableStateOf("") }
    var isEditingDescription by remember { mutableStateOf(false) }


    // 🔹 Footer checkbox toggle
    var logToToday by remember { mutableStateOf(false) }

    Log.d(DEBUG_TAG, "$TAG: Initialized composable with empty meal data")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Meal",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d(DEBUG_TAG, "$TAG: Back button clicked.")
                        onBackPressed()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // 🔹 Main Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Helper text
                Text(
                    text = "Create a meal by adding multiple foods. The meal will be saved as a collection of ingredients.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Meal name section
                Text(
                    text = "Meal Name",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isEditingName) {
                    OutlinedTextField(
                        value = mealName,
                        onValueChange = { mealName = it },
                        placeholder = { Text("Enter meal name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                isEditingName = false
                                Log.d(DEBUG_TAG, "$TAG: Meal name confirmed = $mealName")
                                Toast.makeText(context, "Meal name set!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Confirm", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isEditingName = true
                                Log.d(DEBUG_TAG, "$TAG: Editing meal name triggered.")
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (mealName.isBlank()) "Add Meal Name" else mealName,
                            fontSize = 18.sp,
                            color = if (mealName.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (mealName.isBlank()) FontWeight.Normal else FontWeight.Medium
                        )
                        Icon(Icons.Default.Edit, contentDescription = "Edit name", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description section
                Text(
                    text = "Description",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isEditingDescription) {
                    OutlinedTextField(
                        value = mealDescription,
                        onValueChange = { mealDescription = it },
                        placeholder = { Text("Write a short description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                isEditingDescription = false
                                Log.d(DEBUG_TAG, "$TAG: Description confirmed = $mealDescription")
                                Toast.makeText(context, "Description set!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Confirm", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isEditingDescription = true
                                Log.d(DEBUG_TAG, "$TAG: Editing description triggered.")
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (mealDescription.isBlank()) "Add Description" else mealDescription,
                            fontSize = 16.sp,
                            color = if (mealDescription.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (mealDescription.isBlank()) FontWeight.Normal else FontWeight.Medium
                        )
                        Icon(Icons.Default.Edit, contentDescription = "Edit description", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Ingredients section
                Text(
                    text = "Ingredients",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(visible = ingredients.isEmpty()) {
                    Text(
                        text = "No ingredients added yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ingredients) { ingredient ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Text(
                                    text = "${ingredient.foodName} - ${ingredient.calories.toInt()} kcal",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(onClick = {
                                    ingredients.remove(ingredient)
                                    Log.d(DEBUG_TAG, "$TAG: Ingredient removed -> ${ingredient.foodName}")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Ingredient button
                Button(
                    onClick = {
                        Log.d(DEBUG_TAG, "AddFullMealScreen: 🟢 Add Food button clicked")
                        onAddIngredientClick() // <- trigger the launcher in AddFullMealActivity
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            ),
                            shape = MaterialTheme.shapes.medium
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues()
                ) {
                    Text(
                        text = "Add Ingredient",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp
                    )
                }

            }

            // 🔹 Footer section (conditionally visible)
            AnimatedVisibility(
                visible = ingredients.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = logToToday,
                                onCheckedChange = {
                                    logToToday = it
                                    Log.d(DEBUG_TAG, "$TAG: Checkbox toggled = $logToToday")
                                },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "Log these foods to today's calories?",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                Log.d(DEBUG_TAG, "$TAG: Add Meal button clicked | logToToday=$logToToday | totalIngredients=${ingredients.size}")

                                if (ingredients.isEmpty()) {
                                    Toast.makeText(context, "Please add at least one ingredient.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                                if (logToToday) {
                                    scope.launch {
                                        try {
                                            Log.d(DEBUG_TAG, "MealsDebug: 📡 Creating meal via MealLogRepository...")
                                            
                                            // Calculate total nutrition
                                            val totalCalories = ingredients.sumOf { it.calories }
                                            val totalCarbs = ingredients.sumOf { it.carbs }
                                            val totalFat = ingredients.sumOf { it.fat }
                                            val totalProtein = ingredients.sumOf { it.protein }
                                            
                                            // Convert ingredients to MealIngredient format
                                            val mealIngredients = ingredients.map { ingredient ->
                                                com.example.forkit.data.local.entities.MealIngredient(
                                                    foodName = ingredient.foodName,
                                                    quantity = ingredient.servingSize,
                                                    unit = ingredient.measuringUnit,
                                                    calories = ingredient.calories,
                                                    carbs = ingredient.carbs,
                                                    fat = ingredient.fat,
                                                    protein = ingredient.protein
                                                )
                                            }
                                            
                                            // Create the meal log
                                            val result = repository.createMealLog(
                                                userId = userId,
                                                name = mealName,
                                                description = mealDescription,
                                                ingredients = mealIngredients,
                                                instructions = emptyList(), // No instructions for now
                                                totalCalories = totalCalories,
                                                totalCarbs = totalCarbs,
                                                totalFat = totalFat,
                                                totalProtein = totalProtein,
                                                servings = 1.0,
                                                date = today,
                                                mealType = "Meal"
                                            )
                                            
                                            result.onSuccess { id ->
                                                Log.d(DEBUG_TAG, "MealsDebug: ✅ Meal created successfully: $mealName - $id")
                                                if (!isOnline) {
                                                    Toast.makeText(context, "📱 Meal saved offline - will sync when connected!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "✅ Meal '$mealName' created successfully!", Toast.LENGTH_SHORT).show()
                                                }
                                                onBackPressed() // Go back to main screen
                                            }.onFailure { e ->
                                                Log.e(DEBUG_TAG, "MealsDebug: ❌ Failed to create meal: ${e.message}", e)
                                                Toast.makeText(context, "❌ Failed to create meal. Please try again.", Toast.LENGTH_SHORT).show()
                                            }
                                            
                                        } catch (e: Exception) {
                                            Log.e(DEBUG_TAG, "MealsDebug: 🚨 Error creating meal: ${e.message}", e)
                                            Toast.makeText(context, "❌ Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                } else {
                                    Log.d(DEBUG_TAG, "$TAG: 💾 Skipping meal creation — user unchecked 'Log to Today'.")
                                }

                                Toast.makeText(context, "Meal created successfully!", Toast.LENGTH_SHORT).show()
                                mealName = ""
                                mealDescription = ""
                                ingredients.clear()
                                logToToday = false
                            }
                            ,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "Add Meal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
