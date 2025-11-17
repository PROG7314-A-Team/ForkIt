package com.example.forkit.ui.meals

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.R
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.local.AppDatabase
import com.example.forkit.data.local.entities.MealLogEntity
import com.example.forkit.data.repository.MealLogRepository
import com.example.forkit.ui.theme.ForkItTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MealAdjustmentActivity"

@OptIn(ExperimentalMaterial3Api::class)
class MealAdjustmentActivity : AppCompatActivity() {
    
    private lateinit var mealLogRepository: MealLogRepository
    private var mealTemplate: MealLogEntity? = null
    private var mealTemplateState by mutableStateOf<MealLogEntity?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Get meal template from intent
        val templateId = intent.getStringExtra("templateId")
        val userId = intent.getStringExtra("userId")
        if (templateId == null || userId == null) {
            Toast.makeText(this, "No meal template or user ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Initialize repository
        val database = AppDatabase.getInstance(this)
        mealLogRepository = MealLogRepository(
            apiService = RetrofitClient.api,
            mealLogDao = database.mealLogDao(),
            networkManager = com.example.forkit.utils.NetworkConnectivityManager(this)
        )
        
        // Load meal template with robust ID matching (localId or serverId) and detailed logging
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "🔎 Loading meal template. intentId=$templateId userId=$userId")
                val allLogs = mealLogRepository.getAllMealLogs(userId)
                Log.d(TAG, "📚 Retrieved ${allLogs.size} meal logs from local DB")

                val matched = allLogs.firstOrNull { it.localId == templateId || it.serverId == templateId }
                if (matched == null) {
                    Log.w(TAG, "⚠️ No local match by localId/serverId. Falling back to templates list.")
                    val templates = mealLogRepository.getMealTemplates(userId)
                    Log.d(TAG, "📚 Retrieved ${templates.size} templates from local DB")
                    val fallback = templates.firstOrNull { it.localId == templateId || it.serverId == templateId }
                    if (fallback == null || !fallback.isTemplate) {
                        Log.e(TAG, "❌ Meal template not found for id=$templateId")
                        Toast.makeText(this@MealAdjustmentActivity, "Meal template not found", Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }
                    mealTemplate = fallback
                    mealTemplateState = fallback
                } else {
                    if (!matched.isTemplate) {
                        Log.e(TAG, "❌ Matched entry is not a template. id=$templateId")
                        Toast.makeText(this@MealAdjustmentActivity, "Selected item is not a template", Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }
                    mealTemplate = matched
                    mealTemplateState = matched
                }
                Log.d(TAG, "✅ Meal template loaded: name='${mealTemplateState?.name}' localId=${mealTemplateState?.localId} serverId=${mealTemplateState?.serverId}")
            } catch (e: Exception) {
                Log.e(TAG, "🔥 Error loading meal template: ${e.message}", e)
                Toast.makeText(this@MealAdjustmentActivity, "Error loading meal template", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        
        setContent {
            ForkItTheme {
                mealTemplateState?.let { template ->
                    MealAdjustmentScreen(
                        mealTemplate = template,
                        onBackPressed = { finish() },
                        onLogMeal = { servingMultiplier ->
                            logMealToToday(template.localId, servingMultiplier)
                        }
                    )
                } ?: run {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
    
    private fun logMealToToday(templateId: String, servingMultiplier: Double) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val userId = intent.getStringExtra("userId") ?: return@launch
                Log.d(TAG, "📝 Logging template to today. templateLocalId=$templateId userId=$userId date=$today x$servingMultiplier")
                val result = mealLogRepository.logMealTemplate(
                    templateId = templateId,
                    date = today,
                    userId = userId,
                    servingMultiplier = servingMultiplier
                )
                
                result.onSuccess { id ->
                    Log.d(TAG, "✅ Meal logged result: id=$id")
                    if (id == "offline") {
                        Toast.makeText(this@MealAdjustmentActivity, "Meal saved offline - will sync when connected", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MealAdjustmentActivity, "Meal logged successfully!", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }.onFailure { e ->
                    Log.e(TAG, "❌ Failed to log meal: ${e.message}", e)
                    Toast.makeText(this@MealAdjustmentActivity, "Failed to log meal", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "🔥 Exception when logging meal: ${e.message}", e)
                Toast.makeText(this@MealAdjustmentActivity, "Error logging meal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealAdjustmentScreen(
    mealTemplate: MealLogEntity,
    onBackPressed: () -> Unit,
    onLogMeal: (Double) -> Unit
) {
    var servingMultiplier by remember { mutableStateOf(1.0) }
    
    // Calculate adjusted values
    val adjustedCalories = mealTemplate.totalCalories * servingMultiplier
    val adjustedCarbs = mealTemplate.totalCarbs * servingMultiplier
    val adjustedFat = mealTemplate.totalFat * servingMultiplier
    val adjustedProtein = mealTemplate.totalProtein * servingMultiplier
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Log Meal",
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
            // Meal Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = mealTemplate.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    
                    if (mealTemplate.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = mealTemplate.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Serving Adjustment
            Text(
                text = "Serving Size",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How many servings?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = servingMultiplier.toString(),
                        onValueChange = { 
                            val newValue = it.toDoubleOrNull() ?: 1.0
                            servingMultiplier = newValue.coerceIn(0.1, 10.0)
                        },
                        label = { Text("Servings") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Original: ${mealTemplate.servings} serving${if (mealTemplate.servings != 1.0) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Ingredients List
            Text(
                text = "Ingredients (Adjusted)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            mealTemplate.ingredients.forEach { ingredient ->
                AdjustedIngredientCard(
                    ingredient = ingredient,
                    multiplier = servingMultiplier
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Adjusted Nutrition Summary
            AdjustedNutritionSummaryCard(
                originalCalories = mealTemplate.totalCalories,
                originalCarbs = mealTemplate.totalCarbs,
                originalFat = mealTemplate.totalFat,
                originalProtein = mealTemplate.totalProtein,
                adjustedCalories = adjustedCalories,
                adjustedCarbs = adjustedCarbs,
                adjustedFat = adjustedFat,
                adjustedProtein = adjustedProtein,
                multiplier = servingMultiplier
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Log Meal Button
            Button(
                onClick = { onLogMeal(servingMultiplier) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary
                )
            ) {
                Text(
                    text = "Log Meal to Today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AdjustedIngredientCard(
    ingredient: com.example.forkit.data.local.entities.MealIngredient,
    multiplier: Double
) {
    val adjustedQuantity = ingredient.quantity * multiplier
    val adjustedCalories = ingredient.calories * multiplier
    
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
                    text = "${String.format("%.1f", adjustedQuantity)} ${ingredient.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${adjustedCalories.toInt()} cal",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (multiplier != 1.0) {
                Text(
                    text = "×${String.format("%.1f", multiplier)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AdjustedNutritionSummaryCard(
    originalCalories: Double,
    originalCarbs: Double,
    originalFat: Double,
    originalProtein: Double,
    adjustedCalories: Double,
    adjustedCarbs: Double,
    adjustedFat: Double,
    adjustedProtein: Double,
    multiplier: Double
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
            
            if (multiplier != 1.0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Adjusted for ${String.format("%.1f", multiplier)} serving${if (multiplier != 1.0) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AdjustedNutritionItem("Calories", originalCalories, adjustedCalories, "cal")
                AdjustedNutritionItem("Carbs", originalCarbs, adjustedCarbs, "g")
                AdjustedNutritionItem("Fat", originalFat, adjustedFat, "g")
                AdjustedNutritionItem("Protein", originalProtein, adjustedProtein, "g")
            }
        }
    }
}

@Composable
fun AdjustedNutritionItem(
    label: String,
    originalValue: Double,
    adjustedValue: Double,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${adjustedValue.toInt()}",
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
        
        if (adjustedValue != originalValue) {
            Text(
                text = "(${originalValue.toInt()})",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
