package com.example.forkit

import org.junit.Test
import org.junit.Assert.*
import com.example.forkit.data.models.*

class DataModelsTests {

    @Test
    fun `AuthModels are created correctly`() {
        val registerRequest = RegisterRequest("testuser@email.com", "testpassword")
        val registerResponse = RegisterResponse("success", "testuser-123", "testuser@email.com")
        val loginRequest = LoginRequest("testuser@email.com", "testpassword")
        val loginResponse = LoginResponse("success", "testuser-123", "testtoken", "refreshToken", "expiresIn")

        assertEquals(registerRequest.email, "testuser@email.com")
        assertEquals(registerRequest.password, "testpassword")

        assertEquals(registerResponse.message, "success")
        assertEquals(registerResponse.uid, "testuser-123")
        assertEquals(registerResponse.email, "testuser@email.com")

        assertEquals(loginRequest.email, "testuser@email.com")
        assertEquals(loginRequest.password, "testpassword")

        assertEquals(loginResponse.message, "success")
        assertEquals(loginResponse.userId, "testuser-123")
        assertEquals(loginResponse.idToken,"testtoken" )
        assertEquals(loginResponse.refreshToken, "refreshToken")
        assertEquals(loginResponse.expiresIn, "expiresIn")
    }

    @Test
    fun `CalorieCalculatorModels are created correctly`() {
        // Request models
        val calculateCaloriesRequest = CalculateCaloriesRequest(50.0, 30.0, 20.0)
        val calculateFoodCaloriesRequest = CalculateFoodCaloriesRequest(500.0, 60.0, 25.0, 15.0)
        val calculateIndividualCaloriesRequest = CalculateIndividualCaloriesRequest("carbs", 100.0)

        // Data classes
        val macronutrientDetail = MacronutrientDetail(50.0, 200.0)
        val macronutrientBreakdown = MacronutrientBreakdown(macronutrientDetail, macronutrientDetail, macronutrientDetail)
        val macronutrientCalories = MacronutrientCalories(200.0, 120.0, 180.0)
        val calorieCalculationResult = CalorieCalculationResult(500.0, macronutrientBreakdown, macronutrientCalories)
        val validationResult = ValidationResult(true, "Valid data")
        val foodCalorieCalculationResult = FoodCalorieCalculationResult(500.0, true, macronutrientBreakdown, validationResult, 500.0)
        val individualCalorieCalculation = IndividualCalorieCalculation("carbs", 100.0, 400.0, 4)

        // Response models
        val calculateCaloriesResponse = CalculateCaloriesResponse(true, "Success", calorieCalculationResult)
        val calculateFoodCaloriesResponse = CalculateFoodCaloriesResponse(true, "Success", foodCalorieCalculationResult)
        val macronutrientValuesResponse = MacronutrientValuesResponse(true, "Success", MacronutrientValues(mapOf("carbs" to 4, "protein" to 4, "fat" to 9), mapOf("carbs" to "4 calories per gram")))
        val calculateIndividualCaloriesResponse = CalculateIndividualCaloriesResponse(true, "Success", individualCalorieCalculation)

        // Test request models
        assertEquals(50.0, calculateCaloriesRequest.carbs ?: 0.0, 0.001)
        assertEquals(30.0, calculateCaloriesRequest.protein ?: 0.0, 0.001)
        assertEquals(20.0, calculateCaloriesRequest.fat ?: 0.0, 0.001)

        assertEquals(500.0, calculateFoodCaloriesRequest.calories ?: 0.0, 0.001)
        assertEquals(60.0, calculateFoodCaloriesRequest.carbs ?: 0.0, 0.001)
        assertEquals(25.0, calculateFoodCaloriesRequest.protein ?: 0.0, 0.001)
        assertEquals(15.0, calculateFoodCaloriesRequest.fat ?: 0.0, 0.001)

        assertEquals(calculateIndividualCaloriesRequest.macronutrient, "carbs")
        assertEquals(100.0, calculateIndividualCaloriesRequest.grams, 0.001)

        // Test response models
        assertEquals(calculateCaloriesResponse.success, true)
        assertEquals(calculateCaloriesResponse.message, "Success")
        assertEquals(500.0, calculateCaloriesResponse.data.totalCalories, 0.001)

        assertEquals(calculateFoodCaloriesResponse.success, true)
        assertEquals(500.0, calculateFoodCaloriesResponse.data.totalCalories, 0.001)
        assertEquals(calculateFoodCaloriesResponse.data.calculatedFromMacronutrients, true)

        assertEquals(macronutrientValuesResponse.success, true)
        assertEquals(macronutrientValuesResponse.data.macronutrientCalories["carbs"], 4)

        assertEquals(calculateIndividualCaloriesResponse.success, true)
        assertEquals(calculateIndividualCaloriesResponse.data.macronutrient, "carbs")
        assertEquals(100.0, calculateIndividualCaloriesResponse.data.grams, 0.001)
        assertEquals(400.0, calculateIndividualCaloriesResponse.data.calories, 0.001)
    }

    @Test
    fun `ExerciseLogsModels are created correctly`() {
        // Request models
        val createExerciseLogRequest = CreateExerciseLogRequest("user123", "Running", "2024-01-01", 300.0, "Cardio", 30.0, "Morning run")
        val updateExerciseLogRequest = UpdateExerciseLogRequest("Swimming", "2024-01-02", 400.0, "Cardio", 45.0, "Pool session")

        // Data classes
        val exerciseLog = ExerciseLog("log123", "user123", "Running", "2024-01-01", 300.0, "Cardio", 30.0, "Morning run", "2024-01-01T10:00:00Z", "2024-01-01T10:00:00Z")
        val dailyExerciseTotal = DailyExerciseTotal("user123", "2024-01-01", 300.0, 30.0, 1, 1, 0, listOf(exerciseLog))
        val exerciseTypeBreakdown = ExerciseTypeBreakdown("Cardio", 300, 30, 1, 100)
        val hourlyExerciseDistribution = HourlyExerciseDistribution(10, 300, 30, 1)
        val dailyExerciseSummary = DailyExerciseSummary("user123", "2024-01-01", 300.0, 30.0, 1, listOf(exerciseTypeBreakdown), listOf(hourlyExerciseDistribution), 300.0, 30.0)

        // Response models
        val exerciseLogResponse = ExerciseLogResponse(true, "Success", exerciseLog)
        val exerciseLogsResponse = ExerciseLogsResponse(true, "Success", listOf(exerciseLog))
        val dailyExerciseTotalResponse = DailyExerciseTotalResponse(true, "Success", dailyExerciseTotal)
        val dailyExerciseSummaryResponse = DailyExerciseSummaryResponse(true, "Success", dailyExerciseSummary)

        // Test request models
        assertEquals(createExerciseLogRequest.userId, "user123")
        assertEquals(createExerciseLogRequest.name, "Running")
        assertEquals(createExerciseLogRequest.date, "2024-01-01")
        assertEquals(300.0, createExerciseLogRequest.caloriesBurnt, 0.001)
        assertEquals(createExerciseLogRequest.type, "Cardio")
        assertEquals(30.0, createExerciseLogRequest.duration ?: 0.0, 0.001)
        assertEquals(createExerciseLogRequest.notes, "Morning run")

        assertEquals(updateExerciseLogRequest.name, "Swimming")
        assertEquals(updateExerciseLogRequest.date, "2024-01-02")
        assertEquals(400.0, updateExerciseLogRequest.caloriesBurnt ?: 0.0, 0.001)

        // Test data classes
        assertEquals(exerciseLog.id, "log123")
        assertEquals(exerciseLog.userId, "user123")
        assertEquals(exerciseLog.name, "Running")
        assertEquals(300.0, exerciseLog.caloriesBurnt, 0.001)

        assertEquals(dailyExerciseTotal.userId, "user123")
        assertEquals(dailyExerciseTotal.date, "2024-01-01")
        assertEquals(300.0, dailyExerciseTotal.totalCaloriesBurnt, 0.001)
        assertEquals(dailyExerciseTotal.totalExercises, 1)

        // Test response models
        assertEquals(exerciseLogResponse.success, true)
        assertEquals(exerciseLogResponse.message, "Success")
        assertEquals(exerciseLogResponse.data.id, "log123")

        assertEquals(exerciseLogsResponse.success, true)
        assertEquals(exerciseLogsResponse.data.size, 1)

        assertEquals(dailyExerciseTotalResponse.success, true)
        assertEquals(300.0, dailyExerciseTotalResponse.data.totalCaloriesBurnt, 0.001)
    }

    @Test
    fun `FoodLogsModels are created correctly`() {
        // Request models
        val createFoodLogRequest = CreateFoodLogRequest("user123", "Apple", 1.0, "piece", "2024-01-01", "Breakfast", 80.0, 20.0, 0.3, 0.4, "food123")
        val updateFoodLogRequest = UpdateFoodLogRequest("Banana", 1.5, "piece", "2024-01-02", "Snack", 90.0, 23.0, 0.4, 1.1, "food456")

        // Data classes
        val foodLog = FoodLog("log123", "user123", "Apple", 1.0, "piece", "2024-01-01", "Breakfast", 80.0, 20.0, 0.3, 0.4, "food123", "2024-01-01T10:00:00Z", "2024-01-01T10:00:00Z")
        val mealDistributionEntry = MealDistributionEntry("Breakfast", 80.0, 25)
        val mealTotals = MealTotals(200.0, 400.0, 500.0, 100.0)
        val dailyCalorieSummary = DailyCalorieSummary("user123", "2024-01-01", 1200.0, 300.0, 50.0, 80.0, listOf(mealDistributionEntry), mealTotals, 5)
        val dailyTotalEntry = DailyTotalEntry(1200, 300, 50, 80, 5)
        val monthlyCalorieSummary = MonthlyCalorieSummary("user123", 2024, 1, 36000.0, 9000.0, 1500.0, 2400.0, 1200.0, 30, mapOf("2024-01-01" to dailyTotalEntry), 150)

        // Response models
        val foodLogResponse = FoodLogResponse(true, "Success", foodLog)
        val foodLogsResponse = FoodLogsResponse(true, "Success", listOf(foodLog))
        val dailyCalorieSummaryResponse = DailyCalorieSummaryResponse(true, "Success", dailyCalorieSummary)
        val monthlyCalorieSummaryResponse = MonthlyCalorieSummaryResponse(true, "Success", monthlyCalorieSummary)

        // Test request models
        assertEquals(createFoodLogRequest.userId, "user123")
        assertEquals(createFoodLogRequest.foodName, "Apple")
        assertEquals(1.0, createFoodLogRequest.servingSize, 0.001)
        assertEquals(createFoodLogRequest.measuringUnit, "piece")
        assertEquals(createFoodLogRequest.date, "2024-01-01")
        assertEquals(createFoodLogRequest.mealType, "Breakfast")
        assertEquals(80.0, createFoodLogRequest.calories, 0.001)
        assertEquals(createFoodLogRequest.foodId, "food123")

        assertEquals(updateFoodLogRequest.foodName, "Banana")
        assertEquals(1.5, updateFoodLogRequest.servingSize ?: 0.0, 0.001)
        assertEquals(90.0, updateFoodLogRequest.calories ?: 0.0, 0.001)

        // Test data classes
        assertEquals(foodLog.id, "log123")
        assertEquals(foodLog.userId, "user123")
        assertEquals(foodLog.foodName, "Apple")
        assertEquals(80.0, foodLog.calories, 0.001)

        assertEquals(dailyCalorieSummary.userId, "user123")
        assertEquals(dailyCalorieSummary.date, "2024-01-01")
        assertEquals(1200.0, dailyCalorieSummary.totalCalories, 0.001)
        assertEquals(dailyCalorieSummary.entryCount, 5)

        // Test response models
        assertEquals(foodLogResponse.success, true)
        assertEquals(foodLogResponse.message, "Success")
        assertEquals(foodLogResponse.data.id, "log123")

        assertEquals(foodLogsResponse.success, true)
        assertEquals(foodLogsResponse.data.size, 1)

        assertEquals(dailyCalorieSummaryResponse.success, true)
        assertEquals(1200.0, dailyCalorieSummaryResponse.data.totalCalories, 0.001)
    }

    @Test
    fun `FoodModels are created correctly`() {
        // Data classes
        val nutrients = Nutrients(50.0, 20.0, 10.0, 5.0, 15.0)
        val food = Food("food123", "Apple", "Generic", "123456789", 80.0, nutrients, "apple.jpg", "Fresh apple")
        val searchFoodItem = SearchFoodItem("Apple", "apple.jpg", nutrients, 80.0)

        // Response models
        val getFoodFromBarcodeResponse = GetFoodFromBarcodeResponse(true, "Success", food)
        val getFoodFromNameResponse = GetFoodFromNameResponse(true, "Success", mapOf("apple" to searchFoodItem))
        val createFoodRequest = CreateFoodRequest(food)
        val createFoodResponse = CreateFoodResponse(true, "Success", food)
        val updateFoodRequest = UpdateFoodRequest(food)
        val updateFoodResponse = UpdateFoodResponse(true, "Success", food)
        val deleteFoodResponse = DeleteFoodResponse(true, "Success", food)

        // Test data classes
        assertEquals(50.0, nutrients.carbs, 0.001)
        assertEquals(20.0, nutrients.protein, 0.001)
        assertEquals(10.0, nutrients.fat, 0.001)
        assertEquals(5.0, nutrients.fiber, 0.001)
        assertEquals(15.0, nutrients.sugar, 0.001)

        assertEquals(food.id, "food123")
        assertEquals(food.name, "Apple")
        assertEquals(food.brand, "Generic")
        assertEquals(food.barcode, "123456789")
        assertEquals(80.0, food.calories, 0.001)
        assertEquals(food.image, "apple.jpg")
        assertEquals(food.ingredients, "Fresh apple")

        assertEquals(searchFoodItem.name, "Apple")
        assertEquals(searchFoodItem.image, "apple.jpg")
        assertEquals(80.0, searchFoodItem.calories ?: 0.0, 0.001)

        // Test response models
        assertEquals(getFoodFromBarcodeResponse.success, true)
        assertEquals(getFoodFromBarcodeResponse.message, "Success")
        assertEquals(getFoodFromBarcodeResponse.data.id, "food123")

        assertEquals(getFoodFromNameResponse.success, true)
        assertEquals(getFoodFromNameResponse.data["apple"]?.name, "Apple")

        assertEquals(createFoodRequest.foodData.id, "food123")
        assertEquals(createFoodResponse.success, true)
        assertEquals(createFoodResponse.data.name, "Apple")

        assertEquals(updateFoodRequest.foodData.id, "food123")
        assertEquals(updateFoodResponse.success, true)
        assertEquals(updateFoodResponse.data.name, "Apple")

        assertEquals(deleteFoodResponse.success, true)
        assertEquals(deleteFoodResponse.data.name, "Apple")
    }

    @Test
    fun `HabitModels are created correctly`() {
        // Data classes
        val habit = Habit("habit123", "Drink water", "Stay hydrated", false, null, "2024-01-01T10:00:00Z", "GENERAL", "DAILY", listOf(1, 2, 3), null, "user123")
        val habitCompletion = HabitCompletion("habit123", java.time.LocalDateTime.now(), java.time.LocalDate.now())

        // Request models
        val createHabitRequest = CreateHabitRequest("Exercise daily", "Stay fit", "EXERCISE", "DAILY", listOf(1, 2, 3, 4, 5), null)
        val createHabitApiRequest = CreateHabitApiRequest("user123", createHabitRequest)
        val updateHabitRequest = UpdateHabitRequest("Updated habit", "Updated description", true, HabitCategory.HEALTH, HabitFrequency.WEEKLY)

        // Response models
        val habitResponse = HabitResponse(true, "Success", habit)
        val habitsResponse = HabitsResponse(true, "Success", listOf(habit))

        // Test data classes
        assertEquals(habit.id, "habit123")
        assertEquals(habit.title, "Drink water")
        assertEquals(habit.description, "Stay hydrated")
        assertEquals(habit.isCompleted, false)
        assertEquals(habit.category, "GENERAL")
        assertEquals(habit.frequency, "DAILY")
        assertEquals(habit.userId, "user123")

        assertEquals(habitCompletion.habitId, "habit123")

        // Test request models
        assertEquals(createHabitRequest.title, "Exercise daily")
        assertEquals(createHabitRequest.description, "Stay fit")
        assertEquals(createHabitRequest.category, "EXERCISE")
        assertEquals(createHabitRequest.frequency, "DAILY")

        assertEquals(createHabitApiRequest.userId, "user123")
        assertEquals(createHabitApiRequest.habit.title, "Exercise daily")

        assertEquals(updateHabitRequest.title, "Updated habit")
        assertEquals(updateHabitRequest.description, "Updated description")
        assertEquals(updateHabitRequest.isCompleted, true)

        // Test response models
        assertEquals(habitResponse.success, true)
        assertEquals(habitResponse.message, "Success")
        assertEquals(habitResponse.data?.id, "habit123")

        assertEquals(habitsResponse.success, true)
        assertEquals(habitsResponse.data.size, 1)

        // Test enums
        assertEquals(HabitCategory.NUTRITION.name, "NUTRITION")
        assertEquals(HabitCategory.EXERCISE.name, "EXERCISE")
        assertEquals(HabitCategory.HEALTH.name, "HEALTH")
        assertEquals(HabitCategory.GENERAL.name, "GENERAL")

        assertEquals(HabitFrequency.DAILY.name, "DAILY")
        assertEquals(HabitFrequency.WEEKLY.name, "WEEKLY")
        assertEquals(HabitFrequency.MONTHLY.name, "MONTHLY")
    }

    @Test
    fun `MealLogsModels are created correctly`() {
        // Request models
        val ingredient = Ingredient("Tomato", 2.0, "pieces")
        val createMealLogRequest = CreateMealLogRequest("user123", "Pasta", "Delicious pasta dish", listOf(ingredient), listOf("Boil water", "Add pasta"), 500.0, 60.0, 20.0, 15.0, 2.0, "2024-01-01", "Dinner")
        val updateMealLogRequest = UpdateMealLogRequest("Updated Pasta", "Updated description", listOf(ingredient), listOf("Updated instructions"), 600.0, 70.0, 25.0, 20.0, 3.0, "2024-01-02", "Lunch")

        // Data classes
        val mealLog = MealLog("log123", "user123", "Pasta", "Delicious pasta dish", listOf(ingredient), listOf("Boil water", "Add pasta"), 500.0, 60.0, 20.0, 15.0, 2.0, "2024-01-01", "Dinner", "2024-01-01T10:00:00Z", "2024-01-01T10:00:00Z")

        // Response models
        val mealLogResponse = MealLogResponse(true, "Success", mealLog)
        val mealLogsResponse = MealLogsResponse(true, "Success", listOf(mealLog))

        // Test request models
        assertEquals(createMealLogRequest.userId, "user123")
        assertEquals(createMealLogRequest.name, "Pasta")
        assertEquals(createMealLogRequest.description, "Delicious pasta dish")
        assertEquals(createMealLogRequest.ingredients.size, 1)
        assertEquals(createMealLogRequest.instructions.size, 2)
        assertEquals(500.0, createMealLogRequest.totalCalories ?: 0.0, 0.001)
        assertEquals(2.0, createMealLogRequest.servings ?: 0.0, 0.001)
        assertEquals(createMealLogRequest.date, "2024-01-01")
        assertEquals(createMealLogRequest.mealType, "Dinner")

        assertEquals(updateMealLogRequest.name, "Updated Pasta")
        assertEquals(updateMealLogRequest.description, "Updated description")
        assertEquals(600.0, updateMealLogRequest.totalCalories ?: 0.0, 0.001)

        // Test data classes
        assertEquals(ingredient.name, "Tomato")
        assertEquals(2.0, ingredient.amount, 0.001)
        assertEquals(ingredient.unit, "pieces")

        assertEquals(mealLog.id, "log123")
        assertEquals(mealLog.userId, "user123")
        assertEquals(mealLog.name, "Pasta")
        assertEquals(mealLog.description, "Delicious pasta dish")
        assertEquals(mealLog.ingredients.size, 1)
        assertEquals(mealLog.instructions.size, 2)
        assertEquals(500.0, mealLog.totalCalories, 0.001)
        assertEquals(2.0, mealLog.servings, 0.001)
        assertEquals(mealLog.date, "2024-01-01")
        assertEquals(mealLog.mealType, "Dinner")

        // Test response models
        assertEquals(mealLogResponse.success, true)
        assertEquals(mealLogResponse.message, "Success")
        assertEquals(mealLogResponse.data.id, "log123")

        assertEquals(mealLogsResponse.success, true)
        assertEquals(mealLogsResponse.data.size, 1)
    }

    @Test
    fun `UserModels are created correctly`() {
        // Data classes
        val streakData = StreakData(5, 10, "2024-01-01", "2023-12-27", true)
        val user = User("user123", true, "Success", "user@email.com", 25, 175.0, 70.0, streakData)
        val userGoals = UserGoals("user123", 2000, 8, 10000, 3, "2024-01-01T10:00:00Z")

        // Request models
        val updateUserRequest = UpdateUserRequest("user123", user)
        val updateUserGoalsRequest = UpdateUserGoalsRequest(2200, 9, 12000, 4)
        val updateUserProfileRequest = UpdateUserProfileRequest(26, 176.0, 71.0)

        // Response models
        val getUserResponse = GetUserResponse(true, "Success", user)
        val getUserStreakResponse = GetUserStreakResponse(true, "Success", streakData)
        val updateUserResponse = UpdateUserResponse(true, "Success", user)
        val deleteUserResponse = DeleteUserResponse(true, "Success", user)
        val getUserGoalsResponse = GetUserGoalsResponse(true, "Success", userGoals)
        val updateUserGoalsResponse = UpdateUserGoalsResponse(true, "Success", userGoals)
        val updateUserProfileResponse = UpdateUserProfileResponse(true, "Success", user)

        // Test data classes
        assertEquals(streakData.currentStreak, 5)
        assertEquals(streakData.longestStreak, 10)
        assertEquals(streakData.lastLogDate, "2024-01-01")
        assertEquals(streakData.streakStartDate, "2023-12-27")
        assertEquals(streakData.isActive, true)

        assertEquals(user.userId, "user123")
        assertEquals(user.success, true)
        assertEquals(user.message, "Success")
        assertEquals(user.email, "user@email.com")
        assertEquals(user.age, 25)
        assertEquals(175.0, user.height ?: 0.0, 0.001)
        assertEquals(70.0, user.weight ?: 0.0, 0.001)

        assertEquals(userGoals.userId, "user123")
        assertEquals(userGoals.dailyCalories, 2000)
        assertEquals(userGoals.dailyWater, 8)
        assertEquals(userGoals.dailySteps, 10000)
        assertEquals(userGoals.weeklyExercises, 3)

        // Test request models
        assertEquals(updateUserRequest.userId, "user123")
        assertEquals(updateUserRequest.userData.userId, "user123")

        assertEquals(updateUserGoalsRequest.dailyCalories, 2200)
        assertEquals(updateUserGoalsRequest.dailyWater, 9)
        assertEquals(updateUserGoalsRequest.dailySteps, 12000)
        assertEquals(updateUserGoalsRequest.weeklyExercises, 4)

        assertEquals(updateUserProfileRequest.age, 26)
        assertEquals(176.0, updateUserProfileRequest.height, 0.001)
        assertEquals(71.0, updateUserProfileRequest.weight, 0.001)

        // Test response models
        assertEquals(getUserResponse.success, true)
        assertEquals(getUserResponse.message, "Success")

        assertEquals(getUserStreakResponse.success, true)
        assertEquals(getUserStreakResponse.data.currentStreak, 5)

        assertEquals(updateUserResponse.success, true)
        assertEquals(updateUserResponse.data.userId, "user123")

        assertEquals(deleteUserResponse.success, true)
        assertEquals(deleteUserResponse.data.userId, "user123")

        assertEquals(getUserGoalsResponse.success, true)
        assertEquals(getUserGoalsResponse.data.dailyCalories, 2000)

        assertEquals(updateUserGoalsResponse.success, true)
        assertEquals(updateUserGoalsResponse.data.dailyCalories, 2000)

        assertEquals(updateUserProfileResponse.success, true)
        assertEquals(updateUserProfileResponse.data.userId, "user123")
    }

    @Test
    fun `WaterLogsModels are created correctly`() {
        // Request models
        val createWaterLogRequest = CreateWaterLogRequest("user123", 250.0, "2024-01-01")
        val updateWaterLogRequest = UpdateWaterLogRequest(300.0, "2024-01-02")

        // Data classes
        val waterLog = WaterLog("log123", "user123", 250.0, "2024-01-01", "2024-01-01T10:00:00Z", "2024-01-01T10:00:00Z")
        val dailyWaterTotal = DailyWaterTotal("user123", "2024-01-01", 1000.0, 4)
        val hourlyWaterDistribution = HourlyWaterDistribution(10, 250, 1, 250)
        val dailyWaterSummary = DailyWaterSummary("user123", "2024-01-01", 1000.0, 4, 250.0, listOf(hourlyWaterDistribution), 2000, 1000.0, 50)
        val dailyWaterTotalEntry = DailyWaterTotalEntry("2024-01-01", 1000, 4, true)
        val monthlyWaterSummary = MonthlyWaterSummary("user123", 2024, 1, 30000.0, 120, 1000.0, 30, 2000, 50, listOf(dailyWaterTotalEntry))
        val recentWaterActivityEntry = RecentWaterActivityEntry("log123", 250, "2024-01-01", "2024-01-01T10:00:00Z", "10:00")
        val recentWaterActivity = RecentWaterActivity("user123", listOf(recentWaterActivityEntry), 1)
        val waterTrendEntry = WaterTrendEntry("2024-01-01", 1000, 4)
        val waterTrends = WaterTrends("user123", "2024-01-01", "2024-01-31", "daily", listOf(waterTrendEntry), 31)

        // Response models
        val waterLogResponse = WaterLogResponse(true, "Success", waterLog)
        val waterLogsResponse = WaterLogsResponse(true, "Success", listOf(waterLog))
        val dailyWaterTotalResponse = DailyWaterTotalResponse(true, "Success", dailyWaterTotal)
        val dailyWaterSummaryResponse = DailyWaterSummaryResponse(true, "Success", dailyWaterSummary)
        val monthlyWaterSummaryResponse = MonthlyWaterSummaryResponse(true, "Success", monthlyWaterSummary)
        val recentWaterActivityResponse = RecentWaterActivityResponse(true, "Success", recentWaterActivity)
        val waterTrendsResponse = WaterTrendsResponse(true, "Success", waterTrends)

        // Test request models
        assertEquals(createWaterLogRequest.userId, "user123")
        assertEquals(250.0, createWaterLogRequest.amount, 0.001)
        assertEquals(createWaterLogRequest.date, "2024-01-01")

        assertEquals(300.0, updateWaterLogRequest.amount ?: 0.0, 0.001)
        assertEquals(updateWaterLogRequest.date, "2024-01-02")

        // Test data classes
        assertEquals(waterLog.id, "log123")
        assertEquals(waterLog.userId, "user123")
        assertEquals(250.0, waterLog.amount, 0.001)
        assertEquals(waterLog.date, "2024-01-01")

        assertEquals(dailyWaterTotal.userId, "user123")
        assertEquals(dailyWaterTotal.date, "2024-01-01")
        assertEquals(1000.0, dailyWaterTotal.totalAmount, 0.001)
        assertEquals(dailyWaterTotal.entries, 4)

        assertEquals(dailyWaterSummary.userId, "user123")
        assertEquals(dailyWaterSummary.date, "2024-01-01")
        assertEquals(1000.0, dailyWaterSummary.totalAmount, 0.001)
        assertEquals(dailyWaterSummary.totalEntries, 4)
        assertEquals(250.0, dailyWaterSummary.averagePerEntry, 0.001)
        assertEquals(dailyWaterSummary.goal, 2000)
        assertEquals(1000.0, dailyWaterSummary.remaining, 0.001)
        assertEquals(dailyWaterSummary.goalPercentage, 50)

        assertEquals(monthlyWaterSummary.userId, "user123")
        assertEquals(monthlyWaterSummary.year, 2024)
        assertEquals(monthlyWaterSummary.month, 1)
        assertEquals(30000.0, monthlyWaterSummary.totalAmount, 0.001)
        assertEquals(monthlyWaterSummary.totalEntries, 120)
        assertEquals(1000.0, monthlyWaterSummary.averageDailyAmount, 0.001)
        assertEquals(monthlyWaterSummary.daysWithData, 30)
        assertEquals(monthlyWaterSummary.dailyGoal, 2000)
        assertEquals(monthlyWaterSummary.goalAchievement, 50)

        // Test response models
        assertEquals(waterLogResponse.success, true)
        assertEquals(waterLogResponse.message, "Success")
        assertEquals(waterLogResponse.data.id, "log123")

        assertEquals(waterLogsResponse.success, true)
        assertEquals(waterLogsResponse.data.size, 1)

        assertEquals(dailyWaterTotalResponse.success, true)
        assertEquals(1000.0, dailyWaterTotalResponse.data.totalAmount, 0.001)

        assertEquals(dailyWaterSummaryResponse.success, true)
        assertEquals(1000.0, dailyWaterSummaryResponse.data.totalAmount, 0.001)

        assertEquals(monthlyWaterSummaryResponse.success, true)
        assertEquals(30000.0, monthlyWaterSummaryResponse.data.totalAmount, 0.001)

        assertEquals(recentWaterActivityResponse.success, true)
        assertEquals(recentWaterActivityResponse.data.count, 1)

        assertEquals(waterTrendsResponse.success, true)
        assertEquals(waterTrendsResponse.data.totalDays, 31)
    }
}