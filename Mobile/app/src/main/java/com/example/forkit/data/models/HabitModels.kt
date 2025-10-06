package com.example.forkit.data.models

import java.time.LocalDate
import java.time.LocalDateTime

data class Habit(
    val id: String,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val completedAt: String? = null, // ISO string from API
    val createdAt: String, // ISO string from API
    val category: String = "GENERAL", // String from API
    val frequency: String = "DAILY", // String from API
    val selectedDays: List<Int>? = null, // For weekly habits (0-6, Sunday-Saturday)
    val dayOfMonth: Int? = null, // For monthly habits (1-31)
    val userId: String? = null
)

enum class HabitCategory {
    NUTRITION,
    EXERCISE,
    HEALTH,
    GENERAL
}

enum class HabitFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}

data class HabitCompletion(
    val habitId: String,
    val completedAt: LocalDateTime,
    val date: LocalDate
)

// API Request Models
data class CreateHabitRequest(
    val title: String,
    val description: String? = null,
    val category: String = "GENERAL",
    val frequency: String = "DAILY",
    val selectedDays: List<Int>? = null, // For weekly habits (0-6, Sunday-Saturday)
    val dayOfMonth: Int? = null // For monthly habits (1-31)
)

data class CreateHabitApiRequest(
    val userId: String,
    val habit: CreateHabitRequest
)

data class UpdateHabitRequest(
    val title: String? = null,
    val description: String? = null,
    val isCompleted: Boolean? = null,
    val category: HabitCategory? = null,
    val frequency: HabitFrequency? = null
)

// API Response Models
data class HabitResponse(
    val success: Boolean,
    val message: String,
    val data: Habit? = null
)

data class HabitsResponse(
    val success: Boolean,
    val message: String,
    val data: List<Habit> = emptyList()
)

// Mock data for testing
object MockHabits {
    fun getTodayHabits(): List<Habit> = listOf(
        Habit(
            id = "1",
            title = "Eat 250g Steak",
            description = "High protein meal for muscle building",
            isCompleted = false,
            createdAt = java.time.Instant.now().toString(),
            category = "NUTRITION"
        ),
        Habit(
            id = "2", 
            title = "Eat 2 Fried Eggs",
            description = "Healthy breakfast protein",
            isCompleted = false,
            createdAt = java.time.Instant.now().toString(),
            category = "NUTRITION"
        ),
        Habit(
            id = "3",
            title = "Eat 1 Cup of Rice",
            description = "Carbohydrate source",
            isCompleted = true,
            completedAt = java.time.Instant.now().minusSeconds(7200).toString(),
            createdAt = java.time.Instant.now().toString(),
            category = "NUTRITION"
        ),
        Habit(
            id = "4",
            title = "Have 1 Vitamin Pill",
            description = "Daily vitamin supplement",
            isCompleted = true,
            completedAt = java.time.Instant.now().minusSeconds(3600).toString(),
            createdAt = java.time.Instant.now().toString(),
            category = "HEALTH"
        )
    )
    
    fun getWeeklyHabits(): List<Habit> = listOf(
        Habit(
            id = "5",
            title = "Exercise 3 times this week",
            description = "Maintain fitness routine",
            isCompleted = false,
            createdAt = java.time.Instant.now().toString(),
            category = "EXERCISE",
            frequency = "WEEKLY"
        ),
        Habit(
            id = "6",
            title = "Drink 8 glasses of water daily",
            description = "Stay hydrated",
            isCompleted = true,
            completedAt = java.time.Instant.now().minusSeconds(86400).toString(),
            createdAt = java.time.Instant.now().toString(),
            category = "HEALTH"
        )
    )
    
    fun getMonthlyHabits(): List<Habit> = listOf(
        Habit(
            id = "7",
            title = "Complete monthly health checkup",
            description = "Regular health monitoring",
            isCompleted = false,
            createdAt = java.time.Instant.now().toString(),
            category = "HEALTH",
            frequency = "MONTHLY"
        ),
        Habit(
            id = "8",
            title = "Review nutrition goals",
            description = "Monthly nutrition assessment",
            isCompleted = true,
            completedAt = java.time.Instant.now().minusSeconds(432000).toString(),
            createdAt = java.time.Instant.now().toString(),
            category = "NUTRITION",
            frequency = "MONTHLY"
        )
    )
}
