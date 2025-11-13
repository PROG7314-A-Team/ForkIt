package com.example.forkit.data.local.dao

import androidx.room.*
import com.example.forkit.data.local.entities.MealLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getByUserId(userId: String): List<MealLogEntity>
    
    @Query("SELECT * FROM meal_logs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllFlow(userId: String): Flow<List<MealLogEntity>>
    
    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date = :date ORDER BY createdAt DESC")
    suspend fun getByDate(userId: String, date: String): List<MealLogEntity>
    
    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getByDateRange(userId: String, startDate: String, endDate: String): List<MealLogEntity>
    
    @Query("SELECT * FROM meal_logs WHERE localId = :localId")
    suspend fun getById(localId: String): MealLogEntity?
    
    @Query("SELECT * FROM meal_logs WHERE serverId = :serverId")
    suspend fun getByServerId(serverId: String): MealLogEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mealLog: MealLogEntity)
    
    @Update
    suspend fun update(mealLog: MealLogEntity)
    
    @Delete
    suspend fun delete(mealLog: MealLogEntity)
    
    @Query("DELETE FROM meal_logs WHERE localId = :localId")
    suspend fun deleteById(localId: String)
    
    @Query("SELECT * FROM meal_logs WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedLogs(): List<MealLogEntity>
    
    @Query("UPDATE meal_logs SET isSynced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)
    
    @Query("DELETE FROM meal_logs WHERE isSynced = 1 AND createdAt < :timestamp")
    suspend fun deleteOldSyncedLogs(timestamp: Long)
    
    // NEW: Template-specific queries
    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND isTemplate = 1 ORDER BY createdAt DESC")
    suspend fun getTemplates(userId: String): List<MealLogEntity>
    
    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND isTemplate = 0 AND date = :date ORDER BY createdAt DESC")
    suspend fun getLoggedByDate(userId: String, date: String): List<MealLogEntity>
    
    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND isTemplate = 0 ORDER BY createdAt DESC")
    suspend fun getLoggedMeals(userId: String): List<MealLogEntity>
}