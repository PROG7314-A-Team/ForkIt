package com.example.forkit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.ui.theme.ForkItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Compose layout
        setContent {
            ForkItTheme {
                MainScreen {
                    // Navigate to AccountActivity when button clicked
                    val intent = Intent(this, AccountActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun MainScreen(onButtonClick: () -> Unit) {
    // Column mimics LinearLayout vertical orientation
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello ForkIt (Compose)!",
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp)) // Optional spacing

        Button(onClick = onButtonClick) {
            Text(text = "Click Me")
        }
    }
}

// --- Compose preview for Android Studio ---
@Composable
fun MainScreenPreview() {
    ForkItTheme {
        MainScreen(onButtonClick = {})
    }
}
