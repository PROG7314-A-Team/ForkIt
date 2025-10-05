package com.example.forkit.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class HabitNotificationScheduler(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationHelper = HabitNotificationHelper(context)
    
    companion object {
        const val REQUEST_CODE_DAILY_CHECK = 1001
    }
    
    fun scheduleAllNotifications(userId: String) {
        if (!notificationHelper.areNotificationsEnabled()) {
            cancelAllNotifications()
            return
        }
        
        scheduleDailyCheck(userId)
    }
    
    private fun scheduleDailyCheck(userId: String) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, notificationHelper.getNotificationTimeHour())
            set(Calendar.MINUTE, notificationHelper.getNotificationTimeMinute())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If time has passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        val intent = Intent(context, HabitNotificationReceiver::class.java).apply {
            putExtra("USER_ID", userId)
            putExtra("CHECK_ALL", true)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DAILY_CHECK,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Schedule repeating daily alarm
        // This will check all habits every day and send notifications for relevant ones
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        
        android.util.Log.d("HabitScheduler", "Daily habit check scheduled for ${calendar.time}")
    }
    
    fun cancelAllNotifications() {
        cancelNotification(REQUEST_CODE_DAILY_CHECK)
        android.util.Log.d("HabitScheduler", "All notifications cancelled")
    }
    
    private fun cancelNotification(requestCode: Int) {
        val intent = Intent(context, HabitNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
    
    fun rescheduleNotifications(userId: String) {
        cancelAllNotifications()
        scheduleAllNotifications(userId)
    }
}