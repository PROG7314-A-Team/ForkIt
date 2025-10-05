package com.example.forkit.services

import android.content.Context
import android.content.SharedPreferences

class HabitNotificationHelper(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "habit_notification_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_NOTIFICATION_TIME_HOUR = "notification_time_hour"
        const val KEY_NOTIFICATION_TIME_MINUTE = "notification_time_minute"
        const val DEFAULT_NOTIFICATION_HOUR = 9 // 9 AM
        const val DEFAULT_NOTIFICATION_MINUTE = 0
    }
    
    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }
    
    fun getNotificationTimeHour(): Int {
        return prefs.getInt(KEY_NOTIFICATION_TIME_HOUR, DEFAULT_NOTIFICATION_HOUR)
    }
    
    fun getNotificationTimeMinute(): Int {
        return prefs.getInt(KEY_NOTIFICATION_TIME_MINUTE, DEFAULT_NOTIFICATION_MINUTE)
    }
    
    fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_NOTIFICATION_TIME_HOUR, hour)
            .putInt(KEY_NOTIFICATION_TIME_MINUTE, minute)
            .apply()
    }
}
