package com.example.forkit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey
    val localId: String = UUID.randomUUID().toString(),
    val serverId: String? = null,
    val userId: String,
    val foodName: String,
    val servingSize: Double,
    val measuringUnit: String,
    val date: String,
    val mealType: String,
    val calories: Double,
    val carbs: Double,
    val fat: Double,
    val protein: Double,
    val foodId: String? = null,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

