package com.example.forkit.data.repository

import android.util.Log
import com.example.forkit.data.ApiService
import com.example.forkit.data.local.dao.ExerciseLogDao
import com.example.forkit.data.local.entities.ExerciseLogEntity
import com.example.forkit.data.models.CreateExerciseLogRequest
import com.example.forkit.utils.NetworkConnectivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseLogRepository(
    private val apiService: ApiService,
    private val exerciseLogDao: ExerciseLogDao,
    private val networkManager: NetworkConnectivityManager
) {
    
    /**
     * Create an exercise log entry using an online-first approach.
     */
    suspend fun createExerciseLog(
        userId: String,
        name: String,
        date: String,
        caloriesBurnt: Double,
        type: String,
        duration: Double? = null,
        notes: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val exerciseLogEntity = ExerciseLogEntity(
                userId = userId,
                name = name,
                date = date,
                caloriesBurnt = caloriesBurnt,
                type = type,
                duration = duration,
                notes = notes,
                isSynced = false
            )
            
            // Online-first
            try {
                val request = CreateExerciseLogRequest(
                    userId = userId,
                    name = name,
                    date = date,
                    caloriesBurnt = caloriesBurnt,
                    type = type,
                    duration = duration,
                    notes = notes
                )

                val response = apiService.createExerciseLog(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val serverId = response.body()?.data?.id

                    val syncedEntity = exerciseLogEntity.copy(
                        serverId = serverId,
                        isSynced = true
                    )
                    exerciseLogDao.insert(syncedEntity)

                    Log.d("ExerciseLogRepository", "Exercise log created online and saved locally")
                    return@withContext Result.success(serverId ?: "")
                } else {
                    exerciseLogDao.insert(exerciseLogEntity)
                    Log.w(
                        "ExerciseLogRepository",
                        "API createExerciseLog failed (${response.code()} ${response.message()}), saved offline"
                    )
                    return@withContext Result.success("offline")
                }
            } catch (e: Exception) {
                exerciseLogDao.insert(exerciseLogEntity)
                Log.e("ExerciseLogRepository", "API error, saved offline: ${e.message}", e)
                return@withContext Result.success("offline")
            }
        } catch (e: Exception) {
            Log.e("ExerciseLogRepository", "Error creating exercise log: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Get exercise logs for a specific date
     */
    suspend fun getExerciseLogsByDate(userId: String, date: String): List<ExerciseLogEntity> = withContext(Dispatchers.IO) {
        exerciseLogDao.getByDate(userId, date)
    }
    
    /**
     * Delete an exercise log
     */
    suspend fun deleteExerciseLog(localId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val exerciseLog = exerciseLogDao.getById(localId)
            
            if (exerciseLog != null) {
                // If synced and online, delete from API
                if (exerciseLog.isSynced && exerciseLog.serverId != null && networkManager.isOnline()) {
                    try {
                        apiService.deleteExerciseLog(exerciseLog.serverId)
                    } catch (e: Exception) {
                        Log.w("ExerciseLogRepository", "Failed to delete from API: ${e.message}")
                    }
                }
                
                // Always delete locally
                exerciseLogDao.delete(exerciseLog)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Exercise log not found"))
            }
        } catch (e: Exception) {
            Log.e("ExerciseLogRepository", "Error deleting exercise log: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all unsynced exercise logs for syncing
     */
    suspend fun getUnsyncedLogs(): List<ExerciseLogEntity> = withContext(Dispatchers.IO) {
        exerciseLogDao.getUnsyncedLogs()
    }
    
    /**
     * Mark an exercise log as synced
     */
    suspend fun markAsSynced(localId: String, serverId: String) = withContext(Dispatchers.IO) {
        exerciseLogDao.markAsSynced(localId, serverId)
    }
}

