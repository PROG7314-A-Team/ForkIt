package com.example.forkit.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.forkit.HabitsActivity
import com.example.forkit.R
import com.example.forkit.data.models.Habit

class HabitNotificationService(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "habit_reminders"
        const val CHANNEL_NAME = "Habit Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for your daily, weekly, and monthly habits"
        const val NOTIFICATION_ID_BASE = 1000
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun sendHabitReminder(userId: String, habits: List<Habit>, frequency: String) {
        if (habits.isEmpty()) return
        
        val notificationHelper = HabitNotificationHelper(context)
        if (!notificationHelper.areNotificationsEnabled()) return
        
        val intent = Intent(context, HabitsActivity::class.java).apply {
            putExtra("USER_ID", userId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val title = when (frequency) {
            "DAILY" -> "Daily Habits Reminder"
            "WEEKLY" -> "Weekly Habits Reminder"
            "MONTHLY" -> "Monthly Habits Reminder"
            else -> "Habit Reminder"
        }
        
        val habitCount = habits.size
        val contentText = if (habitCount == 1) {
            "You have 1 habit to complete today: ${habits[0].title}"
        } else {
            "You have $habitCount habits to complete today"
        }
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.forkit_logo)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Add expanded text for multiple habits
        if (habitCount > 1) {
            val inboxStyle = NotificationCompat.InboxStyle()
            inboxStyle.setBigContentTitle(title)
            habits.take(5).forEach { habit ->
                inboxStyle.addLine("• ${habit.title}")
            }
            if (habitCount > 5) {
                inboxStyle.addLine("and ${habitCount - 5} more...")
            }
            builder.setStyle(inboxStyle)
        }
        
        try {
            with(NotificationManagerCompat.from(context)) {
                val notificationId = when (frequency) {
                    "DAILY" -> NOTIFICATION_ID_BASE
                    "WEEKLY" -> NOTIFICATION_ID_BASE + 1
                    "MONTHLY" -> NOTIFICATION_ID_BASE + 2
                    else -> NOTIFICATION_ID_BASE + 3
                }
                notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            android.util.Log.e("HabitNotification", "Permission denied for notifications", e)
        }
    }
    
    fun sendGenericHabitReminder(userId: String, title: String, message: String) {
        val notificationHelper = HabitNotificationHelper(context)
        if (!notificationHelper.areNotificationsEnabled()) return
        
        val intent = Intent(context, HabitsActivity::class.java).apply {
            putExtra("USER_ID", userId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.forkit_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID_BASE + 10, builder.build())
            }
        } catch (e: SecurityException) {
            android.util.Log.e("HabitNotification", "Permission denied for notifications", e)
        }
    }
}
