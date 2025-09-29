package com.example.forkit.data.models

// Calorie Calculator Models for Calorie Calculator Controller

// Request Models
data class CalculateCaloriesRequest(
    val carbs: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null
)

data class CalculateFoodCaloriesRequest(
    val calories: Double? = null,
    val carbs: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null
)

data class CalculateIndividualCaloriesRequest(
    val macronutrient: String, // carbs, protein, or fat
    val grams: Double
)

// Response Models
data class CalculateCaloriesResponse(
    val success: Boolean,
    val message: String,
    val data: CalorieCalculationResult
)

data class CalculateFoodCaloriesResponse(
    val success: Boolean,
    val message: String,
    val data: FoodCalorieCalculationResult
)

data class MacronutrientValuesResponse(
    val success: Boolean,
    val message: String,
    val data: MacronutrientValues
)

data class CalculateIndividualCaloriesResponse(
    val success: Boolean,
    val message: String,
    val data: IndividualCalorieCalculation
)

// Data Classes
data class CalorieCalculationResult(
    val totalCalories: Double,
    val breakdown: MacronutrientBreakdown,
    val macronutrientCalories: MacronutrientCalories
)

data class MacronutrientBreakdown(
    val carbs: MacronutrientDetail,
    val protein: MacronutrientDetail,
    val fat: MacronutrientDetail
)

data class MacronutrientDetail(
    val grams: Double,
    val calories: Double
)

data class MacronutrientCalories(
    val carbs: Double,
    val protein: Double,
    val fat: Double
)

data class FoodCalorieCalculationResult(
    val totalCalories: Double,
    val calculatedFromMacronutrients: Boolean,
    val breakdown: MacronutrientBreakdown,
    val validation: ValidationResult,
    val providedCalories: Double? = null
)

data class ValidationResult(
    val isValid: Boolean,
    val message: String
)

data class MacronutrientValues(
    val macronutrientCalories: Map<String, Int>,
    val description: Map<String, String>
)

data class IndividualCalorieCalculation(
    val macronutrient: String,
    val grams: Double,
    val calories: Double,
    val caloriesPerGram: Int
)