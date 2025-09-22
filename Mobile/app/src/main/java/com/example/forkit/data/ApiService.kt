package com.example.forkit.data

import com.example.forkit.data.models.*

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/users/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    // Login user
    @POST("api/users/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>




}