package com.example.forkit.data.repository

import android.util.Log
import com.example.forkit.data.ApiService
import com.example.forkit.data.local.dao.FoodLogDao
import com.example.forkit.data.local.entities.FoodLogEntity
import com.example.forkit.data.models.CreateFoodLogRequest
import com.example.forkit.utils.NetworkConnectivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FoodLogRepository(
    private val apiService: ApiService,
    private val foodLogDao: FoodLogDao,
    private val networkManager: NetworkConnectivityManager
) {
    
    /**
     * Create a food log entry. If online, save to API then to local DB.
     * If offline, save to local DB with isSynced=false.
     */
    suspend fun createFoodLog(
        userId: String,
        foodName: String,
        servingSize: Double,
        measuringUnit: String,
        date: String,
        mealType: String,
        calories: Double,
        carbs: Double,
        fat: Double,
        protein: Double,
        foodId: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val foodLogEntity = FoodLogEntity(
                userId = userId,
                foodName = foodName,
                servingSize = servingSize,
                measuringUnit = measuringUnit,
                date = date,
                mealType = mealType,
                calories = calories,
                carbs = carbs,
                fat = fat,
                protein = protein,
                foodId = foodId,
                isSynced = false
            )
            
            if (networkManager.isOnline()) {
                // Try to save to API
                try {
                    val request = CreateFoodLogRequest(
                        userId = userId,
                        foodName = foodName,
                        servingSize = servingSize,
                        measuringUnit = measuringUnit,
                        date = date,
                        mealType = mealType,
                        calories = calories,
                        carbs = carbs,
                        fat = fat,
                        protein = protein,
                        foodId = foodId
                    )
                    
                    val response = apiService.createFoodLog(request)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverId = response.body()?.data?.id
                        
                        // Save to local DB with sync flag
                        val syncedEntity = foodLogEntity.copy(
                            serverId = serverId,
                            isSynced = true
                        )
                        foodLogDao.insert(syncedEntity)
                        
                        Log.d("FoodLogRepository", "Food log created online and saved locally")
                        return@withContext Result.success(serverId ?: "")
                    } else {
                        // API call failed, save locally
                        foodLogDao.insert(foodLogEntity)
                        Log.w("FoodLogRepository", "API call failed, saved offline")
                        return@withContext Result.success("offline")
                    }
                } catch (e: Exception) {
                    // Network error, save locally
                    foodLogDao.insert(foodLogEntity)
                    Log.e("FoodLogRepository", "Network error, saved offline: ${e.message}")
                    return@withContext Result.success("offline")
                }
            } else {
                // Offline, save locally
                foodLogDao.insert(foodLogEntity)
                Log.d("FoodLogRepository", "Offline, saved locally")
                return@withContext Result.success("offline")
            }
        } catch (e: Exception) {
            Log.e("FoodLogRepository", "Error creating food log: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Get food logs for a specific date, combining local unsynced and API data
     */
    suspend fun getFoodLogsByDate(userId: String, date: String): List<FoodLogEntity> = withContext(Dispatchers.IO) {
        // Always return local data (includes both synced and unsynced)
        foodLogDao.getByDate(userId, date)
    }
    
    /**
     * Get all food logs for a user from local database
     */
    suspend fun getAllFoodLogs(userId: String): List<FoodLogEntity> = withContext(Dispatchers.IO) {
        // Get all food logs for the user from local database
        foodLogDao.getByDateRange(userId, "2020-01-01", "2030-12-31")
    }
    
    /**
     * Delete a food log
     */
    suspend fun deleteFoodLog(localId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("FoodLogRepository", "🔍 Looking for food log with localId: $localId")
            val foodLog = foodLogDao.getById(localId)
            
            if (foodLog != null) {
                Log.d("FoodLogRepository", "✅ Found food log: ${foodLog.foodName}, isSynced: ${foodLog.isSynced}, serverId: ${foodLog.serverId}")
                // If synced and online, delete from API
                if (foodLog.isSynced && foodLog.serverId != null && networkManager.isOnline()) {
                    try {
                        Log.d("FoodLogRepository", "🌐 Deleting from API with serverId: ${foodLog.serverId}")
                        apiService.deleteFoodLog(foodLog.serverId)
                        Log.d("FoodLogRepository", "✅ Successfully deleted from API")
                    } catch (e: Exception) {
                        Log.w("FoodLogRepository", "⚠️ Failed to delete from API: ${e.message}")
                    }
                } else {
                    Log.d("FoodLogRepository", "📱 Skipping API deletion - isSynced: ${foodLog.isSynced}, serverId: ${foodLog.serverId}, isOnline: ${networkManager.isOnline()}")
                }
                
                // Always delete locally
                Log.d("FoodLogRepository", "🗑️ Deleting from local database")
                foodLogDao.delete(foodLog)
                Log.d("FoodLogRepository", "✅ Successfully deleted from local database")
                Result.success(Unit)
            } else {
                Log.e("FoodLogRepository", "❌ Food log not found with localId: $localId")
                Result.failure(Exception("Food log not found"))
            }
        } catch (e: Exception) {
            Log.e("FoodLogRepository", "❌ Error deleting food log: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all unsynced food logs for syncing
     */
    suspend fun getUnsyncedLogs(): List<FoodLogEntity> = withContext(Dispatchers.IO) {
        foodLogDao.getUnsyncedLogs()
    }
    
    /**
     * Mark a food log as synced
     */
    suspend fun markAsSynced(localId: String, serverId: String) = withContext(Dispatchers.IO) {
        foodLogDao.markAsSynced(localId, serverId)
    }
}

