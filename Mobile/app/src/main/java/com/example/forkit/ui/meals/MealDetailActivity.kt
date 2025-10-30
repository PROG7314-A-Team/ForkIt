@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.forkit.ui.meals

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.forkit.data.local.AppDatabase
import com.example.forkit.data.models.Ingredient
import com.example.forkit.data.repository.MealLogRepository
import com.example.forkit.ui.theme.ForkItTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MealDetailActivity"

class MealDetailActivity : ComponentActivity() {
    
    private lateinit var mealLogRepository: MealLogRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get data from intent
        val mealId = intent.getStringExtra("MEAL_ID") ?: ""
        val mealName = intent.getStringExtra("MEAL_NAME") ?: "Meal"
        val description = intent.getStringExtra("MEAL_DESCRIPTION") ?: "No description available"
        val ingredients = try {
            @Suppress("UNCHECKED_CAST")
            intent.getSerializableExtra("INGREDIENTS") as? Array<Ingredient> ?: emptyArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading ingredients from intent: ${e.message}", e)
            emptyArray()
        }.toList()
        val calories = intent.getDoubleExtra("CALORIES", 0.0)
        val carbs = intent.getDoubleExtra("CARBS", 0.0)
        val fat = intent.getDoubleExtra("FAT", 0.0)
        val protein = intent.getDoubleExtra("PROTEIN", 0.0)
        val servings = intent.getDoubleExtra("SERVINGS", 1.0)
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val isTemplate = intent.getBooleanExtra("IS_TEMPLATE", true)
        
        // Initialize repository
        val database = AppDatabase.getInstance(this)
        mealLogRepository = MealLogRepository(
            apiService = RetrofitClient.api,
            mealLogDao = database.mealLogDao(),
            networkManager = com.example.forkit.utils.NetworkConnectivityManager(this)
        )
        
        setContent {
            ForkItTheme {
                MealDetailScreen(
                    mealId = mealId,
                    mealName = mealName,
                    description = description,
                    ingredients = ingredients,
                    calories = calories,
                    carbs = carbs,
                    fat = fat,
                    protein = protein,
                    servings = servings,
                    userId = userId,
                    isTemplate = isTemplate,
                    onBackPressed = { finish() },
                    onLogToToday = { 
                        val intent = Intent(this@MealDetailActivity, MealAdjustmentActivity::class.java).apply {
                            putExtra("templateId", mealId)
                            putExtra("userId", userId)
                        }
                        startActivity(intent)
                    },
                    onEditMeal = {
                        Toast.makeText(this@MealDetailActivity, "Edit functionality coming soon!", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteMeal = {
                        deleteMealTemplate(mealId)
                    }
                )
            }
        }
    }
    
    private fun deleteMealTemplate(mealId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = mealLogRepository.deleteMealLog(mealId)
                if (result.isSuccess) {
                    Toast.makeText(this@MealDetailActivity, "Meal template deleted successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@MealDetailActivity, "Failed to delete meal template", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting meal template: ${e.message}", e)
                Toast.makeText(this@MealDetailActivity, "Error deleting meal template", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun MealDetailScreen(
    mealId: String,
    mealName: String,
    description: String,
    ingredients: List<Ingredient>,
    calories: Double,
    carbs: Double,
    fat: Double,
    protein: Double,
    servings: Double,
    userId: String,
    isTemplate: Boolean,
    onBackPressed: () -> Unit,
    onLogToToday: () -> Unit,
    onEditMeal: () -> Unit,
    onDeleteMeal: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = mealName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            if (isTemplate) {
                // Action buttons for meal templates
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Log to Today button
                    Button(
                        onClick = onLogToToday,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log to Today")
                    }
                    
                    // Edit button
                    OutlinedButton(
                        onClick = onEditMeal,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    
                    // Delete button
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            // Nutrition summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${calories.toInt()} kcal",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Total Energy",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Nutrition breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${carbs.toInt()}g",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Carbs",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${protein.toInt()}g",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Protein",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${fat.toInt()}g",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Fat",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (servings != 1.0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Per ${servings} serving${if (servings != 1.0) "s" else ""}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description section
            Text(
                text = "Description",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ingredients header
            Text(
                text = "Ingredients",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (ingredients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ingredients listed for this meal.",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(ingredients) { ingredient ->
                        IngredientCard(ingredient = ingredient)
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Meal Template") },
            text = { Text("Are you sure you want to delete this meal template? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteMeal()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
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

@Composable
fun IngredientCard(ingredient: Ingredient) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = ingredient.name,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${ingredient.amount} ${ingredient.unit}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}