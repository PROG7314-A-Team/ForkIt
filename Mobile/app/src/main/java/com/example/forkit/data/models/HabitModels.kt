package com.example.forkit.data.models

import java.time.LocalDate
import java.time.LocalDateTime

data class Habit(
    val id: String,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val category: HabitCategory = HabitCategory.GENERAL,
    val frequency: HabitFrequency = HabitFrequency.DAILY
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
    val category: HabitCategory = HabitCategory.GENERAL,
    val frequency: HabitFrequency = HabitFrequency.DAILY
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
            category = HabitCategory.NUTRITION
        ),
        Habit(
            id = "2", 
            title = "Eat 2 Fried Eggs",
            description = "Healthy breakfast protein",
            isCompleted = false,
            category = HabitCategory.NUTRITION
        ),
        Habit(
            id = "3",
            title = "Eat 1 Cup of Rice",
            description = "Carbohydrate source",
            isCompleted = true,
            completedAt = LocalDateTime.now().minusHours(2),
            category = HabitCategory.NUTRITION
        ),
        Habit(
            id = "4",
            title = "Have 1 Vitamin Pill",
            description = "Daily vitamin supplement",
            isCompleted = true,
            completedAt = LocalDateTime.now().minusHours(1),
            category = HabitCategory.HEALTH
        )
    )
    
    fun getWeeklyHabits(): List<Habit> = listOf(
        Habit(
            id = "5",
            title = "Exercise 3 times this week",
            description = "Maintain fitness routine",
            isCompleted = false,
            category = HabitCategory.EXERCISE,
            frequency = HabitFrequency.WEEKLY
        ),
        Habit(
            id = "6",
            title = "Drink 8 glasses of water daily",
            description = "Stay hydrated",
            isCompleted = true,
            completedAt = LocalDateTime.now().minusDays(1),
            category = HabitCategory.HEALTH
        )
    )
    
    fun getMonthlyHabits(): List<Habit> = listOf(
        Habit(
            id = "7",
            title = "Complete monthly health checkup",
            description = "Regular health monitoring",
            isCompleted = false,
            category = HabitCategory.HEALTH,
            frequency = HabitFrequency.MONTHLY
        ),
        Habit(
            id = "8",
            title = "Review nutrition goals",
            description = "Monthly nutrition assessment",
            isCompleted = true,
            completedAt = LocalDateTime.now().minusDays(5),
            category = HabitCategory.NUTRITION,
            frequency = HabitFrequency.MONTHLY
        )
    )
}
