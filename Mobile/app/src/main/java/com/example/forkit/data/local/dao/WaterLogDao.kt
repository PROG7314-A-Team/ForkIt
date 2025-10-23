package com.example.forkit.data.local.dao

import androidx.room.*
import com.example.forkit.data.local.entities.WaterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(waterLog: WaterLogEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(waterLogs: List<WaterLogEntity>)
    
    @Update
    suspend fun update(waterLog: WaterLogEntity)
    
    @Delete
    suspend fun delete(waterLog: WaterLogEntity)
    
    @Query("SELECT * FROM water_logs WHERE localId = :localId")
    suspend fun getById(localId: String): WaterLogEntity?
    
    @Query("SELECT * FROM water_logs WHERE serverId = :serverId")
    suspend fun getByServerId(serverId: String): WaterLogEntity?
    
    @Query("SELECT * FROM water_logs WHERE userId = :userId AND date = :date ORDER BY createdAt DESC")
    suspend fun getByDate(userId: String, date: String): List<WaterLogEntity>
    
    @Query("SELECT * FROM water_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getByDateRange(userId: String, startDate: String, endDate: String): List<WaterLogEntity>
    
    @Query("SELECT * FROM water_logs WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedLogs(): List<WaterLogEntity>
    
    @Query("SELECT * FROM water_logs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllFlow(userId: String): Flow<List<WaterLogEntity>>
    
    @Query("UPDATE water_logs SET isSynced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)
    
    @Query("DELETE FROM water_logs WHERE isSynced = 1 AND createdAt < :timestamp")
    suspend fun deleteOldSyncedLogs(timestamp: Long)
}

