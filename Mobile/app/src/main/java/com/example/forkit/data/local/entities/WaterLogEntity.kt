package com.example.forkit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey
    val localId: String = UUID.randomUUID().toString(),
    val serverId: String? = null,
    val userId: String,
    val amount: Double,
    val date: String,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

