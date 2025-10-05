package com.example.forkit.ui.meals

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.MealLog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealsScreen(
    userId: String,
    onMealSelected: (MealLog) -> Unit,
    onAddMealClicked: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var meals by remember { mutableStateOf<List<MealLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Brand colors (matches Dashboard)
    val green = Color(0xFF22B27D)
    val gray = Color(0xFF666666)
    val bgColor = Color(0xFFF8F9FA)

    // Fetch meals on launch
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.api.getMealLogs(userId = userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    meals = response.body()?.data ?: emptyList()
                    Log.d("MealsScreen", "Loaded ${meals.size} meals for user $userId")
                } else {
                    errorMessage = "Failed to load meals (${response.code()})"
                    Log.e("MealsScreen", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                errorMessage = "Error loading meals: ${e.message}"
                Log.e("MealsScreen", "Exception: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMealClicked,
                containerColor = green
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Meal", tint = Color.White)
            }
        },
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "🍽 Meals",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = green,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
            )

            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = green)
                }

                errorMessage != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️ $errorMessage",
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }

                meals.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No meals found.\nTap + to add your first meal.",
                        fontSize = 16.sp,
                        color = gray,
                        textAlign = TextAlign.Center
                    )
                }

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(meals) { meal ->
                        MealCard(meal = meal, onClick = { onMealSelected(meal) })
                    }
                }
            }
        }
    }
}

@Composable
fun MealCard(meal: MealLog, onClick: () -> Unit) {
    val green = Color(0xFF22B27D)
    val gray = Color(0xFF666666)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.5.dp, green, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = meal.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = gray
            )

            if (meal.description.isNotBlank()) {
                Text(
                    text = meal.description,
                    fontSize = 14.sp,
                    color = gray.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ingredients: ${meal.ingredients.size}",
                    fontSize = 14.sp,
                    color = gray.copy(alpha = 0.8f)
                )
                Text(
                    text = "${meal.totalCalories.toInt()} kcal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = green
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = meal.mealType ?: "Unspecified",
                    fontSize = 13.sp,
                    color = gray.copy(alpha = 0.7f)
                )
                Text(
                    text = meal.date,
                    fontSize = 13.sp,
                    color = gray.copy(alpha = 0.7f)
                )
            }
        }
    }
}
