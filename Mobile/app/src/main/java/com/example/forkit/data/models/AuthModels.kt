package com.example.forkit.data.models

data class RegisterRequest(
    val email: String,
    val password: String
)

data class RegisterResponse(
    val message: String,
    val uid: String,
    val email: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val userId: String,
    val idToken: String,
    val refreshToken: String,
    val expiresIn: String
)