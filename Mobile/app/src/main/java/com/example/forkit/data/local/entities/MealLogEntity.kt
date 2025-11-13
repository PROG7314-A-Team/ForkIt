package com.example.forkit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.forkit.data.local.converters.Converters
import java.util.UUID

@Entity(tableName = "meal_logs")
@TypeConverters(Converters::class)
data class MealLogEntity(
    @PrimaryKey
    val localId: String = UUID.randomUUID().toString(),
    val serverId: String? = null,
    val userId: String,
    val name: String,
    val description: String,
    val ingredients: List<MealIngredient>,
    val instructions: List<String>,
    val totalCalories: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalProtein: Double,
    val servings: Double,
    val date: String,
    val mealType: String?,
    val isTemplate: Boolean = false, // NEW: true for saved meals, false for logged meals
    val templateId: String? = null,  // NEW: reference to original template if this is a logged meal
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class MealIngredient(
    val foodName: String,
    val quantity: Double,
    val unit: String,
    val calories: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val protein: Double = 0.0
)

