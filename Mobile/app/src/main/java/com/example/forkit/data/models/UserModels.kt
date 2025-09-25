package com.example.forkit.data.models

data class StreakData(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastLogDate: String, // Can format as "yyyy-MM-dd"
    val streakStartDate: String,
    val isActive: Boolean
)

data class User(
    val userId: String, 
    val success: Boolean,
    val message: String,
    val email: String,
    val age: Int?,
    val height: Double?,
    val weight: Double?,
    val streakData: StreakData
)

data class GetUserResponse( 
    val success: Boolean,
    val message: String,
    val data: Any
)

data class GetUserStreakResponse( 
    val success: Boolean,
    val message: String,
    val data: StreakData
)

// Update user
data class UpdateUserRequest( 
    val userId: String,
    val userData: User
) 

data class UpdateUserResponse( 
    val success: Boolean,
    val message: String,
    val data: User
)

data class DeleteUserResponse( 
    val success: Boolean,
    val message: String,
    val data: User
)