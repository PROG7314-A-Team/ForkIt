package com.example.forkit.data.local.dao

import androidx.room.*
import com.example.forkit.data.local.entities.FoodLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodLog: FoodLogEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foodLogs: List<FoodLogEntity>)
    
    @Update
    suspend fun update(foodLog: FoodLogEntity)
    
    @Delete
    suspend fun delete(foodLog: FoodLogEntity)
    
    @Query("SELECT * FROM food_logs WHERE localId = :localId")
    suspend fun getById(localId: String): FoodLogEntity?
    
    @Query("SELECT * FROM food_logs WHERE serverId = :serverId")
    suspend fun getByServerId(serverId: String): FoodLogEntity?
    
    @Query("SELECT * FROM food_logs WHERE userId = :userId AND date = :date ORDER BY createdAt DESC")
    suspend fun getByDate(userId: String, date: String): List<FoodLogEntity>
    
    @Query("SELECT * FROM food_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getByDateRange(userId: String, startDate: String, endDate: String): List<FoodLogEntity>
    
    @Query("SELECT * FROM food_logs WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedLogs(): List<FoodLogEntity>
    
    @Query("SELECT * FROM food_logs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllFlow(userId: String): Flow<List<FoodLogEntity>>
    
    @Query("UPDATE food_logs SET isSynced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)
    
    @Query("DELETE FROM food_logs WHERE isSynced = 1 AND createdAt < :timestamp")
    suspend fun deleteOldSyncedLogs(timestamp: Long)
}

