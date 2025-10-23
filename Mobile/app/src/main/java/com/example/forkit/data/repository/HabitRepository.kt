package com.example.forkit.data.repository

import android.util.Log
import com.example.forkit.data.ApiService
import com.example.forkit.data.local.dao.HabitDao
import com.example.forkit.data.local.entities.HabitEntity
import com.example.forkit.data.models.CreateHabitApiRequest
import com.example.forkit.data.models.CreateHabitRequest
import com.example.forkit.data.models.UpdateHabitRequest
import com.example.forkit.utils.NetworkConnectivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow

class HabitRepository(
    private val apiService: ApiService,
    private val habitDao: HabitDao,
    private val networkManager: NetworkConnectivityManager
) {
    
    /**
     * Create a habit. If online, save to API then to local DB.
     * If offline, save to local DB with isSynced=false.
     */
    suspend fun createHabit(
        userId: String,
        title: String,
        description: String?,
        frequency: String,
        notificationsEnabled: Boolean,
        notificationTime: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val habitEntity = HabitEntity(
                userId = userId,
                title = title,
                description = description,
                frequency = frequency,
                notificationsEnabled = notificationsEnabled,
                notificationTime = notificationTime,
                isSynced = false
            )
            
            if (networkManager.isOnline()) {
                try {
                    val habitRequest = CreateHabitRequest(
                        title = title,
                        description = description,
                        frequency = frequency
                    )
                    
                    val request = CreateHabitApiRequest(
                        userId = userId,
                        habit = habitRequest
                    )
                    
                    val response = apiService.createHabit(request)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverId = response.body()?.data?.id
                        
                        val syncedEntity = habitEntity.copy(
                            serverId = serverId,
                            isSynced = true
                        )
                        habitDao.insert(syncedEntity)
                        
                        Log.d("HabitRepository", "Habit created online and saved locally")
                        return@withContext Result.success(serverId ?: "")
                    } else {
                        habitDao.insert(habitEntity)
                        Log.w("HabitRepository", "API call failed, saved offline")
                        return@withContext Result.success("offline")
                    }
                } catch (e: Exception) {
                    habitDao.insert(habitEntity)
                    Log.e("HabitRepository", "Network error, saved offline: ${e.message}")
                    return@withContext Result.success("offline")
                }
            } else {
                habitDao.insert(habitEntity)
                Log.d("HabitRepository", "Offline, saved locally")
                return@withContext Result.success("offline")
            }
        } catch (e: Exception) {
            Log.e("HabitRepository", "Error creating habit: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Update a habit (e.g., mark as completed)
     */
    suspend fun updateHabit(
        localId: String,
        isCompleted: Boolean? = null,
        completedAt: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val habit = habitDao.getById(localId) ?: return@withContext Result.failure(Exception("Habit not found"))
            
            val updatedHabit = habit.copy(
                isCompleted = isCompleted ?: habit.isCompleted,
                completedAt = completedAt,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            )
            
            habitDao.update(updatedHabit)
            
            // If online and synced before, try to update API
            if (networkManager.isOnline() && habit.serverId != null) {
                try {
                    val request = UpdateHabitRequest(
                        isCompleted = updatedHabit.isCompleted
                    )
                    
                    val response = apiService.updateHabit(habit.serverId, request)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        habitDao.update(updatedHabit.copy(isSynced = true))
                        Log.d("HabitRepository", "Habit updated online")
                    }
                } catch (e: Exception) {
                    Log.e("HabitRepository", "Failed to update online: ${e.message}")
                }
            }
            
            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            Log.e("HabitRepository", "Error updating habit: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Delete a habit
     */
    suspend fun deleteHabit(localId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val habit = habitDao.getById(localId)
            
            if (habit != null) {
                // Mark as deleted locally (soft delete)
                habitDao.markAsDeleted(habit.localId)
                
                // If synced and online, delete from API
                if (habit.serverId != null && networkManager.isOnline()) {
                    try {
                        apiService.deleteHabit(habit.serverId)
                        // Hard delete after successful API delete
                        habitDao.delete(habit)
                    } catch (e: Exception) {
                        Log.w("HabitRepository", "Failed to delete from API, will sync later: ${e.message}")
                    }
                }
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("Habit not found"))
            }
        } catch (e: Exception) {
            Log.e("HabitRepository", "Error deleting habit: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all habits as Flow for real-time updates
     */
    fun getHabits(userId: String): Flow<List<HabitEntity>> {
        return habitDao.getAllFlow(userId)
    }
    
    /**
     * Get habits by frequency
     */
    suspend fun getHabitsByFrequency(userId: String, frequency: String): List<HabitEntity> = withContext(Dispatchers.IO) {
        habitDao.getByFrequency(userId, frequency)
    }
    
    /**
     * Get all unsynced habits
     */
    suspend fun getUnsyncedHabits(): List<HabitEntity> = withContext(Dispatchers.IO) {
        habitDao.getUnsyncedHabits()
    }
    
    /**
     * Get unsynced deletes
     */
    suspend fun getUnsyncedDeletes(): List<HabitEntity> = withContext(Dispatchers.IO) {
        habitDao.getUnsyncedDeletes()
    }
    
    /**
     * Mark a habit as synced
     */
    suspend fun markAsSynced(localId: String, serverId: String) = withContext(Dispatchers.IO) {
        habitDao.markAsSynced(localId, serverId)
    }
}

