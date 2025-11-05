package com.example.forkit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "exercise_logs")
data class ExerciseLogEntity(
    @PrimaryKey
    val localId: String = UUID.randomUUID().toString(),
    val serverId: String? = null,
    val userId: String,
    val name: String,
    val date: String,
    val caloriesBurnt: Double,
    val type: String, // "Cardio" or "Strength"
    val duration: Double? = null,
    val notes: String = "",
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

