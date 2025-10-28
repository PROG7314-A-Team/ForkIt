package com.example.forkit.ui.meals

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.R
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.local.AppDatabase
import com.example.forkit.data.local.entities.MealIngredient as DBMealIngredient
import com.example.forkit.data.models.MealIngredient as UIMealIngredient
import com.example.forkit.data.repository.MealLogRepository
import com.example.forkit.ui.shared.FoodSearchScreen
import com.example.forkit.ui.theme.ForkItTheme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "AddMealActivity"

@OptIn(ExperimentalMaterial3Api::class)
class AddMealActivity : ComponentActivity() {
    
    private lateinit var mealLogRepository: MealLogRepository
    private var pendingIngredient by mutableStateOf<UIMealIngredient?>(null)
    
    private val ingredientLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "🔍 [AddMealActivity] -> Launcher result received: resultCode=${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            val ingredientJson = result.data?.getStringExtra("ingredient")
            Log.d(TAG, "🔍 [AddMealActivity] -> Ingredient JSON: $ingredientJson")
            if (ingredientJson != null) {
                try {
                    val newIngredient = Gson().fromJson(ingredientJson, UIMealIngredient::class.java)
                    pendingIngredient = newIngredient
                    Log.d(TAG, "✅ [AddMealActivity] -> Received ingredient: ${newIngredient.foodName} (${newIngredient.calories} kcal)")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ [AddMealActivity] -> Error parsing ingredient: ${e.message}")
                    Log.e(TAG, "❌ [AddMealActivity] -> JSON was: $ingredientJson")
                }
            } else {
                Log.w(TAG, "⚠️ [AddMealActivity] -> No ingredient JSON found in result data")
            }
        } else {
            Log.w(TAG, "⚠️ [AddMealActivity] -> Result not OK: ${result.resultCode}")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize repository
        val database = AppDatabase.getInstance(this)
        mealLogRepository = MealLogRepository(
            apiService = RetrofitClient.api,
            mealLogDao = database.mealLogDao(),
            networkManager = com.example.forkit.utils.NetworkConnectivityManager(this)
        )
        
        setContent {
            ForkItTheme {
                AddMealScreen(
                    userId = "yeY2AwZAiZgEiCn9HByZUP6rsoY2", // TODO: Get from shared preferences or auth
                    onBackPressed = { finish() },
                    onAddIngredient = { 
                        val intent = Intent(this@AddMealActivity, com.example.forkit.ui.meals.AddIngredientActivity::class.java)
                        ingredientLauncher.launch(intent)
                    },
                    onSaveMeal = { name, description, ingredients ->
                        saveMealTemplate(name, description, ingredients)
                    },
                    mealLogRepository = mealLogRepository,
                    pendingIngredient = pendingIngredient,
                    onIngredientAdded = { pendingIngredient = null }
                )
            }
        }
    }
    
    private fun saveMealTemplate(
        name: String,
        description: String,
        ingredients: List<UIMealIngredient>
    ) {
        Log.d(TAG, "🔍 [saveMealTemplate] -> Starting save process. Name: '$name', Description: '$description', Ingredients: ${ingredients.size}")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Convert UI ingredients to DB ingredients
                val dbIngredients = ingredients.map { ingredient ->
                    DBMealIngredient(
                        foodName = ingredient.foodName,
                        quantity = ingredient.servingSize,
                        unit = ingredient.measuringUnit,
                        calories = ingredient.calories,
                        carbs = ingredient.carbs,
                        fat = ingredient.fat,
                        protein = ingredient.protein
                    )
                }
                
                // Calculate totals
                val totalCalories = ingredients.sumOf { it.calories }
                val totalCarbs = ingredients.sumOf { it.carbs }
                val totalFat = ingredients.sumOf { it.fat }
                val totalProtein = ingredients.sumOf { it.protein }
                
                Log.d(TAG, "🔍 [saveMealTemplate] -> Calling repository.createMealLog with isTemplate=true")
                val result = mealLogRepository.createMealLog(
                    userId = "yeY2AwZAiZgEiCn9HByZUP6rsoY2",
                    name = name,
                    description = description,
                    ingredients = dbIngredients,
                    instructions = emptyList(), // No instructions for now
                    totalCalories = totalCalories,
                    totalCarbs = totalCarbs,
                    totalFat = totalFat,
                    totalProtein = totalProtein,
                    servings = 1.0,
                    date = "", // Empty date for templates
                    mealType = null,
                    isTemplate = true, // This is a template
                    templateId = null
                )
                Log.d(TAG, "🔍 [saveMealTemplate] -> Repository result: ${result.isSuccess}")
                
                if (result.isSuccess) {
                    Toast.makeText(this@AddMealActivity, "Meal template saved successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddMealActivity, "Failed to save meal template", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving meal template: ${e.message}", e)
                Toast.makeText(this@AddMealActivity, "Error saving meal template", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    userId: String,
    onBackPressed: () -> Unit,
    onAddIngredient: () -> Unit,
    onSaveMeal: (String, String, List<UIMealIngredient>) -> Unit,
    mealLogRepository: MealLogRepository,
    pendingIngredient: UIMealIngredient?,
    onIngredientAdded: () -> Unit
) {
    val context = LocalContext.current
    var mealName by remember { mutableStateOf("") }
    var mealDescription by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf<List<UIMealIngredient>>(emptyList()) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    // Handle pending ingredient from AddIngredientActivity
    LaunchedEffect(pendingIngredient) {
        Log.d(TAG, "🔍 [AddMealScreen] -> LaunchedEffect triggered, pendingIngredient: $pendingIngredient")
        pendingIngredient?.let { ingredient ->
            Log.d(TAG, "🔍 [AddMealScreen] -> Processing ingredient: ${ingredient.foodName} (${ingredient.calories} kcal)")
            val oldSize = ingredients.size
            ingredients = ingredients + ingredient
            onIngredientAdded()
            Log.d(TAG, "✅ [AddMealScreen] -> Added ingredient: ${ingredient.foodName}. List size: $oldSize -> ${ingredients.size}")
        } ?: run {
            Log.d(TAG, "🔍 [AddMealScreen] -> No pending ingredient to process")
        }
    }
    
    // Calculate nutrition totals
    val totalCalories = ingredients.sumOf { it.calories }
    val totalCarbs = ingredients.sumOf { it.carbs }
    val totalFat = ingredients.sumOf { it.fat }
    val totalProtein = ingredients.sumOf { it.protein }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Create Meal Template",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.primary,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Meal Name Input
            OutlinedTextField(
                value = mealName,
                onValueChange = { mealName = it },
                label = { Text("Meal Name") },
                placeholder = { Text("e.g., Toasted Ham and Cheese") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Meal Description Input
            OutlinedTextField(
                value = mealDescription,
                onValueChange = { mealDescription = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("Brief description of the meal") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Ingredients Section
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Add Ingredient Button
            Button(
                onClick = onAddIngredient,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Ingredient")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ingredients List
            if (ingredients.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No ingredients added yet. Tap 'Add Ingredient' to get started.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                ingredients.forEachIndexed { index, ingredient ->
                    IngredientCard(
                        ingredient = ingredient,
                        onDelete = { 
                            ingredients = ingredients.toMutableList().apply { removeAt(index) }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Nutrition Summary
            if (ingredients.isNotEmpty()) {
                NutritionSummaryCard(
                    calories = totalCalories,
                    carbs = totalCarbs,
                    fat = totalFat,
                    protein = totalProtein
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Save Button
            Button(
                onClick = {
                    Log.d(TAG, "🔍 [AddMealScreen] -> Save button clicked. Name: '$mealName', Description: '$mealDescription', Ingredients: ${ingredients.size}")
                    if (mealName.isBlank()) {
                        Log.w(TAG, "⚠️ [AddMealScreen] -> Meal name is blank")
                        Toast.makeText(context, "Please enter a meal name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (ingredients.isEmpty()) {
                        Log.w(TAG, "⚠️ [AddMealScreen] -> No ingredients to save")
                        Toast.makeText(context, "Please add at least one ingredient", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    Log.d(TAG, "✅ [AddMealScreen] -> Calling onSaveMeal with ${ingredients.size} ingredients")
                    onSaveMeal(mealName, mealDescription, ingredients)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = mealName.isNotBlank() && ingredients.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary
                )
            ) {
                Text(
                    text = "Save Meal Template",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun IngredientCard(
    ingredient: UIMealIngredient,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = ingredient.foodName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "${ingredient.servingSize} ${ingredient.measuringUnit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${ingredient.calories.toInt()} cal",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete ingredient",
                    tint = colorScheme.error
                )
            }
        }
    }
}

@Composable
fun NutritionSummaryCard(
    calories: Double,
    carbs: Double,
    fat: Double,
    protein: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Nutrition Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutritionItem("Calories", "${calories.toInt()}", "cal")
                NutritionItem("Carbs", "${carbs.toInt()}", "g")
                NutritionItem("Fat", "${fat.toInt()}", "g")
                NutritionItem("Protein", "${protein.toInt()}", "g")
            }
        }
    }
}

@Composable
fun NutritionItem(
    label: String,
    value: String,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onPrimaryContainer
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onPrimaryContainer
        )
    }
}
