package com.example.forkit

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
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
import com.example.forkit.ThemeMode
import com.example.forkit.LanguageManager
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import android.content.Intent

class AppSettingsActivity : AppCompatActivity() {
    companion object {
        const val RESULT_LANGUAGE_CHANGED = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize LanguageManager
        LanguageManager.initialize(this)
        LanguageManager.applyLanguage(this)
        
        setContent {
            ForkItTheme {
                AppSettingsScreen(
                    onBackPressed = { finish() },
                    onLanguageChanged = {
                        // Notify caller and close so it can recreate immediately
                        setResult(RESULT_LANGUAGE_CHANGED)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    onBackPressed: () -> Unit,
    onLanguageChanged: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Use ThemeManager for theme mode state
    val currentThemeMode = ThemeManager.themeMode
    
    // Language state from LanguageManager
    val currentLanguage = LanguageManager.currentLanguage
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_settings),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
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
                text = stringResource(R.string.app_settings),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Text(
                text = stringResource(R.string.customize_app_settings),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Appearance Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            contentDescription = stringResource(R.string.app_settings),
                            modifier = Modifier.size(24.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.app_settings),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.customize_app_settings),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Theme Mode Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // System Default Button
                        ThemeOptionButton(
                            text = stringResource(R.string.app_settings),
                            isSelected = currentThemeMode == ThemeMode.SYSTEM,
                            onClick = { ThemeManager.saveThemeMode(context, ThemeMode.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Light Button
                        ThemeOptionButton(
                            text = "Light", // Keep as "Light" - universal
                            isSelected = currentThemeMode == ThemeMode.LIGHT,
                            onClick = { ThemeManager.saveThemeMode(context, ThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Dark Button
                        ThemeOptionButton(
                            text = "Dark", // Keep as "Dark" - universal
                            isSelected = currentThemeMode == ThemeMode.DARK,
                            onClick = { ThemeManager.saveThemeMode(context, ThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when (currentThemeMode) {
                            ThemeMode.SYSTEM -> "System"
                            ThemeMode.LIGHT -> "Light"
                            ThemeMode.DARK -> "Dark"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
            
            // Language Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Language",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Select your preferred language",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        Text(
                            text = currentLanguage.displayName,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Language Options - Grid Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // English Button
                        ThemeOptionButton(
                            text = "English",
                            isSelected = currentLanguage == LanguageManager.Language.ENGLISH,
                            onClick = {
                                LanguageManager.saveLanguage(context, LanguageManager.Language.ENGLISH)
                                onLanguageChanged()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Afrikaans Button
                        ThemeOptionButton(
                            text = "Afrikaans",
                            isSelected = currentLanguage == LanguageManager.Language.AFRIKAANS,
                            onClick = {
                                LanguageManager.saveLanguage(context, LanguageManager.Language.AFRIKAANS)
                                onLanguageChanged()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Zulu Button
                        ThemeOptionButton(
                            text = "isiZulu",
                            isSelected = currentLanguage == LanguageManager.Language.ZULU,
                            onClick = {
                                LanguageManager.saveLanguage(context, LanguageManager.Language.ZULU)
                                onLanguageChanged()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOptionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFF1E9ECD) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

