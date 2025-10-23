package com.example.forkit.data.local.dao

import androidx.room.*
import com.example.forkit.data.local.entities.MealLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mealLog: MealLogEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mealLogs: List<MealLogEntity>)
    
    @Update
    suspend fun update(mealLog: MealLogEntity)
    
    @Delete
    suspend fun delete(mealLog: MealLogEntity)
    
    @Query("SELECT * FROM meal_logs WHERE localId = :localId")
    suspend fun getById(localId: String): MealLogEntity?
    
    @Query("SELECT * FROM meal_logs WHERE serverId = :serverId")
    suspend fun getByServerId(serverId: String): MealLogEntity?
    
    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date = :date ORDER BY createdAt DESC")
    suspend fun getByDate(userId: String, date: String): List<MealLogEntity>
    
    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getByDateRange(userId: String, startDate: String, endDate: String): List<MealLogEntity>
    
    @Query("SELECT * FROM meal_logs WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedLogs(): List<MealLogEntity>
    
    @Query("SELECT * FROM meal_logs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllFlow(userId: String): Flow<List<MealLogEntity>>
    
    @Query("UPDATE meal_logs SET isSynced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)
    
    @Query("DELETE FROM meal_logs WHERE isSynced = 1 AND createdAt < :timestamp")
    suspend fun deleteOldSyncedLogs(timestamp: Long)
}

