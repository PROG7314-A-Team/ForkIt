package com.example.forkit.utils

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import com.example.forkit.DashboardActivity
import com.example.forkit.TellUsAboutYourselfActivity

fun Context.navigateToDashboard(userId: String, idToken: String, email: String) {
    persistSession(userId, idToken, email)
    val intent = Intent(this, DashboardActivity::class.java).apply {
        putExtra("USER_ID", userId)
        putExtra("ID_TOKEN", idToken)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
    (this as? ComponentActivity)?.finish()
}

fun Context.navigateToOnboarding(userId: String, idToken: String, email: String) {
    persistSession(userId, idToken, email)
    val intent = Intent(this, TellUsAboutYourselfActivity::class.java).apply {
        putExtra("USER_ID", userId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
    (this as? ComponentActivity)?.finish()
}

private fun Context.persistSession(userId: String, idToken: String, email: String) {
    AuthPreferences(this).saveLoginData(userId, idToken, email)
}

