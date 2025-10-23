package com.example.forkit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val localId: String = UUID.randomUUID().toString(),
    val serverId: String? = null,
    val userId: String,
    val title: String,
    val description: String? = null,
    val frequency: String, // "daily", "weekly", "monthly"
    val isCompleted: Boolean = false,
    val completedAt: String? = null,
    val notificationsEnabled: Boolean = false,
    val notificationTime: String? = null,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false, // Soft delete flag
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

