package com.example.forkit.data.models

// -------------------------------------------------------------
// 🧠 Local model for a user-built full meal (not an API log)
// -------------------------------------------------------------
data class MealIngredient(
    val id: String,
    val foodName: String,
    val servingSize: Double,
    val measuringUnit: String,
    val calories: Double,
    val carbs: Double,
    val fat: Double,
    val protein: Double
)

// -------------------------------------------------------------
// 🥘 A locally composed full meal with its ingredient list
// -------------------------------------------------------------
data class FullMeal(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val ingredients: List<MealIngredient> = emptyList(),
    val totalCalories: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalProtein: Double = 0.0
)
