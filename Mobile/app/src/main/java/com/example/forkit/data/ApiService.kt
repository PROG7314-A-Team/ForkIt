package com.example.forkit.data

import com.example.forkit.data.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path

interface ApiService {

    // == USER ENDPOINTS ==
    // Register user
    @POST("api/users/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    // Login user
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
    suspend fun deleteFood(@Path("id") foodId: String): Response<DeleteFoodResponse>
}