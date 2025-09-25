package com.example.forkit.data.models

data class Nutrients(
    val carbs: Double,
    val protein: Double,
    val fat: Double,
    val fiber: Double,
    val sugar: Double,
)

data class Food(
    val id: String,
    val name: String,
    val brand: String,
    val barcode: String,
    val calories: Double,
    val nutrients: Nutrients,
    val image: String,
    val ingredients: String
)

// Get food from barcode
data class GetFoodFromBarcodeResponse(
    val success: Boolean,
    val message: String,
    val data: Food
)

data class GetFoodFromNameResponse(
    val success: Boolean,
    val message: String,
    val data: Any
)

// Create food 
data class CreateFoodRequest(
    val foodData: Food
)

data class CreateFoodResponse(
    val success: Boolean,
    val message: String,
    val data: Food
)

// Update food
data class UpdateFoodRequest(
    val foodData: Food
)

data class UpdateFoodResponse(
    val success: Boolean,
    val message: String,
    val data: Food
)

// Delete food
data class DeleteFoodResponse(
    val success: Boolean,
    val message: String,
    val data: Food
)
