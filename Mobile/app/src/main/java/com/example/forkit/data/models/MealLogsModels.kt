package com.example.forkit.data.models

// Meal Logs Models for Meal Logging Controller

// Request Models
data class CreateMealLogRequest(
    val userId: String,
    val name: String,
    val description: String? = null,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val totalCalories: Double? = null,
    val totalCarbs: Double? = null,
    val totalFat: Double? = null,
    val totalProtein: Double? = null,
    val servings: Double? = null,
    val date: String,
    val mealType: String? = null,
    val isTemplate: Boolean = false,
    val templateId: String? = null
)

data class UpdateMealLogRequest(
    val name: String? = null,
    val description: String? = null,
    val ingredients: List<Ingredient>? = null,
    val instructions: List<String>? = null,
    val totalCalories: Double? = null,
    val totalCarbs: Double? = null,
    val totalFat: Double? = null,
    val totalProtein: Double? = null,
    val servings: Double? = null,
    val date: String? = null,
    val mealType: String? = null
)

// Response Models
data class MealLogResponse(
    val success: Boolean,
    val message: String,
    val data: MealLog
)

data class MealLogsResponse(
    val success: Boolean,
    val message: String,
    val data: List<MealLog>
)

// Data Classes
data class Ingredient(
    val name: String,
    val amount: Double,
    val unit: String
)

data class MealLog(
    val id: String,
    val userId: String,
    val name: String,
    val description: String,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val totalCalories: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalProtein: Double,
    val servings: Double,
    val date: String,
    val mealType: String?,
    val isTemplate: Boolean = false,
    val templateId: String? = null,
    val createdAt: String,
    val updatedAt: String
)
