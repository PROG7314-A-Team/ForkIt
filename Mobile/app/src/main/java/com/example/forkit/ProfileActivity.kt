package com.example.forkit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
//import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forkit.ui.theme.ForkItTheme

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val userId = intent.getStringExtra("USER_ID") ?: ""
        
        setContent {
            ForkItTheme {
                ProfileScreen(userId = userId)
            }
        }
    }
}

data class ProfileOption(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
fun ProfileScreen(userId: String = "") {
    val context = LocalContext.current
    
    // Handle back button press
    BackHandler {
        val intent = Intent(context, DashboardActivity::class.java)
        intent.putExtra("USER_ID", userId)
        context.startActivity(intent)
    }
    
    val profileOptions = listOf(
        ProfileOption(
            icon = Icons.Default.Person,
            title = "Account",
            description = "Manage your account settings"
        ),
        ProfileOption(
            icon = Icons.Default.Star,
            title = "Goals",
            description = "Set your daily health and fitness goals"
        ),
        ProfileOption(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            description = "Configure notification preferences"
        ),
        ProfileOption(
            icon = Icons.Default.Settings,
            title = "App Settings",
            description = "Customize app settings"
        ),
        ProfileOption(
            icon = Icons.Default.Info,
            title = "About",
            description = "App information and version"
        ),
        ProfileOption(
            icon = Icons.Default.Build,
            title = "Developer Tools",
            description = "Access development and testing features"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    val intent = Intent(context, DashboardActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    context.startActivity(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary // ForkIt Green
            )
        }
        
        // Profile Options List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(profileOptions) { option ->
                ProfileOptionCard(
                    option = option,
                    onClick = { 
                        when (option.title) {
                            "Account" -> {
                                val intent = Intent(context, AccountActivity::class.java)
                                context.startActivity(intent)
                            }
                            "Goals" -> {
                                val intent = Intent(context, GoalsActivity::class.java)
                                intent.putExtra("USER_ID", userId)
                                context.startActivity(intent)
                            }
                            "Notifications" -> {
                                val intent = Intent(context, NotificationsActivity::class.java)
                                context.startActivity(intent)
                            }
                            "App Settings" -> {
                                val intent = Intent(context, AppSettingsActivity::class.java)
                                context.startActivity(intent)
                            }
                            "About" -> {
                                val intent = Intent(context, AboutActivity::class.java)
                                context.startActivity(intent)
                            }
                            "Developer Tools" -> {
                                val intent = Intent(context, DevelopmentActivity::class.java)
                                context.startActivity(intent)
                            }
                        }
                    }
                )
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Logout Button
            item {
                Button(
                    onClick = {
                        // Navigate to Login screen and clear back stack
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFE53935), // Red
                                        Color(0xFFD32F2F)  // Darker red
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Logout",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            // Final bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileOptionCard(
    option: ProfileOption,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = option.icon,
                contentDescription = option.title,
                tint = MaterialTheme.colorScheme.secondary, // ForkIt Blue
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Title and Description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = option.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary // ForkIt Blue
                )
                Text(
                    text = option.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Arrow
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ForkItTheme {
        ProfileScreen()
    }
}
