package com.example.forkit

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.ui.theme.ForkItTheme
import com.example.forkit.utils.AuthPreferences
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize LanguageManager and apply saved language
        LanguageManager.initialize(this)
        LanguageManager.applyLanguage(this)
        
        // Load theme preference before setting content
        ThemeManager.loadThemeMode(this)
        
        setContent {
            ForkItTheme {
                SplashScreen(
                    onTimeout = {
                        checkAutoSignIn()
                    }
                )
            }
        }
    }
    
    private fun checkAutoSignIn() {
        val authPreferences = AuthPreferences(this)
        
        // Check if user is logged in and login is still valid
        if (authPreferences.isLoggedIn() && authPreferences.isLoginValid()) {
            val userId = authPreferences.getUserId()
            val idToken = authPreferences.getIdToken()
            
            if (userId != null && idToken != null) {
                // Auto sign-in: Navigate directly to Dashboard
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("USER_ID", userId)
                intent.putExtra("ID_TOKEN", idToken)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return
            }
        }
        
        // Clear invalid login data and navigate to Login
        if (authPreferences.isLoggedIn() && !authPreferences.isLoginValid()) {
            authPreferences.logout()
        }
        
        // Navigate to Login after splash
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    
    // Animate logo appearance
    LaunchedEffect(Unit) {
        // Scale and fade in animation
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                delayMillis = 100
            )
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                delayMillis = 100
            )
        )
        
        // Wait for 2 seconds total (including animation)
        delay(1500)
        
        // Navigate to login
        onTimeout()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF22B27D), // ForkIt Green
                        Color(0xFF1E9ECD)  // ForkIt Blue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            // ForkIt Logo
            Image(
                painter = painterResource(id = R.drawable.forkit_logo),
                contentDescription = "ForkIt Logo",
                modifier = Modifier
                    .size(200.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Name
            Text(
                text = "ForkIt",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White,
                            Color.White.copy(alpha = 0.9f)
                        )
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            Text(
                text = stringResource(R.string.app_tagline),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

