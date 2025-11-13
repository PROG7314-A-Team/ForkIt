package com.example.forkit.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.local.AppDatabase
import com.example.forkit.data.models.*
import com.example.forkit.data.repository.*
import com.example.forkit.utils.NetworkConnectivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val TAG = "DataSyncWorker"
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting data sync...")
        
        try {
            val database = AppDatabase.getInstance(applicationContext)
            val networkManager = NetworkConnectivityManager(applicationContext)
            val apiService = RetrofitClient.api
            
            // Check if online
            if (!networkManager.isOnline()) {
                Log.w(TAG, "Device is offline, skipping sync")
                return@withContext Result.retry()
            }
            
            // Create repositories
            val foodLogRepository = FoodLogRepository(
                apiService,
                database.foodLogDao(),
                networkManager
            )
            
            val mealLogRepository = MealLogRepository(
                apiService,
                database.mealLogDao(),
                networkManager
            )
            
            val waterLogRepository = WaterLogRepository(
                apiService,
                database.waterLogDao(),
                networkManager
            )
            
            val exerciseLogRepository = ExerciseLogRepository(
                apiService,
                database.exerciseLogDao(),
                networkManager
            )
            
            val habitRepository = HabitRepository(
                apiService,
                database.habitDao(),
                networkManager
            )
            
            val affectedUserIds = mutableSetOf<String>()
            
            var syncSuccess = true
            
            // Sync Food Logs
            val unsyncedFoodLogs = foodLogRepository.getUnsyncedLogs()
            Log.d(TAG, "Syncing ${unsyncedFoodLogs.size} food logs...")
            
            for (foodLog in unsyncedFoodLogs) {
                affectedUserIds.add(foodLog.userId)
                try {
                    val request = CreateFoodLogRequest(
                        userId = foodLog.userId,
                        foodName = foodLog.foodName,
                        servingSize = foodLog.servingSize,
                        measuringUnit = foodLog.measuringUnit,
                        date = foodLog.date,
                        mealType = foodLog.mealType,
                        calories = foodLog.calories,
                        carbs = foodLog.carbs,
                        fat = foodLog.fat,
                        protein = foodLog.protein,
                        foodId = foodLog.foodId
                    )
                    
                    val response = apiService.createFoodLog(request)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverId = response.body()?.data?.id
                        if (serverId != null) {
                            foodLogRepository.markAsSynced(foodLog.localId, serverId)
                            Log.d(TAG, "Food log synced: ${foodLog.localId} -> $serverId")
                        }
                    } else {
                        Log.w(TAG, "Failed to sync food log: ${response.message()}")
                        syncSuccess = false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing food log: ${e.message}", e)
                    syncSuccess = false
                }
            }
            
            // Sync Meal Logs
            val unsyncedMealLogs = mealLogRepository.getUnsyncedLogs()
            Log.d(TAG, "Syncing ${unsyncedMealLogs.size} meal logs...")
            
            for (mealLog in unsyncedMealLogs) {
                affectedUserIds.add(mealLog.userId)
                try {
                    val request = CreateMealLogRequest(
                        userId = mealLog.userId,
                        name = mealLog.name,
                        description = mealLog.description,
                        ingredients = mealLog.ingredients.map {
                            Ingredient(
                                name = it.foodName,
                                amount = it.quantity,
                                unit = it.unit
                            )
                        },
                        instructions = mealLog.instructions,
                        totalCalories = mealLog.totalCalories,
                        totalCarbs = mealLog.totalCarbs,
                        totalFat = mealLog.totalFat,
                        totalProtein = mealLog.totalProtein,
                        servings = mealLog.servings,
                        date = mealLog.date,
                        mealType = mealLog.mealType
                    )
                    
                    val response = apiService.createMealLog(request)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverId = response.body()?.data?.id
                        if (serverId != null) {
                            mealLogRepository.markAsSynced(mealLog.localId, serverId)
                            Log.d(TAG, "Meal log synced: ${mealLog.localId} -> $serverId")
                        }
                    } else {
                        Log.w(TAG, "Failed to sync meal log: ${response.message()}")
                        syncSuccess = false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing meal log: ${e.message}", e)
                    syncSuccess = false
                }
            }
            
            // Sync Water Logs
            val unsyncedWaterLogs = waterLogRepository.getUnsyncedLogs()
            Log.d(TAG, "Syncing ${unsyncedWaterLogs.size} water logs...")
            
            for (waterLog in unsyncedWaterLogs) {
                affectedUserIds.add(waterLog.userId)
                try {
                    val request = CreateWaterLogRequest(
                        userId = waterLog.userId,
                        amount = waterLog.amount,
                        date = waterLog.date
                    )
                    
                    val response = apiService.createWaterLog(request)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverId = response.body()?.data?.id
                        if (serverId != null) {
                            waterLogRepository.markAsSynced(waterLog.localId, serverId)
                            Log.d(TAG, "Water log synced: ${waterLog.localId} -> $serverId")
                        }
                    } else {
                        Log.w(TAG, "Failed to sync water log: ${response.message()}")
                        syncSuccess = false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing water log: ${e.message}", e)
                    syncSuccess = false
                }
            }
            
            // Sync Exercise Logs
            val unsyncedExerciseLogs = exerciseLogRepository.getUnsyncedLogs()
            Log.d(TAG, "Syncing ${unsyncedExerciseLogs.size} exercise logs...")
            
            for (exerciseLog in unsyncedExerciseLogs) {
                affectedUserIds.add(exerciseLog.userId)
                try {
                    val request = CreateExerciseLogRequest(
                        userId = exerciseLog.userId,
                        name = exerciseLog.name,
                        date = exerciseLog.date,
                        caloriesBurnt = exerciseLog.caloriesBurnt,
                        type = exerciseLog.type,
                        duration = exerciseLog.duration,
                        notes = exerciseLog.notes
                    )
                    
                    val response = apiService.createExerciseLog(request)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverId = response.body()?.data?.id
                        if (serverId != null) {
                            exerciseLogRepository.markAsSynced(exerciseLog.localId, serverId)
                            Log.d(TAG, "Exercise log synced: ${exerciseLog.localId} -> $serverId")
                        }
                    } else {
                        Log.w(TAG, "Failed to sync exercise log: ${response.message()}")
                        syncSuccess = false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing exercise log: ${e.message}", e)
                    syncSuccess = false
                }
            }
            
            // Sync Habits
            val unsyncedHabits = habitRepository.getUnsyncedHabits()
            Log.d(TAG, "Syncing ${unsyncedHabits.size} habits...")
            
            for (habit in unsyncedHabits) {
                affectedUserIds.add(habit.userId)
                try {
                    val habitRequest = CreateHabitRequest(
                        title = habit.title,
                        description = habit.description,
                        frequency = habit.frequency
                    )
                    
                    val request = CreateHabitApiRequest(
                        userId = habit.userId,
                        habit = habitRequest
                    )
                    
                    val response = apiService.createHabit(request)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverId = response.body()?.data?.id
                        if (serverId != null) {
                            habitRepository.markAsSynced(habit.localId, serverId)
                            Log.d(TAG, "Habit synced: ${habit.localId} -> $serverId")
                        }
                    } else {
                        Log.w(TAG, "Failed to sync habit: ${response.message()}")
                        syncSuccess = false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing habit: ${e.message}", e)
                    syncSuccess = false
                }
            }
            
            // Sync Habit Deletions
            val unsyncedDeletes = habitRepository.getUnsyncedDeletes()
            Log.d(TAG, "Syncing ${unsyncedDeletes.size} habit deletions...")
            
            for (habit in unsyncedDeletes) {
                habit.userId?.let { affectedUserIds.add(it) }
                try {
                    if (habit.serverId != null) {
                        val response = apiService.deleteHabit(habit.serverId)
                        
                        if (response.isSuccessful && response.body()?.success == true) {
                            // Hard delete after successful sync
                            val entity = database.habitDao().getById(habit.localId)
                            if (entity != null) {
                                database.habitDao().delete(entity)
                            }
                            Log.d(TAG, "Habit deletion synced: ${habit.serverId}")
                        }
                    } else {
                        // Never synced, just clean up locally
                        val entity = database.habitDao().getById(habit.localId)
                        if (entity != null) {
                            database.habitDao().delete(entity)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing habit deletion: ${e.message}", e)
                    syncSuccess = false
                }
            }
            
            val totalSynced = unsyncedFoodLogs.size + unsyncedMealLogs.size + 
                             unsyncedWaterLogs.size + unsyncedExerciseLogs.size + unsyncedHabits.size
            Log.d(TAG, "Sync complete! Total items processed: $totalSynced")

            for (userId in affectedUserIds) {
                try {
                    foodLogRepository.refreshUserLogs(userId)
                    mealLogRepository.refreshUserMeals(userId)
                    waterLogRepository.refreshUserWaterLogs(userId)
                    exerciseLogRepository.refreshUserExerciseLogs(userId)
                    habitRepository.refreshHabits(userId)
                } catch (e: Exception) {
                    Log.w(TAG, "Post-sync refresh failed for user $userId: ${e.message}")
                }
            }
            
            return@withContext if (syncSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Sync worker failed: ${e.message}", e)
            return@withContext Result.retry()
        }
    }
}

