package com.example.forkit.ui.shared

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.R
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.*
import com.example.forkit.data.repository.FoodLogRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "FoodSearchScreen"

/**
 * Shared food search screen used by both AddMealActivity and AddIngredientActivity
 * Provides search, food history, and barcode scanning functionality
 */
@Composable
fun FoodSearchScreen(
    userId: String,
    screenTitle: String, // "Add Food" or "Add Ingredient"
    onBackPressed: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToAdjustServing: (RecentActivityEntry?) -> Unit,
    onBarcodeScan: () -> Unit,
    onSearchFoodSelected: (SearchFoodItem) -> Unit,
    repository: FoodLogRepository? = null, // Optional for ingredient mode
    isOnline: Boolean = true,
    refreshTrigger: Int = 0
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

    // performSearchWithRetry: retry food search on transient failures
    suspend fun performSearchWithRetry(query: String, maxRetries: Int = 2): retrofit2.Response<GetFoodFromNameResponse>? {
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

    // Handle search query changes with debounce
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(500) // Debounce - wait 500ms after user stops typing
            performSearch(searchQuery)
        } else {
            showSearchResults = false
            searchResults = emptyList()
        }
    }

    // Function to load food history
    suspend fun loadFoodHistory() {
        isLoading = true
        try {
            // First try to get from API
            val response = RetrofitClient.api.getFoodLogs(userId = userId)
            var allFoodLogs = emptyList<FoodLog>()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val apiFoodLogs = response.body()?.data ?: emptyList()
                allFoodLogs = apiFoodLogs.map { log ->
                    log.copy(localId = log.id)
                }
                Log.d(TAG, "Loaded ${allFoodLogs.size} food logs from API")
            } else {
                Log.w(TAG, "API call failed: ${response.message()}")
            }
            
            // Also try local database if repository is provided
            if (repository != null) {
                Log.d(TAG, "Also checking local database for additional food logs...")
                val localFoodLogs = repository.getAllFoodLogs(userId)
                val localFoodLogsConverted = localFoodLogs.map { entity ->
                    FoodLog(
                        id = entity.serverId ?: entity.localId,
                        localId = entity.localId,
                        userId = entity.userId,
                        foodName = entity.foodName,
                        servingSize = entity.servingSize,
                        measuringUnit = entity.measuringUnit,
                        date = entity.date,
                        mealType = entity.mealType,
                        calories = entity.calories,
                        carbs = entity.carbs,
                        fat = entity.fat,
                        protein = entity.protein,
                        foodId = entity.foodId,
                        createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date(entity.createdAt)),
                        updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date(entity.createdAt))
                    )
                }
                Log.d(TAG, "Loaded ${localFoodLogsConverted.size} food logs from local database")
                
                // Combine API and local data
                val combinedFoodLogs = if (allFoodLogs.isNotEmpty()) {
                    val apiIds = allFoodLogs.map { it.id }.toSet()
                    val localOnly = localFoodLogsConverted.filter { it.id !in apiIds }
                    allFoodLogs + localOnly
                } else {
                    localFoodLogsConverted
                }
                
                allFoodLogs = combinedFoodLogs
                Log.d(TAG, "Combined total: ${allFoodLogs.size} food logs")
            }

            // Group by food name and take most recent entry for each unique food
            historyItems = allFoodLogs
                .groupBy { it.foodName.lowercase().trim() }
                .map { (_, logs) -> logs.maxByOrNull { it.createdAt } ?: logs.first() }
                .map { log ->
                    RecentActivityEntry(
                        id = log.id,
                        localId = log.localId,
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

            errorMessage = ""
            Log.d(TAG, "Loaded My Foods: ${historyItems.size} unique foods")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading food history", e)
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Load food history when screen first loads
    LaunchedEffect(userId) {
        loadFoodHistory()
    }

    // Refresh food history when refresh trigger changes
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            Log.d(TAG, "Refreshing My Foods due to trigger: $refreshTrigger")
            loadFoodHistory()
        }
    }

    // UI Layout
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
                    text = screenTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Search bar and content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
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
                            contentDescription = "Search",
                            tint = colorScheme.secondary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    stringResource(R.string.search_food),
                                    color = colorScheme.onSurfaceVariant
                                )
                            },
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

                // Search Results or History
                if (searchQuery.isNotBlank()) {
                    if (showSearchResults && searchResults.isNotEmpty()) {
                        Text(
                            stringResource(R.string.search_results),
                            fontSize = 16.sp,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            searchResults.forEach { foodItem ->
                                SearchResultCard(
                                    foodItem = foodItem,
                                    onClick = { onSearchFoodSelected(foodItem) }
                                )
                            }
                            Spacer(modifier = Modifier.height(100.dp)) // Space for buttons
                        }
                    } else if (isSearching) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Text(
                            stringResource(R.string.no_results_found, searchQuery),
                            fontSize = 14.sp,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    // Food History Section
                    Text(
                        stringResource(R.string.my_foods),
                        fontSize = 16.sp,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
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
                                stringResource(R.string.no_foods_logged),
                                fontSize = 14.sp,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                        else -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
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
                                        onClick = { onNavigateToAdjustServing(item) },
                                        onDelete = {
                                            scope.launch {
                                                try {
                                                    val response = RetrofitClient.api.deleteFoodLog(item.id)
                                                    if (response.isSuccessful && response.body()?.success == true) {
                                                        historyItems = historyItems.filter { it.id != item.id }
                                                        Toast.makeText(
                                                            context,
                                                            context.getString(R.string.deleted_successfully),
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            context.getString(R.string.failed_to_delete),
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "Error deleting: ${e.message}")
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(R.string.error_message, e.message ?: ""),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(100.dp)) // Space for buttons
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Buttons (Fixed at bottom)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Scan Barcode Button
                    Button(
                        onClick = onBarcodeScan,
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
                                stringResource(R.string.scan_barcode),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.background
                            )
                        }
                    }

                    // Add Manually Button
                    Button(
                        onClick = { onNavigateToAdjustServing(null) },
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
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = colorScheme.background,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    screenTitle,
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

@Composable
fun SearchResultCard(
    foodItem: SearchFoodItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.background, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = colorScheme.outline, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = foodItem.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )

                if (!foodItem.brand.isNullOrBlank()) {
                    Text(
                        text = foodItem.brand,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

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
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

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
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = colorScheme.secondary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add food",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF1E9ECD)
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
            .background(colorScheme.background, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = colorScheme.outline, shape = RoundedCornerShape(12.dp))
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
                    Text(date, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    Text("•", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    Text(mealType, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Delete Food Log?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to delete \"$foodName\" from your log?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
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

