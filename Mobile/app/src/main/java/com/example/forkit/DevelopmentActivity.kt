package com.example.forkit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.ui.theme.ForkItTheme
import com.example.forkit.ui.dashboard.DashboardActivity


class DevelopmentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForkItTheme {
                DevelopmentScreen()
            }
        }
    }
}

data class ScreenItem(
    val name: String,
    val description: String,
    val activityClass: Class<*>
)

@Composable
fun DevelopmentScreen() {
    val context = LocalContext.current
    
    val screens = listOf(
        ScreenItem(
            name = "Dashboard Screen",
            description = "Main app dashboard with calorie tracking and nutrition overview",
            activityClass = DashboardActivity::class.java
        ),
        ScreenItem(
            name = "Login Screen",
            description = "User authentication screen with login and sign up options",
            activityClass = LoginActivity::class.java
        ),
        ScreenItem(
            name = "Sign Up Screen",
            description = "User registration screen with form fields and account creation",
            activityClass = SignUpActivity::class.java
        ),
        ScreenItem(
            name = "Sign In Screen",
            description = "User login screen with email/password fields and Google login",
            activityClass = SignInActivity::class.java
        ),
        ScreenItem(
            name = "Biometric Screen",
            description = "Screen requires biometric when opened",
            activityClass = AccountActivity::class.java
        ),
        ScreenItem(
            name = "API Testing Screen",
            description = "Test all API endpoints with input fields and response display",
            activityClass = TestingActivity::class.java
        ),
        ScreenItem(
            name = "Habits Screen",
            description = "Habit tracking and management with time-based filtering",
            activityClass = HabitsActivity::class.java
        ),
        ScreenItem(
            name = "Add Habit Screen",
            description = "Create new habits with repeat options and reminders",
            activityClass = AddHabitActivity::class.java
        ),
        ScreenItem(
            name = "Goals Screen",
            description = "Set and manage daily health and fitness goals",
            activityClass = GoalsActivity::class.java
        )

        // Add more screens here as you develop them
        // ScreenItem(
        //     name = "Profile Screen",
        //     description = "User profile management",
        //     activityClass = ProfileActivity::class.java
        // )
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "ForkIt Development",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        }
        
        item {
            Text(
                text = "Navigate to different screens for development and testing",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Screen Navigation List
        items(screens) { screen ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                onClick = {
                    val intent = Intent(context, screen.activityClass)
                    context.startActivity(intent)
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = screen.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = screen.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
        
        // Footer
        item {
            Text(
                text = "Development Mode",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DevelopmentScreenPreview() {
    ForkItTheme {
        DevelopmentScreen()
    }
}
