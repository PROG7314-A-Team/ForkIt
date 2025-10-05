package com.example.forkit.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.Habit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar

class HabitNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val userId = intent.getStringExtra("USER_ID") ?: return
        val checkAll = intent.getBooleanExtra("CHECK_ALL", false)
        
        if (checkAll) {
            // Check all habits and send notifications for today's habits
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    checkAndNotifyTodaysHabits(context, userId)
                } catch (e: Exception) {
                    android.util.Log.e("HabitNotificationReceiver", "Error checking habits", e)
                }
            }
        } else {
            // Legacy support for old frequency-based notifications
            val frequency = intent.getStringExtra("FREQUENCY") ?: "DAILY"
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val habits = fetchHabitsForToday(userId, frequency)
                    withContext(Dispatchers.Main) {
                        val notificationService = HabitNotificationService(context)
                        notificationService.sendHabitReminder(userId, habits, frequency)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HabitNotificationReceiver", "Error fetching habits", e)
                }
            }
        }
    }
    
    private suspend fun checkAndNotifyTodaysHabits(context: Context, userId: String) {
        val today = Calendar.getInstance()
        val dayOfWeek = today.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, ..., 7=Saturday
        val dayOfMonth = today.get(Calendar.DAY_OF_MONTH)
        
        // Fetch all habit types
        val dailyHabits = fetchHabits(userId, "DAILY")
        val weeklyHabits = fetchHabits(userId, "WEEKLY")
        val monthlyHabits = fetchHabits(userId, "MONTHLY")
        
        val notificationService = HabitNotificationService(context)
        
        // Send notification for daily habits (always)
        if (dailyHabits.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                notificationService.sendHabitReminder(userId, dailyHabits, "DAILY")
            }
        }
        
        // Filter weekly habits that should notify today
        val todaysWeeklyHabits = weeklyHabits.filter { habit ->
            habit.selectedDays?.contains(dayOfWeek - 1) == true // Convert to 0-based (0=Sunday)
        }
        
        if (todaysWeeklyHabits.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                notificationService.sendHabitReminder(userId, todaysWeeklyHabits, "WEEKLY")
            }
        }
        
        // Filter monthly habits that should notify today
        val todaysMonthlyHabits = monthlyHabits.filter { habit ->
            habit.dayOfMonth == dayOfMonth
        }
        
        if (todaysMonthlyHabits.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                notificationService.sendHabitReminder(userId, todaysMonthlyHabits, "MONTHLY")
            }
        }
        
        android.util.Log.d(
            "HabitNotificationReceiver",
            "Checked habits - Daily: ${dailyHabits.size}, Weekly (today): ${todaysWeeklyHabits.size}, Monthly (today): ${todaysMonthlyHabits.size}"
        )
    }
    
    private suspend fun fetchHabits(userId: String, frequency: String): List<Habit> {
        return try {
            val response = when (frequency) {
                "DAILY" -> RetrofitClient.api.getDailyHabits(userId)
                "WEEKLY" -> RetrofitClient.api.getWeeklyHabits(userId)
                "MONTHLY" -> RetrofitClient.api.getMonthlyHabits(userId)
                else -> RetrofitClient.api.getDailyHabits(userId)
            }
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Only return uncompleted habits
                response.body()?.data?.filter { !it.isCompleted } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitNotificationReceiver", "Error fetching $frequency habits", e)
            emptyList()
        }
    }
    
    private suspend fun fetchHabitsForToday(userId: String, frequency: String): List<Habit> {
        return fetchHabits(userId, frequency)
    }
}