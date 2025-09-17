package com.example.forkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.forkit.ui.theme.ForkItTheme
import com.example.forkit.ThemeManager

class AppSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            ForkItTheme {
                AppSettingsScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    onBackPressed: () -> Unit
) {
    // Use ThemeManager for dark mode state
    val isDarkMode = ThemeManager.isDarkMode
    
    // Language state
    var selectedLanguage by remember { mutableStateOf("English") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "App Settings",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E9ECD) // ForkIt Blue
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "App Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeManager.forkItBlue
            )
            
            Text(
                text = "Customize your app experience",
                fontSize = 16.sp,
                color = ThemeManager.onBackgroundColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Appearance Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = ThemeManager.cardColor)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_appearance),
                            contentDescription = "Appearance",
                            modifier = Modifier.size(24.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(ThemeManager.forkItBlue)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Appearance",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = ThemeManager.onSurfaceColor
                            )
                            Text(
                                text = "Choose between light and dark mode",
                                fontSize = 14.sp,
                                color = ThemeManager.onBackgroundColor
                            )
                        }
                        
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { ThemeManager.toggleDarkMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF1E9ECD),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (isDarkMode) "Dark Mode" else "Light Mode",
                        fontSize = 14.sp,
                        color = if (isDarkMode) ThemeManager.forkItBlue else ThemeManager.onBackgroundColor,
                        fontWeight = if (isDarkMode) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
            
            // Language Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = ThemeManager.cardColor)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_language),
                            contentDescription = "Language",
                            modifier = Modifier.size(24.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(ThemeManager.forkItBlue)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Language",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = ThemeManager.onSurfaceColor
                            )
                            Text(
                                text = "Select your preferred language",
                                fontSize = 14.sp,
                                color = ThemeManager.onBackgroundColor
                            )
                        }
                        
                        Text(
                            text = selectedLanguage,
                            fontSize = 16.sp,
                            color = ThemeManager.forkItBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Language options coming soon",
                        fontSize = 12.sp,
                        color = ThemeManager.onBackgroundColor,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

