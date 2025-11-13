package com.example.forkit.data.local.dao

import androidx.room.*
import com.example.forkit.data.local.entities.ExerciseLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseLogDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exerciseLog: ExerciseLogEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exerciseLogs: List<ExerciseLogEntity>)
    
    @Update
    suspend fun update(exerciseLog: ExerciseLogEntity)
    
    @Delete
    suspend fun delete(exerciseLog: ExerciseLogEntity)
    
    @Query("SELECT * FROM exercise_logs WHERE localId = :localId")
    suspend fun getById(localId: String): ExerciseLogEntity?
    
    @Query("SELECT * FROM exercise_logs WHERE serverId = :serverId")
    suspend fun getByServerId(serverId: String): ExerciseLogEntity?
    
    @Query("SELECT * FROM exercise_logs WHERE userId = :userId AND date = :date ORDER BY createdAt DESC")
    suspend fun getByDate(userId: String, date: String): List<ExerciseLogEntity>
    
    @Query("SELECT * FROM exercise_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getByDateRange(userId: String, startDate: String, endDate: String): List<ExerciseLogEntity>
    
    @Query("SELECT * FROM exercise_logs WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedLogs(): List<ExerciseLogEntity>
    
    @Query("SELECT * FROM exercise_logs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllFlow(userId: String): Flow<List<ExerciseLogEntity>>
    
    @Query("UPDATE exercise_logs SET isSynced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)
    
    @Query("DELETE FROM exercise_logs WHERE isSynced = 1 AND createdAt < :timestamp")
    suspend fun deleteOldSyncedLogs(timestamp: Long)
}

