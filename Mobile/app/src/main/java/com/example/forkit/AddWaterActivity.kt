package com.example.forkit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.CreateWaterLogRequest
import com.example.forkit.ui.theme.ForkItTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import com.example.forkit.data.repository.WaterLogRepository
import com.example.forkit.data.local.AppDatabase
import com.example.forkit.utils.NetworkConnectivityManager

class AddWaterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val userId = intent.getStringExtra("USER_ID") ?: ""
        
        setContent {
            ForkItTheme {
                AddWaterScreen(
                    userId = userId,
                    onBackPressed = { finish() },
                    onSuccess = { 
                        Toast.makeText(this, getString(R.string.water_logged_success), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWaterScreen(
    userId: String,
    onBackPressed: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Initialize repository
    val database = remember { AppDatabase.getInstance(context) }
    val networkManager = remember { NetworkConnectivityManager(context) }
    val repository = remember {
        WaterLogRepository(
            apiService = RetrofitClient.api,
            waterLogDao = database.waterLogDao(),
            networkManager = networkManager
        )
    }
    val isOnline = remember { networkManager.isOnline() }
    
    var amount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Initialize with current date
    val currentDate = remember { Date() }
    var selectedDate by remember { mutableStateOf(currentDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Format date for display and API
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val apiDateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val selectedDateString = remember(selectedDate) { dateFormatter.format(selectedDate) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.add_water),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            // Main content - centered
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Input fields section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                    // Amount input field
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            // Only allow numbers
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                amount = it
                                errorMessage = ""
                            }
                        },
                        label = { Text(stringResource(R.string.amount_ml)) },
                        placeholder = { Text(stringResource(R.string.enter_amount_ml)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            focusedLabelColor = MaterialTheme.colorScheme.secondary,
                            cursorColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    // Date input field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { showDatePicker = true }
                    ) {
                        Text(
                            text = selectedDateString,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                                .wrapContentHeight(Alignment.CenterVertically)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Quick Adds section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.quick_adds),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Quick add buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            QuickAddButton(
                                text = stringResource(R.string.add_250ml),
                                onClick = { amount = "250" }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            QuickAddButton(
                                text = stringResource(R.string.add_500ml),
                                onClick = { amount = "500" }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            QuickAddButton(
                                text = stringResource(R.string.add_1000ml),
                                onClick = { amount = "1000" }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Error message
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Add Water button
                Button(
                    onClick = {
                        // Validate input
                        if (amount.isEmpty()) {
                            errorMessage = context.getString(R.string.please_enter_amount)
                            return@Button
                        }
                        
                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue == null || amountValue <= 0) {
                            errorMessage = context.getString(R.string.please_enter_valid_amount)
                            return@Button
                        }
                        
                        if (userId.isEmpty()) {
                            errorMessage = context.getString(R.string.user_id_not_found)
                            return@Button
                        }
                        
                        // Clear error and start loading
                        errorMessage = ""
                        isLoading = true
                        
                        // Use repository for offline-first logging
                        scope.launch {
                            try {
                                android.util.Log.d("AddWaterActivity", "Adding water: $amountValue ml for user $userId on ${apiDateFormatter.format(selectedDate)}")
                                
                                val result = repository.createWaterLog(
                                    userId = userId,
                                    amount = amountValue,
                                    date = apiDateFormatter.format(selectedDate)
                                )
                                
                                result.onSuccess { id ->
                                    android.util.Log.d("AddWaterActivity", "Water logged successfully: $id")
                        if (!isOnline) {
                            Toast.makeText(
                                context,
                                "Saved offline - will sync when connected",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                                    onSuccess()
                                }.onFailure { e ->
                                    android.util.Log.e("AddWaterActivity", "Failed to log water: ${e.message}", e)
                                    errorMessage = "Couldn't save water. Please try again"
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("AddWaterActivity", "Error logging water: ${e.message}", e)
                                errorMessage = "Something went wrong. Please try again"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.add_water),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.time
            )
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = Date(millis)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text(
                            stringResource(R.string.ok),
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePicker = false }
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = MaterialTheme.colorScheme.secondary,
                        todayDateBorderColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun QuickAddButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .border(
                width = 3.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium
        )
    }
}
