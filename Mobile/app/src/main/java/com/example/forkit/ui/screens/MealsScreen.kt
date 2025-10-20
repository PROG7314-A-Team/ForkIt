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
import com.example.forkit.ui.meals.AddFullMealActivity
import com.example.forkit.ui.meals.MealDetailActivity
import androidx.compose.ui.res.stringResource
import com.example.forkit.R

private const val DEBUG_TAG = "MealsDebug"

@Composable
fun MealsScreen(userId: String) {
    val TAG = "MealsScreen"
    val context = LocalContext.current

    // UI state variables
    var meals by remember { mutableStateOf<List<MealLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Log the start of the screen
    Log.d(DEBUG_TAG, "$TAG: Initializing MealsScreen for userId=$userId")

    // Load meals
    LaunchedEffect(userId) {
        Log.d("MealsScreen", "Fetching real meals from API for userId=$userId")
        try {
            val response = RetrofitClient.api.getMealLogs(userId = userId)
            if (response.isSuccessful && response.body()?.success == true) {
                val allMeals = response.body()?.data ?: emptyList()

                // Remove duplicates by unique name + calories + date combination
                meals = allMeals.distinctBy { "${it.name}-${it.totalCalories}-${it.date}" }

                Log.d("MealsScreen", "✅ Meals loaded: ${allMeals.size}, unique after filter: ${meals.size}")
            }
            else {
                Log.w("MealsScreen", "⚠️ Failed to fetch meals: ${response.message()}")
                meals = emptyList()
            }
        } catch (e: Exception) {
            Log.e("MealsScreen", "❌ Error fetching meals", e)
            meals = emptyList()
        } finally {
            isLoading = false
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
                    Log.d(DEBUG_TAG, "$TAG: No meals found for user.")
                    Text(
                        text = stringResource(R.string.no_meals_logged_yet),
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

                                // Convert Ingredient objects to names
                                val ingredientNames = ArrayList(meal.ingredients.map { it.name })

                                val intent = Intent(context, MealDetailActivity::class.java).apply {
                                    putExtra("MEAL_NAME", meal.name)
                                    putExtra("MEAL_DESCRIPTION", meal.description ?: "No description available")
                                    putStringArrayListExtra("INGREDIENTS", ingredientNames)
                                    putExtra("CALORIES", meal.totalCalories)
                                    putExtra("USER_ID", userId)
                                }


                                context.startActivity(intent)
                            }

                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }

        // Floating Add Meal Button
        FloatingActionButton(
            onClick = {
                Log.d(DEBUG_TAG, "$TAG: Add Meal button clicked. Opening AddFullMealActivity.")
                val intent = Intent(context, AddFullMealActivity::class.java).apply {
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
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_meal))
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

