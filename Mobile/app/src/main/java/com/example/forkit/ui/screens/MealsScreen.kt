package com.example.forkit.ui.screens

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.MealLog
import com.example.forkit.ui.meals.AddMealActivity
import com.example.forkit.ui.meals.MealDetailActivity
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import com.example.forkit.R
import com.example.forkit.data.repository.MealLogRepository
import com.example.forkit.data.local.entities.MealLogEntity
import java.text.SimpleDateFormat
import java.util.Locale

private const val DEBUG_TAG = "MealsDebug"

@Composable
fun MealsScreen(userId: String, mealLogRepository: MealLogRepository? = null, refreshTrigger: Int = 0) {
    val TAG = "MealsScreen"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // UI state variables
    var meals by remember { mutableStateOf<List<MealLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Log the start of the screen
    Log.d(DEBUG_TAG, "$TAG: Initializing MealsScreen for userId=$userId")

    // Function to fetch meals
    val fetchMeals: suspend () -> Unit = {
        Log.d("MealsScreen", "Fetching meals from API and local database for userId=$userId")
        isLoading = true
        errorMessage = null
        try {
            // First try to get from API
            val response = RetrofitClient.api.getMealLogs(userId = userId)
            var allMeals = emptyList<MealLog>()

            if (response.isSuccessful && response.body()?.success == true) {
                allMeals = response.body()?.data ?: emptyList()
                Log.d("MealsScreen", "Loaded ${allMeals.size} meals from API")
            } else {
                Log.w("MealsScreen", "API call failed: ${response.message()}")
            }

            // Always try local database as well to get the most complete data
            if (mealLogRepository != null) {
                Log.d("MealsScreen", "Also checking local database for meal templates...")
                // Get only meal templates from local database
                val localTemplates = mealLogRepository.getMealTemplates(userId)
                val localTemplatesConverted = localTemplates.map { entity ->
                    MealLog(
                        id = entity.serverId ?: entity.localId,
                        userId = entity.userId,
                        name = entity.name,
                        description = entity.description,
                        ingredients = entity.ingredients.map { ingredient ->
                            com.example.forkit.data.models.Ingredient(
                                name = ingredient.foodName,
                                amount = ingredient.quantity,
                                unit = ingredient.unit
                            )
                        },
                        instructions = entity.instructions,
                        totalCalories = entity.totalCalories,
                        totalCarbs = entity.totalCarbs,
                        totalFat = entity.totalFat,
                        totalProtein = entity.totalProtein,
                        servings = entity.servings,
                        date = entity.date,
                        mealType = entity.mealType,
                        isTemplate = entity.isTemplate,
                        templateId = entity.templateId,
                        createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(java.util.Date(entity.createdAt)),
                        updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(java.util.Date(entity.createdAt))
                    )
                }
                Log.d("MealsScreen", "Loaded ${localTemplatesConverted.size} meal templates from local database")

                // For templates, we primarily use local data since API might not have isTemplate field yet
                // Filter API data to only include templates (if API supports it)
                val apiTemplates = allMeals.filter { it.isTemplate }
                val combinedTemplates = if (apiTemplates.isNotEmpty()) {
                    // Merge API and local templates, with local data taking precedence
                    val localIds = localTemplatesConverted.map { it.id }.toSet()
                    val apiOnly = apiTemplates.filter { it.id !in localIds }
                    localTemplatesConverted + apiOnly
                } else {
                    localTemplatesConverted
                }

                allMeals = combinedTemplates
                Log.d("MealsScreen", "Combined total: ${allMeals.size} meal templates (API: ${apiTemplates.size}, Local: ${localTemplatesConverted.size})")
            }

            // Remove duplicates by unique name + calories combination (templates don't have dates)
            meals = allMeals.distinctBy { "${it.name}-${it.totalCalories}" }

            Log.d("MealsScreen", "✅ Final meal templates loaded: ${allMeals.size}, unique after filter: ${meals.size}")
            errorMessage = null
        } catch (e: Exception) {
            Log.e("MealsScreen", "❌ Error fetching meals", e)
            errorMessage = e.message
            meals = emptyList()
        } finally {
            isLoading = false
        }
    }

    // Load meals on initial load or when refresh is triggered
    LaunchedEffect(userId, refreshTrigger) {
        fetchMeals()
    }

    // Auto-refresh when returning to this screen (e.g., after saving a template)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, userId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d(DEBUG_TAG, "$TAG: ON_RESUME detected, auto-refreshing meals")
                scope.launch { fetchMeals() }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    // UI Composition
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Header section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.your_meals),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                )
                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {
                    Log.d(DEBUG_TAG, "$TAG: Refresh button clicked.")
                    Toast.makeText(context, context.getString(R.string.refreshing_meals), Toast.LENGTH_SHORT).show()
                    refreshTrigger++ // Trigger refresh
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Conditional UI states
            when {
                isLoading -> {
                    Log.d(DEBUG_TAG, "$TAG: Showing loading indicator...")
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                errorMessage != null -> {
                    Log.w(DEBUG_TAG, "$TAG: Displaying error message: $errorMessage")
                    Text(
                        text = stringResource(R.string.error_loading_meals, errorMessage ?: ""),
                        color = Color.Red,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                meals.isEmpty() -> {
                    Log.d(DEBUG_TAG, "$TAG: No meal templates found for user.")
                    Text(
                        text = "No meal templates created yet. Create your first meal template!",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }

                else -> {
                    Log.d(DEBUG_TAG, "$TAG: Rendering meals list...")
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(meals) { meal ->
                            MealCard(meal = meal) {
                                Log.i(DEBUG_TAG, "$TAG: Clicked meal -> ${meal.name}")

                                // Pass full ingredient objects to MealDetailActivity
                                val intent = Intent(context, MealDetailActivity::class.java).apply {
                                    putExtra("MEAL_ID", meal.id)
                                    putExtra("MEAL_NAME", meal.name)
                                    putExtra("MEAL_DESCRIPTION", meal.description ?: "No description available")
                                    putExtra("INGREDIENTS", meal.ingredients.toTypedArray())
                                    putExtra("CALORIES", meal.totalCalories)
                                    putExtra("CARBS", meal.totalCarbs)
                                    putExtra("FAT", meal.totalFat)
                                    putExtra("PROTEIN", meal.totalProtein)
                                    putExtra("SERVINGS", meal.servings)
                                    putExtra("USER_ID", userId)
                                    putExtra("IS_TEMPLATE", meal.isTemplate)
                                }


                                context.startActivity(intent)
                            }

                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }

        // Floating Add Meal Template Button
        FloatingActionButton(
            onClick = {
                Log.d(DEBUG_TAG, "$TAG: Add Meal Template button clicked. Opening AddMealActivity.")
                val intent = Intent(context, AddMealActivity::class.java).apply {
                    putExtra("USER_ID", userId)
                }
                context.startActivity(intent)
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Meal Template")
        }
    }
}

// Data Model for Meal
data class Meal(
    val name: String,
    val ingredients: List<String>,
    val calories: Double
)

// MealCard Composable
// Displays each meal item in a card layout
@Composable
fun MealCard(meal: MealLog, onClick: () -> Unit) {
    val TAG = "MealCard"
    Log.v(DEBUG_TAG, "$TAG: Rendering card for ${meal.name}")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Log.d(DEBUG_TAG, "$TAG: Card clicked for ${meal.name}")
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {

                // Meal name
                Text(
                    text = meal.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Safely render ingredient preview
                val ingredientNames = meal.ingredients.map { it.name }
                val ingredientText = ingredientNames.joinToString(", ")
                val abbreviatedText = if (ingredientText.length > 40)
                    ingredientText.take(40) + "…"
                else
                    ingredientText

                Text(
                    text = "${meal.ingredients.size} ingredients: $abbreviatedText",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Calories display (from totalCalories field)
            Text(
                text = "${meal.totalCalories.toInt()} kcal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

