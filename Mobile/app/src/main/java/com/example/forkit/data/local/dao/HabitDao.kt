package com.example.forkit.data.local.dao

import androidx.room.*
import com.example.forkit.data.local.entities.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(habits: List<HabitEntity>)
    
    @Update
    suspend fun update(habit: HabitEntity)
    
    @Delete
    suspend fun delete(habit: HabitEntity)
    
    @Query("SELECT * FROM habits WHERE localId = :localId")
    suspend fun getById(localId: String): HabitEntity?
    
    @Query("SELECT * FROM habits WHERE serverId = :serverId")
    suspend fun getByServerId(serverId: String): HabitEntity?
    
    @Query("SELECT * FROM habits WHERE userId = :userId AND isDeleted = 0 AND frequency = :frequency ORDER BY createdAt DESC")
    suspend fun getByFrequency(userId: String, frequency: String): List<HabitEntity>
    
    @Query("SELECT * FROM habits WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getAllHabits(userId: String): List<HabitEntity>
    
    @Query("SELECT * FROM habits WHERE isSynced = 0 AND isDeleted = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedHabits(): List<HabitEntity>
    
    @Query("SELECT * FROM habits WHERE isSynced = 0 AND isDeleted = 1 ORDER BY updatedAt ASC")
    suspend fun getUnsyncedDeletes(): List<HabitEntity>
    
    @Query("SELECT * FROM habits WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllFlow(userId: String): Flow<List<HabitEntity>>
    
    @Query("UPDATE habits SET isSynced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)
    
    @Query("UPDATE habits SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE localId = :localId")
    suspend fun markAsDeleted(localId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM habits WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun cleanupDeletedHabits()
}

