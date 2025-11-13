package com.example.forkit.data.repository

import android.util.Log
import com.example.forkit.data.ApiService
import com.example.forkit.data.local.dao.WaterLogDao
import com.example.forkit.data.local.entities.WaterLogEntity
import com.example.forkit.data.models.CreateWaterLogRequest
import com.example.forkit.data.models.WaterLog
import com.example.forkit.utils.NetworkConnectivityManager
import com.example.forkit.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

class WaterLogRepository(
    private val apiService: ApiService,
    private val waterLogDao: WaterLogDao,
    private val networkManager: NetworkConnectivityManager
) {
    
    /**
     * Create a water log entry using an online-first approach.
     */
    suspend fun createWaterLog(
        userId: String,
        amount: Double,
        date: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val waterLogEntity = WaterLogEntity(
                userId = userId,
                amount = amount,
                date = date,
                isSynced = false
            )
            
            val currentlyOnline = networkManager.isOnline()
            if (!currentlyOnline) {
                waterLogDao.insert(waterLogEntity)
                Log.i("WaterLogRepository", "Device offline, saved water log locally for later sync")
                return@withContext Result.success("offline")
            }

            try {
                val request = CreateWaterLogRequest(
                    userId = userId,
                    amount = amount,
                    date = date
                )

                val response = apiService.createWaterLog(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val serverLog = response.body()?.data
                    val syncedEntity = serverLog?.toEntity(waterLogEntity.localId) ?: waterLogEntity.copy(
                        serverId = response.body()?.data?.id,
                        isSynced = true
                    )
                    waterLogDao.insert(syncedEntity.copy(isSynced = true))

                    Log.d("WaterLogRepository", "Water log created online and saved locally")
                    return@withContext Result.success(syncedEntity.serverId ?: "")
                } else {
                    val message = response.errorBody()?.string() ?: response.message()
                    Log.w("WaterLogRepository", "API createWaterLog failed (${response.code()}): $message")
                    return@withContext Result.failure(Exception(message))
                }
            } catch (e: IOException) {
                waterLogDao.insert(waterLogEntity)
                Log.e("WaterLogRepository", "Network error, saved offline: ${e.message}", e)
                return@withContext Result.success("offline")
            } catch (e: Exception) {
                Log.e("WaterLogRepository", "API error creating water log: ${e.message}", e)
                return@withContext Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e("WaterLogRepository", "Error creating water log: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Get water logs for a specific date
     */
    suspend fun getWaterLogsByDate(userId: String, date: String): List<WaterLogEntity> = withContext(Dispatchers.IO) {
        waterLogDao.getByDate(userId, date)
    }
    
    /**
     * Delete a water log
     */
    suspend fun deleteWaterLog(localId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val waterLog = waterLogDao.getById(localId)
            
            if (waterLog != null) {
                // If synced and online, delete from API
                if (waterLog.isSynced && waterLog.serverId != null && networkManager.isOnline()) {
                    try {
                        apiService.deleteWaterLog(waterLog.serverId)
                    } catch (e: Exception) {
                        Log.w("WaterLogRepository", "Failed to delete from API: ${e.message}")
                    }
                }
                
                // Always delete locally
                waterLogDao.delete(waterLog)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Water log not found"))
            }
        } catch (e: Exception) {
            Log.e("WaterLogRepository", "Error deleting water log: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all unsynced water logs for syncing
     */
    suspend fun getUnsyncedLogs(): List<WaterLogEntity> = withContext(Dispatchers.IO) {
        waterLogDao.getUnsyncedLogs()
    }
    
    /**
     * Mark a water log as synced
     */
    suspend fun markAsSynced(localId: String, serverId: String) = withContext(Dispatchers.IO) {
        waterLogDao.markAsSynced(localId, serverId)
    }

    suspend fun refreshUserWaterLogs(userId: String) = withContext(Dispatchers.IO) {
        if (!networkManager.isOnline()) return@withContext
        try {
            val response = apiService.getWaterLogs(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                val remoteLogs = response.body()?.data.orEmpty()
                val unsynced = waterLogDao.getUnsyncedLogs()
                val entities = remoteLogs.map { log ->
                    val existing = waterLogDao.getByServerId(log.id)
                    log.toEntity(existing?.localId)
                }
                if (entities.isNotEmpty()) {
                    entities.forEach { waterLogDao.insert(it.copy(isSynced = true)) }
                }
                unsynced.forEach { waterLogDao.insert(it) }
                Log.d("WaterLogRepository", "Refreshed ${entities.size} water logs from server")
            } else {
                Log.w("WaterLogRepository", "Failed to refresh water logs: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.w("WaterLogRepository", "Error refreshing water logs: ${e.message}")
        }
    }

    private fun WaterLog.toEntity(existingLocalId: String? = null): WaterLogEntity {
        val localIdentifier = existingLocalId ?: UUID.randomUUID().toString()
        return WaterLogEntity(
            localId = localIdentifier,
            serverId = id,
            userId = userId,
            amount = amount,
            date = date,
            isSynced = true,
            createdAt = TimeUtils.parseIsoToMillis(createdAt)
        )
    }
}

