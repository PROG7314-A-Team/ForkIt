package com.example.forkit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.forkit.ui.theme.ForkItTheme
import com.example.forkit.R
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.UpdateUserProfileRequest
import kotlinx.coroutines.launch
import android.widget.Toast

class TellUsAboutYourselfActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val userId = intent.getStringExtra("USER_ID") ?: ""
        
        setContent {
            ForkItTheme {
                TellUsAboutYourselfScreen(
                    userId = userId,
                    onBackPressed = { finish() },
                    onContinue = { age, height, weight ->
                        // Navigate to SetGoalsActivity
                        val intent = Intent(this, SetGoalsActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        intent.putExtra("AGE", age)
                        intent.putExtra("HEIGHT", height)
                        intent.putExtra("WEIGHT", weight)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun TellUsAboutYourselfScreen(
    userId: String,
    onBackPressed: () -> Unit,
    onContinue: (Int, Double, Double) -> Unit
) {
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with back button and progress
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
                    tint = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Progress indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // First step - completed
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF22B27D),
                                    Color(0xFF1E9ECD)
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                // Second step - not completed
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "1/2",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
        }
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title
            Text(
                text = "Tell us about yourself",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22B27D),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Age Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "How old are you?",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
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
                                    errorMessage = null
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "Enter your age",
                                    color = Color(0xFF666666),
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
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        Text(
                            text = "years",
                            color = Color(0xFF666666),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Height Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "How tall are you?",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
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
                                    errorMessage = null
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "Enter your height",
                                    color = Color(0xFF666666),
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
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        
                        Text(
                            text = "cm",
                            color = Color(0xFF666666),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Weight Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "What's your current weight?",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
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
                                    errorMessage = null
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "Enter your weight",
                                    color = Color(0xFF666666),
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
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        
                        Text(
                            text = "kg",
                            color = Color(0xFF666666),
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
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Continue Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF22B27D),
                                Color(0xFF1E9ECD)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { 
                        if (!isLoading) {
                            // Validate inputs
                            val ageInt = age.toIntOrNull()
                            val heightDouble = height.toDoubleOrNull()
                            val weightDouble = weight.toDoubleOrNull()
                            
                            when {
                                age.isEmpty() || ageInt == null || ageInt < 1 || ageInt > 120 -> {
                                    errorMessage = "Please enter a valid age (1-120)"
                                }
                                height.isEmpty() || heightDouble == null || heightDouble < 50 || heightDouble > 250 -> {
                                    errorMessage = "Please enter a valid height (50-250 cm)"
                                }
                                weight.isEmpty() || weightDouble == null || weightDouble < 20 || weightDouble > 300 -> {
                                    errorMessage = "Please enter a valid weight (20-300 kg)"
                                }
                                else -> {
                                    // Save profile data first, then continue
                                    scope.launch {
                                        saveUserProfile(
                                            context = context,
                                            userId = userId,
                                            age = ageInt,
                                            height = heightDouble,
                                            weight = weightDouble,
                                            isLoading = { isLoading = it },
                                            errorMessage = { errorMessage = it },
                                            onSuccess = {
                                                onContinue(ageInt, heightDouble, weightDouble)
                                            }
                                        )
                                    }
                                }
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
                        text = stringResource(id = R.string.btn_continue),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Bottom spacing
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private suspend fun saveUserProfile(
    context: Context,
    userId: String,
    age: Int,
    height: Double,
    weight: Double,
    isLoading: (Boolean) -> Unit,
    errorMessage: (String?) -> Unit,
    onSuccess: () -> Unit
) {
    try {
        isLoading(true)
        errorMessage(null)
        
        android.util.Log.d("TellUsAboutYourselfActivity", "Saving profile for user: $userId")
        android.util.Log.d("TellUsAboutYourselfActivity", "Profile: age=$age, height=$height, weight=$weight")
        
        if (userId.isEmpty()) {
            errorMessage("User ID is missing. Please log in again.")
            return
        }
        
        val profileRequest = UpdateUserProfileRequest(
            age = age,
            height = height,
            weight = weight
        )
        
        android.util.Log.d("TellUsAboutYourselfActivity", "API Request: $profileRequest")
        
        val response = RetrofitClient.api.updateUserProfile(userId, profileRequest)
        
        android.util.Log.d("TellUsAboutYourselfActivity", "Response code: ${response.code()}")
        android.util.Log.d("TellUsAboutYourselfActivity", "Response body: ${response.body()}")
        
        if (response.isSuccessful && response.body()?.success == true) {
            android.util.Log.d("TellUsAboutYourselfActivity", "Profile saved successfully")
            Toast.makeText(context, "Profile saved successfully! 🎉", Toast.LENGTH_SHORT).show()
            onSuccess()
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("TellUsAboutYourselfActivity", "Error response body: $errorBody")
            
            val errorMsg = response.body()?.message ?: "Failed to save profile (Code: ${response.code()})"
            android.util.Log.e("TellUsAboutYourselfActivity", "Failed to save profile: $errorMsg")
            Toast.makeText(context, "Failed to save profile: $errorMsg", Toast.LENGTH_LONG).show()
            errorMessage(errorMsg)
        }
        
    } catch (e: Exception) {
        val errorMsg = "Error saving profile: ${e.message}"
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        errorMessage(errorMsg)
    } finally {
        isLoading(false)
    }
}
