package com.example.forkit.data.models

data class RegisterRequest(
    val email: String,
    val password: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: String
)

data class GoogleRegisterRequest(
    val email: String
)

data class GoogleRegisterResponse(
    val success: Boolean,
    val message: String,
    val userId: String? = null
)

data class GoogleLoginRequest(
    val email: String
)

data class GoogleLoginResponse(
    val success: Boolean,
    val message: String,
    val userId: String? = null
)
