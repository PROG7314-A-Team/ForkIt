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
        
        // Get User Goals
        GetUserGoalsSection { userId ->
            makeApiCall {
                val response = apiService.getUserGoals(userId)
                responseText = "Get User Goals Response:\n${response.body()}"
            }
        }
        
        // Update User Goals
        UpdateUserGoalsSection { userId, dailyCalories, dailyWater, dailySteps, weeklyExercises ->
            makeApiCall {
                val request = UpdateUserGoalsRequest(
                    dailyCalories = dailyCalories,
                    dailyWater = dailyWater,
                    dailySteps = dailySteps,
                    weeklyExercises = weeklyExercises
                )
                val response = apiService.updateUserGoals(userId, request)
                responseText = "Update User Goals Response:\n${response.body()}"
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
                responseText = "Create Food Response:\n${response}"
            }
        }
        
        // Update Food
        UpdateFoodSection { foodId, foodData ->
            makeApiCall {
                val request = UpdateFoodRequest(foodData)
                val response = apiService.updateFood(foodId, request)
                responseText = "Update Food Response:\n${response}"
            }
        }
        
        // Delete Food
        DeleteFoodSection { foodId ->
            makeApiCall {
                val response = apiService.deleteFood(foodId)
                responseText = "Delete Food Response:\n${response}"
            }
        }
        
        // FOOD LOGGING ENDPOINTS SECTION
        Text(
            text = "FOOD LOGGING ENDPOINTS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        // Get Food Logs
        GetFoodLogsSection { userId, date ->
            makeApiCall {
                val response = apiService.getFoodLogs(userId, date)
                responseText = "Get Food Logs Response:\n${response.body()}"
            }
        }
        
        // Create Food Log
        CreateFoodLogSection { foodLogData ->
            makeApiCall {
                val request = CreateFoodLogRequest(
                    userId = foodLogData.userId,
                    foodName = foodLogData.foodName,
                    servingSize = foodLogData.servingSize,
                    measuringUnit = foodLogData.measuringUnit,
                    date = foodLogData.date,
                    mealType = foodLogData.mealType,
                    calories = foodLogData.calories,
                    carbs = foodLogData.carbs,
                    fat = foodLogData.fat,
                    protein = foodLogData.protein,
                    foodId = foodLogData.foodId
                )
                val response = apiService.createFoodLog(request)
                responseText = "Create Food Log Response:\n${response.body()}"
            }
        }
        
        // Get Daily Calorie Summary
        GetDailyCalorieSummarySection { userId, date ->
            makeApiCall {
                val response = apiService.getDailyCalorieSummary(userId, date)
                responseText = "Get Daily Calorie Summary Response:\n${response.body()}"
            }
        }
        
        // MEAL LOGGING ENDPOINTS SECTION
        Text(
            text = "MEAL LOGGING ENDPOINTS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        // Get Meal Logs
        GetMealLogsSection { userId, date ->
            makeApiCall {
                val response = apiService.getMealLogs(userId, date)
                responseText = "Get Meal Logs Response:\n${response.body()}"
            }
        }
        
        // Create Meal Log
        CreateMealLogSection { mealLogData ->
            makeApiCall {
                val request = CreateMealLogRequest(
                    userId = mealLogData.userId,
                    name = mealLogData.name,
                    description = mealLogData.description,
                    ingredients = mealLogData.ingredients,
                    instructions = mealLogData.instructions,
                    totalCalories = mealLogData.totalCalories,
                    totalCarbs = mealLogData.totalCarbs,
                    totalFat = mealLogData.totalFat,
                    totalProtein = mealLogData.totalProtein,
                    servings = mealLogData.servings,
                    date = mealLogData.date,
                    mealType = mealLogData.mealType
                )
                val response = apiService.createMealLog(request)
                responseText = "Create Meal Log Response:\n${response.body()}"
            }
        }
        
        // WATER LOGGING ENDPOINTS SECTION
        Text(
            text = "WATER LOGGING ENDPOINTS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        // Get Water Logs
        GetWaterLogsSection { userId, date ->
            makeApiCall {
                val response = apiService.getWaterLogs(userId, date)
                responseText = "Get Water Logs Response:\n${response.body()}"
            }
        }
        
        // Create Water Log
        CreateWaterLogSection { waterLogData ->
            makeApiCall {
                val request = CreateWaterLogRequest(
                    userId = waterLogData.userId,
                    amount = waterLogData.amount,
                    date = waterLogData.date
                )
                val response = apiService.createWaterLog(request)
                responseText = "Create Water Log Response:\n${response.body()}"
            }
        }
        
        // Get Daily Water Summary
        GetDailyWaterSummarySection { userId, date ->
            makeApiCall {
                val response = apiService.getDailyWaterSummary(userId, date)
                responseText = "Get Daily Water Summary Response:\n${response.body()}"
            }
        }
        
        // EXERCISE LOGGING ENDPOINTS SECTION
        Text(
            text = "EXERCISE LOGGING ENDPOINTS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        // Get Exercise Logs
        GetExerciseLogsSection { userId, date, type ->
            makeApiCall {
                val response = apiService.getExerciseLogs(userId, date, type)
                responseText = "Get Exercise Logs Response:\n${response.body()}"
            }
        }
        
        // Create Exercise Log
        CreateExerciseLogSection { exerciseLogData ->
            makeApiCall {
                val request = CreateExerciseLogRequest(
                    userId = exerciseLogData.userId,
                    name = exerciseLogData.name,
                    date = exerciseLogData.date,
                    caloriesBurnt = exerciseLogData.caloriesBurnt,
                    type = exerciseLogData.type,
                    duration = exerciseLogData.duration,
                    notes = exerciseLogData.notes
                )
                val response = apiService.createExerciseLog(request)
                responseText = "Create Exercise Log Response:\n${response.body()}"
            }
        }
        
        // Get Daily Exercise Summary
        GetDailyExerciseSummarySection { userId, date ->
            makeApiCall {
                val response = apiService.getDailyExerciseSummary(userId, date)
                responseText = "Get Daily Exercise Summary Response:\n${response.body()}"
            }
        }
        
        // CALORIE CALCULATOR ENDPOINTS SECTION
        Text(
            text = "CALORIE CALCULATOR ENDPOINTS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        // Get Macronutrient Values
        GetMacronutrientValuesSection {
            makeApiCall {
                val response = apiService.getMacronutrientValues()
                responseText = "Get Macronutrient Values Response:\n${response.body()}"
            }
        }
        
        // Calculate Calories
        CalculateCaloriesSection { carbs, protein, fat ->
            makeApiCall {
                val request = CalculateCaloriesRequest(carbs, protein, fat)
                val response = apiService.calculateCalories(request)
                responseText = "Calculate Calories Response:\n${response.body()}"
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
                        protein = protein.toDoubleOrNull() ?: 0.0,
                        carbs = carbs.toDoubleOrNull() ?: 0.0,
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
                        protein = protein.toDoubleOrNull() ?: 0.0,
                        carbs = carbs.toDoubleOrNull() ?: 0.0,
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

// FOOD LOGGING UI COMPONENTS
@Composable
fun GetFoodLogsSection(onTest: (String?, String?) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Get Food Logs", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(userId.ifEmpty { null }, date.ifEmpty { null }) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Get Food Logs")
            }
        }
    }
}

@Composable
fun CreateFoodLogSection(onTest: (FoodLog) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var foodName by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("") }
    var measuringUnit by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var foodId by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Create Food Log", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("Food Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = servingSize,
                    onValueChange = { servingSize = it },
                    label = { Text("Serving Size") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = measuringUnit,
                    onValueChange = { measuringUnit = it },
                    label = { Text("Unit") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = mealType,
                    onValueChange = { mealType = it },
                    label = { Text("Meal Type") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Macronutrients:", fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbs") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fat") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = foodId,
                onValueChange = { foodId = it },
                label = { Text("Food ID (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    val foodLog = FoodLog(
                        id = "",
                        userId = userId,
                        foodName = foodName,
                        servingSize = servingSize.toDoubleOrNull() ?: 0.0,
                        measuringUnit = measuringUnit,
                        date = date,
                        mealType = mealType,
                        calories = calories.toDoubleOrNull() ?: 0.0,
                        carbs = carbs.toDoubleOrNull() ?: 0.0,
                        fat = fat.toDoubleOrNull() ?: 0.0,
                        protein = protein.toDoubleOrNull() ?: 0.0,
                        foodId = foodId.ifEmpty { null },
                        createdAt = "",
                        updatedAt = ""
                    )
                    onTest(foodLog)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Create Food Log")
            }
        }
    }
}

@Composable
fun GetDailyCalorieSummarySection(onTest: (String, String) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Get Daily Calorie Summary", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(userId, date) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Get Daily Calorie Summary")
            }
        }
    }
}

// MEAL LOGGING UI COMPONENTS
@Composable
fun GetMealLogsSection(onTest: (String?, String?) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Get Meal Logs", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(userId.ifEmpty { null }, date.ifEmpty { null }) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Get Meal Logs")
            }
        }
    }
}

@Composable
fun CreateMealLogSection(onTest: (MealLog) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("") }
    var totalCalories by remember { mutableStateOf("") }
    var totalCarbs by remember { mutableStateOf("") }
    var totalFat by remember { mutableStateOf("") }
    var totalProtein by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Create Meal Log", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Meal Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = mealType,
                    onValueChange = { mealType = it },
                    label = { Text("Meal Type") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Nutritional Info:", fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = totalCalories,
                    onValueChange = { totalCalories = it },
                    label = { Text("Total Calories") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it },
                    label = { Text("Servings") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = totalCarbs,
                    onValueChange = { totalCarbs = it },
                    label = { Text("Total Carbs") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = totalProtein,
                    onValueChange = { totalProtein = it },
                    label = { Text("Total Protein") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = totalFat,
                onValueChange = { totalFat = it },
                label = { Text("Total Fat") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    val mealLog = MealLog(
                        id = "",
                        userId = userId,
                        name = name,
                        description = description,
                        ingredients = emptyList<Ingredient>(),
                        instructions = emptyList(),
                        totalCalories = totalCalories.toDoubleOrNull() ?: 0.0,
                        totalCarbs = totalCarbs.toDoubleOrNull() ?: 0.0,
                        totalFat = totalFat.toDoubleOrNull() ?: 0.0,
                        totalProtein = totalProtein.toDoubleOrNull() ?: 0.0,
                        servings = servings.toDoubleOrNull() ?: 1.0,
                        date = date,
                        mealType = mealType.ifEmpty { null },
                        createdAt = "",
                        updatedAt = ""
                    )
                    onTest(mealLog)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Create Meal Log")
            }
        }
    }
}

// WATER LOGGING UI COMPONENTS
@Composable
fun GetWaterLogsSection(onTest: (String?, String?) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Get Water Logs", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(userId.ifEmpty { null }, date.ifEmpty { null }) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Get Water Logs")
            }
        }
    }
}

@Composable
fun CreateWaterLogSection(onTest: (WaterLog) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Create Water Log", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (ml)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    val waterLog = WaterLog(
                        id = "",
                        userId = userId,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        date = date,
                        createdAt = "",
                        updatedAt = ""
                    )
                    onTest(waterLog)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Create Water Log")
            }
        }
    }
}

@Composable
fun GetDailyWaterSummarySection(onTest: (String, String) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Get Daily Water Summary", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(userId, date) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Get Daily Water Summary")
            }
        }
    }
}

// EXERCISE LOGGING UI COMPONENTS
@Composable
fun GetExerciseLogsSection(onTest: (String?, String?, String?) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Get Exercise Logs", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Type (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(userId.ifEmpty { null }, date.ifEmpty { null }, type.ifEmpty { null }) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Get Exercise Logs")
            }
        }
    }
}

@Composable
fun CreateExerciseLogSection(onTest: (ExerciseLog) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var caloriesBurnt by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Create Exercise Log", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Exercise Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = caloriesBurnt,
                    onValueChange = { caloriesBurnt = it },
                    label = { Text("Calories Burnt") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (min)") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    val exerciseLog = ExerciseLog(
                        id = "",
                        userId = userId,
                        name = name,
                        date = date,
                        caloriesBurnt = caloriesBurnt.toDoubleOrNull() ?: 0.0,
                        type = type,
                        duration = duration.toDoubleOrNull(),
                        notes = notes,
                        createdAt = "",
                        updatedAt = ""
                    )
                    onTest(exerciseLog)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Create Exercise Log")
            }
        }
    }
}

@Composable
fun GetDailyExerciseSummarySection(onTest: (String, String) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Get Daily Exercise Summary", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest(userId, date) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Get Daily Exercise Summary")
            }
        }
    }
}

// CALORIE CALCULATOR UI COMPONENTS
@Composable
fun GetMacronutrientValuesSection(onTest: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Get Macronutrient Values", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onTest() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Get Macronutrient Values")
            }
        }
    }
}

@Composable
fun CalculateCaloriesSection(onTest: (Double?, Double?, Double?) -> Unit) {
    var carbs by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Calculate Calories", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbs (g)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein (g)") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = fat,
                onValueChange = { fat = it },
                label = { Text("Fat (g)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    onTest(
                        carbs.toDoubleOrNull(),
                        protein.toDoubleOrNull(),
                        fat.toDoubleOrNull()
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Calculate Calories")
            }
        }
    }
}

// USER GOALS UI COMPONENTS
@Composable
fun GetUserGoalsSection(onTest: (String) -> Unit) {
    var userId by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎯 Get User Goals",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
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
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Test Get Goals")
            }
        }
    }
}

@Composable
fun UpdateUserGoalsSection(onTest: (String, Int?, Int?, Int?, Int?) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var dailyCalories by remember { mutableStateOf("") }
    var dailyWater by remember { mutableStateOf("") }
    var dailySteps by remember { mutableStateOf("") }
    var weeklyExercises by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎯 Update User Goals",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Leave fields empty to keep current values",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = dailyCalories,
                    onValueChange = { dailyCalories = it },
                    label = { Text("Calories (kcal)") },
                    placeholder = { Text("1200-10000") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = dailyWater,
                    onValueChange = { dailyWater = it },
                    label = { Text("Water (ml)") },
                    placeholder = { Text("500-10000") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = dailySteps,
                    onValueChange = { dailySteps = it },
                    label = { Text("Steps") },
                    placeholder = { Text("0-50000") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = weeklyExercises,
                    onValueChange = { weeklyExercises = it },
                    label = { Text("Weekly Exercise") },
                    placeholder = { Text("0-21") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    onTest(
                        userId,
                        dailyCalories.toIntOrNull(),
                        dailyWater.toIntOrNull(),
                        dailySteps.toIntOrNull(),
                        weeklyExercises.toIntOrNull()
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Test Update Goals")
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