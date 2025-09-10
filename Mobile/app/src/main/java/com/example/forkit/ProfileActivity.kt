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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
//import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        setContent {
            ForkItTheme {
                ProfileScreen()
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
fun ProfileScreen() {
    val context = LocalContext.current
    
    // Handle back button press
    BackHandler {
        val intent = Intent(context, DashboardActivity::class.java)
        context.startActivity(intent)
    }
    
    val profileOptions = listOf(
        ProfileOption(
            icon = Icons.Default.Person,
            title = "Account",
            description = "Manage your account settings"
        ),
        ProfileOption(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            description = "Configure notification preferences"
        ),
        ProfileOption(
            icon = Icons.Default.Settings,
            title = "Appearance",
            description = "Customize app appearance"
        ),
        ProfileOption(
            icon = Icons.Default.Lock,
            title = "Privacy & Security",
            description = "Manage your privacy settings"
        ),
        //ProfileOption(
         //   icon = Icons.Default.QuestionAnswer,
          //  title = "Help & Support",
          //  description = "Get help and contact support"
      //  ),
        ProfileOption(
            icon = Icons.Default.Info,
            title = "About",
            description = "App information and version"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
                    context.startActivity(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22B27D) // ForkIt Green
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
                    onClick = { /* Handle option click */ }
                )
            }
            
            // Bottom spacing
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                tint = Color(0xFF1E9ECD), // ForkIt Blue
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
                    color = Color(0xFF1E9ECD) // ForkIt Blue
                )
                Text(
                    text = option.description,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
            
            // Arrow
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Black,
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
