package com.example.forkit.data.repository

import android.util.Log
import com.example.forkit.data.ApiService
import com.example.forkit.data.local.dao.WaterLogDao
import com.example.forkit.data.local.entities.WaterLogEntity
import com.example.forkit.data.models.CreateWaterLogRequest
import com.example.forkit.utils.NetworkConnectivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WaterLogRepository(
    private val apiService: ApiService,
    private val waterLogDao: WaterLogDao,
    private val networkManager: NetworkConnectivityManager
) {
    
    /**
     * Create a water log entry. If online, save to API then to local DB.
     * If offline, save to local DB with isSynced=false.
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
            
            if (networkManager.isOnline()) {
                // Try to save to API
                try {
                    val request = CreateWaterLogRequest(
                        userId = userId,
                        amount = amount,
                        date = date
                    )
                    
                    val response = apiService.createWaterLog(request)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverId = response.body()?.data?.id
                        
                        // Save to local DB with sync flag
                        val syncedEntity = waterLogEntity.copy(
                            serverId = serverId,
                            isSynced = true
                        )
                        waterLogDao.insert(syncedEntity)
                        
                        Log.d("WaterLogRepository", "Water log created online and saved locally")
                        return@withContext Result.success(serverId ?: "")
                    } else {
                        // API call failed, save locally
                        waterLogDao.insert(waterLogEntity)
                        Log.w("WaterLogRepository", "API call failed, saved offline")
                        return@withContext Result.success("offline")
                    }
                } catch (e: Exception) {
                    // Network error, save locally
                    waterLogDao.insert(waterLogEntity)
                    Log.e("WaterLogRepository", "Network error, saved offline: ${e.message}")
                    return@withContext Result.success("offline")
                }
            } else {
                // Offline, save locally
                waterLogDao.insert(waterLogEntity)
                Log.d("WaterLogRepository", "Offline, saved locally")
                return@withContext Result.success("offline")
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
}

