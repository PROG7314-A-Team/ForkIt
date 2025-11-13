package com.example.forkit.data.repository

import android.util.Log
import com.example.forkit.data.ApiService
import com.example.forkit.data.local.dao.ExerciseLogDao
import com.example.forkit.data.local.entities.ExerciseLogEntity
import com.example.forkit.data.models.CreateExerciseLogRequest
import com.example.forkit.data.models.ExerciseLog
import com.example.forkit.utils.NetworkConnectivityManager
import com.example.forkit.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

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
            
            val currentlyOnline = networkManager.isOnline()
            if (!currentlyOnline) {
                exerciseLogDao.insert(exerciseLogEntity)
                Log.i("ExerciseLogRepository", "Device offline, saved exercise log locally for later sync")
                return@withContext Result.success("offline")
            }

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
                    val serverLog = response.body()?.data
                    val syncedEntity = serverLog?.toEntity(exerciseLogEntity.localId) ?: exerciseLogEntity.copy(
                        serverId = response.body()?.data?.id,
                        isSynced = true
                    )
                    exerciseLogDao.insert(syncedEntity.copy(isSynced = true))

                    Log.d("ExerciseLogRepository", "Exercise log created online and saved locally")
                    return@withContext Result.success(syncedEntity.serverId ?: "")
                } else {
                    val message = response.errorBody()?.string() ?: response.message()
                    Log.w("ExerciseLogRepository", "API createExerciseLog failed (${response.code()}): $message")
                    return@withContext Result.failure(Exception(message))
                }
            } catch (e: IOException) {
                exerciseLogDao.insert(exerciseLogEntity)
                Log.e("ExerciseLogRepository", "Network error, saved offline: ${e.message}", e)
                return@withContext Result.success("offline")
            } catch (e: Exception) {
                Log.e("ExerciseLogRepository", "API error creating exercise log: ${e.message}", e)
                return@withContext Result.failure(e)
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

    suspend fun refreshUserExerciseLogs(userId: String) = withContext(Dispatchers.IO) {
        if (!networkManager.isOnline()) return@withContext
        try {
            val response = apiService.getExerciseLogs(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                val remoteLogs = response.body()?.data.orEmpty()
                val unsynced = exerciseLogDao.getUnsyncedLogs()
                val entities = remoteLogs.map { log ->
                    val existing = exerciseLogDao.getByServerId(log.id)
                    log.toEntity(existing?.localId ?: UUID.randomUUID().toString()).copy(isSynced = true)
                }
                entities.forEach { exerciseLogDao.insert(it) }
                unsynced.forEach { exerciseLogDao.insert(it) }
                Log.d("ExerciseLogRepository", "Refreshed ${entities.size} exercise logs from server")
            } else {
                Log.w("ExerciseLogRepository", "Failed to refresh exercise logs: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.w("ExerciseLogRepository", "Error refreshing exercise logs: ${e.message}")
        }
    }

    private fun ExerciseLog.toEntity(localId: String): ExerciseLogEntity {
        return ExerciseLogEntity(
            localId = localId,
            serverId = id,
            userId = userId,
            name = name,
            date = date,
            caloriesBurnt = caloriesBurnt,
            type = type,
            duration = duration,
            notes = notes ?: "",
            isSynced = true,
            createdAt = TimeUtils.parseIsoToMillis(createdAt)
        )
    }
}

