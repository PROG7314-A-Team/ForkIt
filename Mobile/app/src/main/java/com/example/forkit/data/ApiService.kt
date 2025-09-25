package com.example.forkit.data

import com.example.forkit.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/users/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>
    
    // Login User
    @POST("api/users/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    // Get user by id
    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") userId: String): Response<GetUserResponse>
    
    // Get user streak
    @GET("api/users/{id}/streak")
    suspend fun getUserStreak(@Path("id") userId: String): Response<GetUserStreakResponse>

    // Update user
    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") userId: String, @Body request: UpdateUserRequest): Response<UpdateUserResponse>
    
    // Delete user
    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") userId: String): Response<DeleteUserResponse>

    // == FOOD ENDPOINTS ==
    // Get food from barcode
    @GET("api/food/barcode/{code}")
    suspend fun getFoodFromBarcode(@Path("code") barcode: String): Response<GetFoodFromBarcodeResponse>
    
    // Get food from name
    @GET("api/food/{name}")
    suspend fun getFoodFromName(@Path("name") name: String): Response<GetFoodFromNameResponse>

    // Create food
    @POST("api/food")
    suspend fun createFood(@Body request: CreateFoodRequest): Response<CreateFoodResponse>
    
    // Update food
    @PUT("api/food/{id}")
    suspend fun updateFood(@Path("id") foodId: String, @Body request: UpdateFoodRequest): Response<UpdateFoodResponse>
    
    // Delete food
    @DELETE("api/food/{id}")
// ==================== FOOD MANAGEMENT ====================
    
    // Get Food by Barcode
    @GET("api/food/barcode/{code}")
    suspend fun getFoodByBarcode(@Path("code") code: String): Response<FoodResponse>
    
    // Get Food by Name
    @GET("api/food/{name}")
    suspend fun getFoodByName(@Path("name") name: String): Response<FoodSearchResponse>
    
    // Create Food
    @POST("api/food")
    suspend fun createFood(@Body request: CreateFoodRequest): Response<FoodResponse>
    
    // Update Food
    @PUT("api/food/{id}")
    suspend fun updateFood(@Path("id") id: String, @Body request: UpdateFoodRequest): Response<FoodResponse>
    
    // Delete Food
    @DELETE("api/food/{id}")
    suspend fun deleteFood(@Path("id") id: String): Response<FoodResponse>
    
    // ==================== FOOD LOGGING ====================
    
    // Get Food Logs
    @GET("api/food-logs")
    suspend fun getFoodLogs(
        @Query("userId") userId: String? = null,
        @Query("date") date: String? = null
    ): Response<FoodLogsResponse>
    
    // Get Food Logs by Date Range
    @GET("api/food-logs/date-range")
    suspend fun getFoodLogsByDateRange(
        @Query("userId") userId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<FoodLogsResponse>
    
    // Get Daily Calorie Summary
    @GET("api/food-logs/daily-summary")
    suspend fun getDailyCalorieSummary(
        @Query("userId") userId: String,
        @Query("date") date: String
    ): Response<DailyCalorieSummaryResponse>
    
    // Get Monthly Calorie Summary
    @GET("api/food-logs/monthly-summary")
    suspend fun getMonthlyCalorieSummary(
        @Query("userId") userId: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<MonthlyCalorieSummaryResponse>
    
    // Get Recent Food Activity
    @GET("api/food-logs/recent-activity")
    suspend fun getRecentFoodActivity(
        @Query("userId") userId: String,
        @Query("limit") limit: Int = 10
    ): Response<RecentFoodActivityResponse>
    
    // Get Calorie Trends
    @GET("api/food-logs/trends")
    suspend fun getCalorieTrends(
        @Query("userId") userId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("groupBy") groupBy: String = "day"
    ): Response<CalorieTrendsResponse>
    
    // Get Food Log Dashboard
    @GET("api/food-logs/dashboard")
    suspend fun getFoodLogDashboard(
        @Query("userId") userId: String,
        @Query("date") date: String? = null,
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null
    ): Response<FoodLogDashboardResponse>
    
    // Get Food Log by ID
    @GET("api/food-logs/{id}")
    suspend fun getFoodLogById(@Path("id") id: String): Response<FoodLogResponse>
    
    // Create Food Log
    @POST("api/food-logs")
    suspend fun createFoodLog(@Body request: CreateFoodLogRequest): Response<FoodLogResponse>
    
    // Update Food Log
    @PUT("api/food-logs/{id}")
    suspend fun updateFoodLog(@Path("id") id: String, @Body request: UpdateFoodLogRequest): Response<FoodLogResponse>
    
    // Delete Food Log
    @DELETE("api/food-logs/{id}")
    suspend fun deleteFoodLog(@Path("id") id: String): Response<FoodLogResponse>
    
    // ==================== MEAL LOGGING ====================
    
    // Get Meal Logs
    @GET("api/meal-logs")
    suspend fun getMealLogs(
        @Query("userId") userId: String? = null,
        @Query("date") date: String? = null
    ): Response<MealLogsResponse>
    
    // Get Meal Logs by Date Range
    @GET("api/meal-logs/date-range")
    suspend fun getMealLogsByDateRange(
        @Query("userId") userId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<MealLogsResponse>
    
    // Get Meal Log by ID
    @GET("api/meal-logs/{id}")
    suspend fun getMealLogById(@Path("id") id: String): Response<MealLogResponse>
    
    // Create Meal Log
    @POST("api/meal-logs")
    suspend fun createMealLog(@Body request: CreateMealLogRequest): Response<MealLogResponse>
    
    // Update Meal Log
    @PUT("api/meal-logs/{id}")
    suspend fun updateMealLog(@Path("id") id: String, @Body request: UpdateMealLogRequest): Response<MealLogResponse>
    
    // Delete Meal Log
    @DELETE("api/meal-logs/{id}")
    suspend fun deleteMealLog(@Path("id") id: String): Response<MealLogResponse>
    
    // ==================== WATER LOGGING ====================
    
    // Get Water Logs
    @GET("api/water-logs")
    suspend fun getWaterLogs(
        @Query("userId") userId: String? = null,
        @Query("date") date: String? = null
    ): Response<WaterLogsResponse>
    
    // Get Water Logs by Date Range
    @GET("api/water-logs/date-range")
    suspend fun getWaterLogsByDateRange(
        @Query("userId") userId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<WaterLogsResponse>
    
    // Get Daily Water Total
    @GET("api/water-logs/daily-total")
    suspend fun getDailyWaterTotal(
        @Query("userId") userId: String,
        @Query("date") date: String
    ): Response<DailyWaterTotalResponse>
    
    // Get Daily Water Summary
    @GET("api/water-logs/daily-summary")
    suspend fun getDailyWaterSummary(
        @Query("userId") userId: String,
        @Query("date") date: String
    ): Response<DailyWaterSummaryResponse>
    
    // Get Monthly Water Summary
    @GET("api/water-logs/monthly-summary")
    suspend fun getMonthlyWaterSummary(
        @Query("userId") userId: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<MonthlyWaterSummaryResponse>
    
    // Get Recent Water Activity
    @GET("api/water-logs/recent-activity")
    suspend fun getRecentWaterActivity(
        @Query("userId") userId: String,
        @Query("limit") limit: Int = 10
    ): Response<RecentWaterActivityResponse>
    
    // Get Water Trends
    @GET("api/water-logs/trends")
    suspend fun getWaterTrends(
        @Query("userId") userId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("groupBy") groupBy: String = "day"
    ): Response<WaterTrendsResponse>
    
    // Get Water Dashboard
    @GET("api/water-logs/dashboard")
    suspend fun getWaterDashboard(
        @Query("userId") userId: String,
        @Query("date") date: String? = null,
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null
    ): Response<WaterDashboardResponse>
    
    // Get Water Log by ID
    @GET("api/water-logs/{id}")
    suspend fun getWaterLogById(@Path("id") id: String): Response<WaterLogResponse>
    
    // Create Water Log
    @POST("api/water-logs")
    suspend fun createWaterLog(@Body request: CreateWaterLogRequest): Response<WaterLogResponse>
    
    // Update Water Log
    @PUT("api/water-logs/{id}")
    suspend fun updateWaterLog(@Path("id") id: String, @Body request: UpdateWaterLogRequest): Response<WaterLogResponse>
    
    // Delete Water Log
    @DELETE("api/water-logs/{id}")
    suspend fun deleteWaterLog(@Path("id") id: String): Response<WaterLogResponse>
    
    // ==================== EXERCISE LOGGING ====================
    
    // Get Exercise Logs
    @GET("api/exercise-logs")
    suspend fun getExerciseLogs(
        @Query("userId") userId: String? = null,
        @Query("date") date: String? = null,
        @Query("type") type: String? = null
    ): Response<ExerciseLogsResponse>
    
    // Get Exercise Logs by Date Range
    @GET("api/exercise-logs/date-range")
    suspend fun getExerciseLogsByDateRange(
        @Query("userId") userId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<ExerciseLogsResponse>
    
    // Get Daily Exercise Total
    @GET("api/exercise-logs/daily-total")
    suspend fun getDailyExerciseTotal(
        @Query("userId") userId: String,
        @Query("date") date: String
    ): Response<DailyExerciseTotalResponse>
    
    // Get Daily Exercise Summary
    @GET("api/exercise-logs/daily-summary")
    suspend fun getDailyExerciseSummary(
        @Query("userId") userId: String,
        @Query("date") date: String
    ): Response<DailyExerciseSummaryResponse>
    
    // Get Monthly Exercise Summary
    @GET("api/exercise-logs/monthly-summary")
    suspend fun getMonthlyExerciseSummary(
        @Query("userId") userId: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<MonthlyExerciseSummaryResponse>
    
    // Get Recent Exercise Activity
    @GET("api/exercise-logs/recent-activity")
    suspend fun getRecentExerciseActivity(
        @Query("userId") userId: String,
        @Query("limit") limit: Int = 10
    ): Response<RecentExerciseActivityResponse>
    
    // Get Exercise Trends
    @GET("api/exercise-logs/trends")
    suspend fun getExerciseTrends(
        @Query("userId") userId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("groupBy") groupBy: String = "day"
    ): Response<ExerciseTrendsResponse>
    
    // Get Exercise Dashboard
    @GET("api/exercise-logs/dashboard")
    suspend fun getExerciseDashboard(
        @Query("userId") userId: String,
        @Query("date") date: String? = null,
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null
    ): Response<ExerciseDashboardResponse>
    
    // Get Exercise Log by ID
    @GET("api/exercise-logs/{id}")
    suspend fun getExerciseLogById(@Path("id") id: String): Response<ExerciseLogResponse>
    
    // Create Exercise Log
    @POST("api/exercise-logs")
    suspend fun createExerciseLog(@Body request: CreateExerciseLogRequest): Response<ExerciseLogResponse>
    
    // Update Exercise Log
    @PUT("api/exercise-logs/{id}")
    suspend fun updateExerciseLog(@Path("id") id: String, @Body request: UpdateExerciseLogRequest): Response<ExerciseLogResponse>
    
    // Delete Exercise Log
    @DELETE("api/exercise-logs/{id}")
    suspend fun deleteExerciseLog(@Path("id") id: String): Response<ExerciseLogResponse>
    
    // ==================== CALORIE CALCULATOR ====================
    
    // Get Macronutrient Values
    @GET("api/calorie-calculator/macronutrient-values")
    suspend fun getMacronutrientValues(): Response<MacronutrientValuesResponse>
    
    // Calculate Calories
    @POST("api/calorie-calculator/calculate")
    suspend fun calculateCalories(@Body request: CalculateCaloriesRequest): Response<CalculateCaloriesResponse>
    
    // Calculate Food Calories
    @POST("api/calorie-calculator/food-calories")
    suspend fun calculateFoodCalories(@Body request: CalculateFoodCaloriesRequest): Response<CalculateFoodCaloriesResponse>
    
    // Calculate Individual Calories
    @POST("api/calorie-calculator/individual")
    suspend fun calculateIndividualCalories(@Body reques



}