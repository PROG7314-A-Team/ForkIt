package com.example.forkit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.forkit.data.local.converters.Converters
import com.example.forkit.data.local.dao.*
import com.example.forkit.data.local.entities.*

@Database(
    entities = [
        FoodLogEntity::class,
        MealLogEntity::class,
        WaterLogEntity::class,
        ExerciseLogEntity::class,
        HabitEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun foodLogDao(): FoodLogDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun exerciseLogDao(): ExerciseLogDao
    abstract fun habitDao(): HabitDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "forkit_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

