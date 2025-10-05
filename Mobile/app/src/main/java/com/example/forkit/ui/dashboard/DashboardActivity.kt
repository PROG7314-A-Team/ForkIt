package com.example.forkit.ui.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.util.Log
import com.example.forkit.ui.theme.ForkItTheme

/**
 * 🧱 DashboardActivity.kt
 *
 * This is the entry point for the main dashboard screen of the ForkIt app.
 * It initializes the Composable UI and handles lifecycle refreshes.
 */
class DashboardActivity : ComponentActivity() {
    // Holds a reference to the refresh function inside DashboardScreen
    private var refreshCallback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userId = intent.getStringExtra("USER_ID") ?: ""
        val initialTab = intent.getIntExtra("SELECTED_TAB", 0)

        Log.d("DashboardActivity", "✅ onCreate → launching DashboardScreen | userId=$userId | initialTab=$initialTab")

        setContent {
            ForkItTheme {
                DashboardScreen(
                    userId = userId,
                    initialSelectedTab = initialTab,
                    onRefreshCallbackSet = { callback ->
                        Log.d("DashboardActivity", "📲 Refresh callback linked to activity lifecycle")
                        refreshCallback = callback
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("DashboardActivity", "🔄 onResume → triggering dashboard refresh if callback is set")
        refreshCallback?.invoke()
    }
}
