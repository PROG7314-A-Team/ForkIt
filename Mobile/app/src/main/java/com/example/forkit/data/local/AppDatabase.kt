package com.example.forkit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3,
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
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habits ADD COLUMN selectedDays TEXT")
                database.execSQL("ALTER TABLE habits ADD COLUMN dayOfMonth INTEGER")
            }
        }
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "forkit_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

