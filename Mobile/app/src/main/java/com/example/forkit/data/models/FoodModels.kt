package com.example.forkit.data.models

data class Nutrients(
    val carbs: Double,
    val protein: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
)

data class ServingSize(
    val size: String?,
    val quantity: Double?,
    val unit: String?,
    val original: String?,
    val apiQuantity: Any?, // Can be String or Double
    val apiUnit: String?
)

data class Food(
    val id: String,
    val name: String,
    val brand: String,
    val barcode: String,
    val calories: Double,
    val nutrients: Nutrients,
    val servingSize: ServingSize?,
    val nutrientsPerServing: Nutrients?,
    val caloriesPerServing: Double?,
    val image: String,
    val ingredients: String
)

// Get food from barcode
data class GetFoodFromBarcodeResponse(
    val success: Boolean,
    val message: String,
    val data: Food
)

data class SearchFoodItem(
    val name: String,
    val brand: String? = null,
    val image: String?,
    val nutrients: Nutrients,
    val calories: Double?,
    val servingSize: ServingSize?,
    val nutrientsPerServing: Nutrients?,
    val caloriesPerServing: Double?
)

data class GetFoodFromNameResponse(
    val success: Boolean,
    val message: String,
    //val data: Map<String, SearchFoodItem>
    val data: List<SearchFoodItem>
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
