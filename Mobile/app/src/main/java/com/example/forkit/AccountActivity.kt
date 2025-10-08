package com.example.forkit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.UpdateUserProfileRequest
import com.example.forkit.ui.theme.ForkItTheme
import com.example.forkit.R
import kotlinx.coroutines.launch

class AccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val userId = intent.getStringExtra("USER_ID") ?: ""

        setContent {
            ForkItTheme {
                AccountScreen(
                    userId = userId,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@Composable
fun AccountScreen(
    userId: String,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }

    // Load user data on first composition
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            loadUserData(
                context = context,
                userId = userId,
                onDataLoaded = { userAge, userHeight, userWeight, userEmail ->
                    age = userAge?.toString() ?: ""
                    height = userHeight?.toString() ?: ""
                    weight = userWeight?.toString() ?: ""
                    email = userEmail ?: ""
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = stringResource(id = R.string.account),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email
            Text(
                text = email.ifEmpty { "user@example.com" },
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Weight Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.weight_label),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFF1E9ECD), // Light blue border
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = weight,
                            onValueChange = { 
                                if (it.length <= 6 && (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$")))) {
                                    weight = it
                                    hasChanges = true
                                    errorMessage = null
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "75 kg",
                                    color = Color(0xFF1E9ECD),
                                    fontSize = 16.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFF22B27D)
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                color = Color(0xFF1E9ECD),
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        
                        Text(
                            text = "kg",
                            color = Color(0xFF1E9ECD),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Age Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.age_label),
                    fontSize = 16.sp,
                    color = Color(0xFF1E9ECD), // Light blue color
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFF1E9ECD), // Light blue border
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = age,
                            onValueChange = { 
                                if (it.length <= 3 && (it.isEmpty() || it.all { char -> char.isDigit() })) {
                                    age = it
                                    hasChanges = true
                                    errorMessage = null
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "25 years",
                                    color = Color(0xFF1E9ECD),
                                    fontSize = 16.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFF22B27D)
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                color = Color(0xFF1E9ECD),
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        Text(
                            text = "years",
                            color = Color(0xFF1E9ECD),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Height Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.height_label),
                    fontSize = 16.sp,
                    color = Color(0xFF1E9ECD), // Light blue color
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFF1E9ECD), // Light blue border
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = height,
                            onValueChange = { 
                                if (it.length <= 6 && (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$")))) {
                                    height = it
                                    hasChanges = true
                                    errorMessage = null
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "175.5 cm",
                                    color = Color(0xFF1E9ECD),
                                    fontSize = 16.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFF22B27D)
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                color = Color(0xFF1E9ECD),
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        
                        Text(
                            text = "cm",
                            color = Color(0xFF1E9ECD),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Error message
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Update Data Button (only show if there are changes)
            if (hasChanges) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            color = Color(0xFF22B27D), // Green color
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { 
                            if (!isLoading) {
                                scope.launch {
                                    updateUserProfile(
                                        context = context,
                                        userId = userId,
                                        age = age,
                                        height = height,
                                        weight = weight,
                                        isLoading = { isLoading = it },
                                        errorMessage = { errorMessage = it },
                                        onSuccess = { hasChanges = false }
                                    )
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.update_your_account),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Delete Account Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(
                        width = 2.dp,
                        color = Color.Red,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { showDeleteDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.delete_account),
                    color = Color.Red,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Bottom spacing
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Delete Account Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(id = R.string.delete_account_title)) },
            text = { Text(stringResource(id = R.string.delete_account_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            deleteUserAccount(
                                context = context,
                                userId = userId,
                                onSuccess = {
                                    // Navigate back to login/signup
                                    val intent = Intent(context, SignInActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                    (context as? ComponentActivity)?.finish()
                                }
                            )
                        }
                    }
                ) {
                    Text(stringResource(id = R.string.delete), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

private suspend fun loadUserData(
    context: Context,
    userId: String,
    onDataLoaded: (Int?, Double?, Double?, String?) -> Unit
) {
    try {
        android.util.Log.d("AccountActivity", "Loading user data for: $userId")
        
        val response = RetrofitClient.api.getUserById(userId)
        
        if (response.isSuccessful && response.body()?.success == true) {
            val responseBody = response.body()
            android.util.Log.d("AccountActivity", "API Response: $responseBody")
            
            // The API returns an array of users, get the first one
            if (responseBody?.data != null) {
                try {
                    // Parse the user data from the response
                    val userDataList = responseBody.data as? List<Map<String, Any>>
                    android.util.Log.d("AccountActivity", "User data list: $userDataList")
                    
                    if (userDataList != null && userDataList.isNotEmpty()) {
                        val userData = userDataList[0]
                        android.util.Log.d("AccountActivity", "First user data: $userData")
                        
                        // Extract individual fields - these might be null if not set
                        val userAge = (userData["age"] as? Double)?.toInt()
                        val userHeight = userData["height"]?.toString()?.toDoubleOrNull()
                        val userWeight = userData["weight"]?.toString()?.toDoubleOrNull()
                        val userEmail = userData["email"]?.toString()
                        
                        android.util.Log.d("AccountActivity", "Parsed data - Age: $userAge, Height: $userHeight, Weight: $userWeight, Email: $userEmail")
                        
                        onDataLoaded(userAge, userHeight, userWeight, userEmail)
                    } else {
                        android.util.Log.e("AccountActivity", "No users found in data array")
                        onDataLoaded(null, null, null, "user@example.com")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AccountActivity", "Error parsing user data: ${e.message}")
                    onDataLoaded(null, null, null, "user@example.com")
                }
            } else {
                android.util.Log.e("AccountActivity", "No user data found in response")
                onDataLoaded(null, null, null, "user@example.com")
            }
        } else {
            android.util.Log.e("AccountActivity", "Failed to load user data: ${response.code()}")
            onDataLoaded(null, null, null, "user@example.com")
        }
    } catch (e: Exception) {
        android.util.Log.e("AccountActivity", "Error loading user data: ${e.message}")
        onDataLoaded(null, null, null, "user@example.com")
    }
}

private suspend fun updateUserProfile(
    context: Context,
    userId: String,
    age: String,
    height: String,
    weight: String,
    isLoading: (Boolean) -> Unit,
    errorMessage: (String?) -> Unit,
    onSuccess: () -> Unit
) {
    try {
        isLoading(true)
        errorMessage(null)
        
        val ageInt = age.toIntOrNull()
        val heightDouble = height.toDoubleOrNull()
        val weightDouble = weight.toDoubleOrNull()
        
        when {
            age.isEmpty() || ageInt == null || ageInt < 1 || ageInt > 120 -> {
                errorMessage("Please enter a valid age (1-120)")
                return
            }
            height.isEmpty() || heightDouble == null || heightDouble < 50 || heightDouble > 250 -> {
                errorMessage("Please enter a valid height (50-250 cm)")
                return
            }
            weight.isEmpty() || weightDouble == null || weightDouble < 20 || weightDouble > 300 -> {
                errorMessage("Please enter a valid weight (20-300 kg)")
                return
            }
        }
        
        android.util.Log.d("AccountActivity", "Updating profile for user: $userId")
        
        val profileRequest = UpdateUserProfileRequest(
            age = ageInt,
            height = heightDouble,
            weight = weightDouble
        )
        
        val response = RetrofitClient.api.updateUserProfile(userId, profileRequest)
        
        if (response.isSuccessful && response.body()?.success == true) {
            android.util.Log.d("AccountActivity", "Profile updated successfully")
            Toast.makeText(context, "Profile updated successfully! 🎉", Toast.LENGTH_SHORT).show()
            onSuccess()
        } else {
            val errorMsg = response.body()?.message ?: "Failed to update profile (Code: ${response.code()})"
            Toast.makeText(context, "Failed to update profile: $errorMsg", Toast.LENGTH_LONG).show()
            errorMessage(errorMsg)
        }
        
    } catch (e: Exception) {
        val errorMsg = "Error updating profile: ${e.message}"
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        errorMessage(errorMsg)
    } finally {
        isLoading(false)
    }
}

private suspend fun deleteUserAccount(
    context: Context,
    userId: String,
    onSuccess: () -> Unit
) {
    try {
        android.util.Log.d("AccountActivity", "Deleting account for user: $userId")
        
        val response = RetrofitClient.api.deleteUser(userId)
        
        if (response.isSuccessful && response.body()?.success == true) {
            android.util.Log.d("AccountActivity", "Account deleted successfully")
            Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()
            onSuccess()
        } else {
            val errorMsg = response.body()?.message ?: "Failed to delete account (Code: ${response.code()})"
            Toast.makeText(context, "Failed to delete account: $errorMsg", Toast.LENGTH_LONG).show()
        }
        
    } catch (e: Exception) {
        val errorMsg = "Error deleting account: ${e.message}"
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
    }
}