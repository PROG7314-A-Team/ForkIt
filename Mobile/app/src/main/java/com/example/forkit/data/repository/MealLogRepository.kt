package com.example.forkit.data.repository

import android.util.Log
import com.example.forkit.data.ApiService
import com.example.forkit.data.local.dao.MealLogDao
import com.example.forkit.data.local.entities.MealIngredient
import com.example.forkit.data.local.entities.MealLogEntity
import com.example.forkit.data.models.CreateMealLogRequest
import com.example.forkit.data.models.Ingredient
import com.example.forkit.utils.NetworkConnectivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MealLogRepository(
    private val apiService: ApiService,
    private val mealLogDao: MealLogDao,
    private val networkManager: NetworkConnectivityManager
) {
    
    /**
     * Create a full meal log entry. If online, save to API then to local DB.
     * If offline, save to local DB with isSynced=false.
     */
    suspend fun createMealLog(
        userId: String,
        name: String,
        description: String,
        ingredients: List<MealIngredient>,
        instructions: List<String>,
        totalCalories: Double,
        totalCarbs: Double,
        totalFat: Double,
        totalProtein: Double,
        servings: Double,
        date: String,
        mealType: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val mealLogEntity = MealLogEntity(
                userId = userId,
                name = name,
                description = description,
                ingredients = ingredients,
                instructions = instructions,
                totalCalories = totalCalories,
                totalCarbs = totalCarbs,
                totalFat = totalFat,
                totalProtein = totalProtein,
                servings = servings,
                date = date,
                mealType = mealType,
                isSynced = false
            )
            
            if (networkManager.isOnline()) {
                // Try to save to API
                try {
                    val request = CreateMealLogRequest(
                        userId = userId,
                        name = name,
                        description = description,
                        ingredients = ingredients.map {
                            Ingredient(
                                name = it.foodName,
                                amount = it.quantity,
                                unit = it.unit
                            )
                        },
                        instructions = instructions,
                        totalCalories = totalCalories,
                        totalCarbs = totalCarbs,
                        totalFat = totalFat,
                        totalProtein = totalProtein,
                        servings = servings,
                        date = date,
                        mealType = mealType
                    )
                    
                    val response = apiService.createMealLog(request)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverId = response.body()?.data?.id
                        
                        // Save to local DB with sync flag
                        val syncedEntity = mealLogEntity.copy(
                            serverId = serverId,
                            isSynced = true
                        )
                        mealLogDao.insert(syncedEntity)
                        
                        Log.d("MealLogRepository", "Meal log created online and saved locally")
                        return@withContext Result.success(serverId ?: "")
                    } else {
                        // API call failed, save locally
                        mealLogDao.insert(mealLogEntity)
                        Log.w("MealLogRepository", "API call failed, saved offline")
                        return@withContext Result.success("offline")
                    }
                } catch (e: Exception) {
                    // Network error, save locally
                    mealLogDao.insert(mealLogEntity)
                    Log.e("MealLogRepository", "Network error, saved offline: ${e.message}")
                    return@withContext Result.success("offline")
                }
            } else {
                // Offline, save locally
                mealLogDao.insert(mealLogEntity)
                Log.d("MealLogRepository", "Offline, saved locally")
                return@withContext Result.success("offline")
            }
        } catch (e: Exception) {
            Log.e("MealLogRepository", "Error creating meal log: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Get meal logs for a specific date
     */
    suspend fun getMealLogsByDate(userId: String, date: String): List<MealLogEntity> = withContext(Dispatchers.IO) {
        mealLogDao.getByDate(userId, date)
    }
    
    /**
     * Get meal logs for a date range
     */
    suspend fun getMealLogsByDateRange(userId: String, startDate: String, endDate: String): List<MealLogEntity> = withContext(Dispatchers.IO) {
        mealLogDao.getByDateRange(userId, startDate, endDate)
    }
    
    /**
     * Get all meal logs for a user
     */
    suspend fun getAllMealLogs(userId: String): List<MealLogEntity> = withContext(Dispatchers.IO) {
        mealLogDao.getByUserId(userId)
    }
    
    /**
     * Delete a meal log
     */
    suspend fun deleteMealLog(mealLogId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val mealLog = mealLogDao.getById(mealLogId)
            
            if (mealLog != null) {
                // If synced and online, delete from API
                if (mealLog.isSynced && mealLog.serverId != null && networkManager.isOnline()) {
                    try {
                        apiService.deleteMealLog(mealLog.serverId)
                    } catch (e: Exception) {
                        Log.w("MealLogRepository", "Failed to delete from API: ${e.message}")
                    }
                }
                
                // Delete from local DB
                mealLogDao.deleteById(mealLogId)
                Log.d("MealLogRepository", "Meal log deleted successfully")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Meal log not found"))
            }
        } catch (e: Exception) {
            Log.e("MealLogRepository", "Error deleting meal log: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get unsynced meal logs for sync
     */
    suspend fun getUnsyncedLogs(): List<MealLogEntity> = withContext(Dispatchers.IO) {
        mealLogDao.getUnsyncedLogs()
    }
    
    /**
     * Mark meal log as synced
     */
    suspend fun markAsSynced(localId: String, serverId: String) = withContext(Dispatchers.IO) {
        mealLogDao.markAsSynced(localId, serverId)
    }
}