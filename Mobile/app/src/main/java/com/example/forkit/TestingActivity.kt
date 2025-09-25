package com.example.forkit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.data.ApiService
import com.example.forkit.data.models.*
import com.example.forkit.ui.theme.ForkItTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TestingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForkItTheme {
                TestingScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestingScreen() {
    val scrollState = rememberScrollState()
    var responseText by remember { mutableStateOf("API responses will appear here...") }
    
    // Retrofit setup
    val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/") // Android emulator localhost
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService = retrofit.create(ApiService::class.java)
    val coroutineScope = rememberCoroutineScope()
    
    fun makeApiCall(apiCall: suspend () -> Unit) {
        coroutineScope.launch {
            try {
                responseText = "Loading..."
                apiCall()
            } catch (e: Exception) {
                responseText = "Error: ${e.message}"
                Log.e("TestingActivity", "API Error", e)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "API Endpoint Testing",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        // Response Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Response:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = responseText,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // USER ENDPOINTS SECTION
        // Get User by ID
        GetUserSection { userId ->
            makeApiCall {
                val response = apiService.getUserById(userId)
                responseText = "Get User Response:\n${response.body()}"
            }
        }
        
        // Get User Streak
        GetUserStreakSection { userId ->
            makeApiCall {
                val response = apiService.getUserStreak(userId)
                responseText = "Get User Streak Response:\n${response.body()}"
            }
        }
        
        // Update User
        UpdateUserSection { userId, email, age, height, weight ->
            makeApiCall {
                val userData = User(
                    userId = userId,
                    success = true,
                    message = "",
                    email = email,
                    age = age,
                    height = height,
                    weight = weight,
                    streakData = StreakData(0, 0, "", "", false)
                )
                val request = UpdateUserRequest(userId, userData)
                val response = apiService.updateUser(userId, request)
                responseText = "Update User Response:\n${response.body()}"
            }
        }
        
        // Delete User
        DeleteUserSection { userId ->
            makeApiCall {
                val response = apiService.deleteUser(userId)
                responseText = "Delete User Response:\n${response.body()}"
            }
        }
        
        // FOOD ENDPOINTS SECTION
        Text(
            text = "FOOD ENDPOINTS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        // Get Food from Barcode
        GetFoodFromBarcodeSection { barcode ->
            makeApiCall {
                val response = apiService.getFoodFromBarcode(barcode)
                responseText = "Get Food from Barcode Response:\n${response.body()}"
            }
        }
        
        // Get Food from Name
        GetFoodFromNameSection { name ->
            makeApiCall {
                val response = apiService.getFoodFromName(name)
                responseText = "Get Food from Name Response:\n${response.body()}"
            }
        }
        
        // Create Food
        CreateFoodSection { foodData ->
            makeApiCall {
                val request = CreateFoodRequest(foodData)
                val response = apiService.createFood(request)
                responseText = "Create Food Response:\n${response.body()}"
            }
        }
        
        // Update Food
        UpdateFoodSection { foodId, foodData ->
            makeApiCall {
                val request = UpdateFoodRequest(foodData)
                val response = apiService.updateFood(foodId, request)
                responseText = "Update Food Response:\n${response.body()}"
            }
        }
        
        // Delete Food
        DeleteFoodSection { foodId ->
            makeApiCall {
                val response = apiService.deleteFood(foodId)
                responseText = "Delete Food Response:\n${response.body()}"
            }
        }
    }
}

@Composable
fun UserRegisterSection(onTest: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Register User", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(email, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Register")
            }
        }
    }
}

@Composable
fun UserLoginSection(onTest: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Login User", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(email, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Login")
            }
        }
    }
}

@Composable
fun GetUserSection(onTest: (String) -> Unit) {
    var userId by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Get User by ID", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(userId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Get User")
            }
        }
    }
}

@Composable
fun GetUserStreakSection(onTest: (String) -> Unit) {
    var userId by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Get User Streak", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(userId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Get Streak")
            }
        }
    }
}

@Composable
fun UpdateUserSection(onTest: (String, String, Int, Double, Double) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Update User", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    onTest(
                        userId, 
                        email, 
                        age.toIntOrNull() ?: 0, 
                        height.toDoubleOrNull() ?: 0.0, 
                        weight.toDoubleOrNull() ?: 0.0
                    ) 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Update User")
            }
        }
    }
}

@Composable
fun DeleteUserSection(onTest: (String) -> Unit) {
    var userId by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Delete User", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(userId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Delete User")
            }
        }
    }
}

@Composable
fun GetFoodFromBarcodeSection(onTest: (String) -> Unit) {
    var barcode by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Get Food from Barcode", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("Barcode") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(barcode) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Get Food from Barcode")
            }
        }
    }
}

@Composable
fun GetFoodFromNameSection(onTest: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Get Food from Name", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Food Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(name) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Get Food from Name")
            }
        }
    }
}

@Composable
fun CreateFoodSection(onTest: (Food) -> Unit) {
    var id by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var sugar by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Create Food", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("Food ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Food Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("Barcode") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                label = { Text("Calories") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Nutrients:", fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbs") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fat") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = fiber,
                    onValueChange = { fiber = it },
                    label = { Text("Fiber") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = sugar,
                onValueChange = { sugar = it },
                label = { Text("Sugar") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = image,
                onValueChange = { image = it },
                label = { Text("Image URL") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Ingredients") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    val nutrients = Nutrients(
                        carbs = carbs.toDoubleOrNull() ?: 0.0,
                        protein = protein.toDoubleOrNull() ?: 0.0,
                        fat = fat.toDoubleOrNull() ?: 0.0,
                        fiber = fiber.toDoubleOrNull() ?: 0.0,
                        sugar = sugar.toDoubleOrNull() ?: 0.0
                    )
                    val food = Food(
                        id = id,
                        name = name,
                        brand = brand,
                        barcode = barcode,
                        calories = calories.toDoubleOrNull() ?: 0.0,
                        nutrients = nutrients,
                        image = image,
                        ingredients = ingredients
                    )
                    onTest(food)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Create Food")
            }
        }
    }
}

@Composable
fun UpdateFoodSection(onTest: (String, Food) -> Unit) {
    var foodId by remember { mutableStateOf("") }
    var id by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var sugar by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Update Food", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = foodId,
                onValueChange = { foodId = it },
                label = { Text("Food ID to Update") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("New Food ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Food Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("Barcode") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                label = { Text("Calories") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Nutrients:", fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbs") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fat") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = fiber,
                    onValueChange = { fiber = it },
                    label = { Text("Fiber") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = sugar,
                onValueChange = { sugar = it },
                label = { Text("Sugar") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = image,
                onValueChange = { image = it },
                label = { Text("Image URL") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Ingredients") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    val nutrients = Nutrients(
                        carbs = carbs.toDoubleOrNull() ?: 0.0,
                        protein = protein.toDoubleOrNull() ?: 0.0,
                        fat = fat.toDoubleOrNull() ?: 0.0,
                        fiber = fiber.toDoubleOrNull() ?: 0.0,
                        sugar = sugar.toDoubleOrNull() ?: 0.0
                    )
                    val food = Food(
                        id = id,
                        name = name,
                        brand = brand,
                        barcode = barcode,
                        calories = calories.toDoubleOrNull() ?: 0.0,
                        nutrients = nutrients,
                        image = image,
                        ingredients = ingredients
                    )
                    onTest(foodId, food)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Update Food")
            }
        }
    }
}

@Composable
fun DeleteFoodSection(onTest: (String) -> Unit) {
    var foodId by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Delete Food", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = foodId,
                onValueChange = { foodId = it },
                label = { Text("Food ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(foodId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Delete Food")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestingScreenPreview() {
    ForkItTheme {
        TestingScreen()
    }
}