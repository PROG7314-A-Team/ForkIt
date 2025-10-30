package com.example.forkit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
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
import com.example.forkit.utils.AuthPreferences
import androidx.compose.ui.res.stringResource
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import androidx.fragment.app.FragmentActivity

class ProfileActivity : FragmentActivity() {
    
    private val appSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // If language was changed in AppSettingsActivity, recreate this activity
        if (result.resultCode == AppSettingsActivity.RESULT_LANGUAGE_CHANGED) {
            recreate()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Apply saved language
        LanguageManager.applyLanguage(this)
        
        val userId = intent.getStringExtra("USER_ID") ?: ""
        
        setContent {
            ForkItTheme {
                ProfileScreen(
                    userId = userId,
                    onNavigateToAppSettings = {
                        val intent = Intent(this, AppSettingsActivity::class.java)
                        appSettingsLauncher.launch(intent)
                    }
                )
            }
        }
    }
}

enum class ProfileOptionType {
    ACCOUNT, GOALS, NOTIFICATIONS, APP_SETTINGS, ABOUT
}

data class ProfileOption(
    val icon: ImageVector,
    val type: ProfileOptionType,
    val title: String,
    val description: String
)

@Composable
fun ProfileScreen(
    userId: String = "",
    onNavigateToAppSettings: () -> Unit = {}
) {
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
            type = ProfileOptionType.ACCOUNT,
            title = stringResource(R.string.account),
            description = stringResource(R.string.manage_account_settings)
        ),
        ProfileOption(
            icon = Icons.Default.Star,
            type = ProfileOptionType.GOALS,
            title = stringResource(R.string.goals),
            description = stringResource(R.string.set_daily_health_goals)
        ),
        ProfileOption(
            icon = Icons.Default.Notifications,
            type = ProfileOptionType.NOTIFICATIONS,
            title = stringResource(R.string.notifications),
            description = stringResource(R.string.configure_notifications)
        ),
        ProfileOption(
            icon = Icons.Default.Settings,
            type = ProfileOptionType.APP_SETTINGS,
            title = stringResource(R.string.app_settings),
            description = stringResource(R.string.customize_app_settings)
        ),
        ProfileOption(
            icon = Icons.Default.Info,
            type = ProfileOptionType.ABOUT,
            title = stringResource(R.string.about),
            description = stringResource(R.string.app_info_version)
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = stringResource(R.string.profile),
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
                        when (option.type) {
                            ProfileOptionType.ACCOUNT -> {
                                // Require biometric authentication before navigating to Account
                                val activity = (context as? FragmentActivity)
                                if (activity != null) {
                                    val biometricManager = BiometricManager.from(activity)
                                    val canAuth = biometricManager.canAuthenticate(
                                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                                    )

                                    if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
                                        val executor: Executor = ContextCompat.getMainExecutor(activity)
                                        val prompt = BiometricPrompt(
                                            activity,
                                            executor,
                                            object : BiometricPrompt.AuthenticationCallback() {
                                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                    super.onAuthenticationSucceeded(result)
                                                    val intent = Intent(activity, AccountActivity::class.java)
                                                    intent.putExtra("USER_ID", userId)
                                                    activity.startActivity(intent)
                                                }

                                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                    super.onAuthenticationError(errorCode, errString)
                                                    // Do nothing; stay on Profile
                                                }

                                                override fun onAuthenticationFailed() {
                                                    super.onAuthenticationFailed()
                                                    // Do nothing; user can try again
                                                }
                                            }
                                        )

                                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                            .setTitle(context.getString(R.string.account))
                                            .setSubtitle(context.getString(R.string.manage_account_settings))
                                            .setAllowedAuthenticators(
                                                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                                            )
                                            .build()

                                        prompt.authenticate(promptInfo)
                                    } else {
                                        // If device has no biometric/credential setup, block access silently
                                        // Optionally, you could show a Toast/snackbar here.
                                    }
                                }
                            }
                            ProfileOptionType.GOALS -> {
                                val intent = Intent(context, GoalsActivity::class.java)
                                intent.putExtra("USER_ID", userId)
                                context.startActivity(intent)
                            }
                            ProfileOptionType.NOTIFICATIONS -> {
                                val intent = Intent(context, NotificationsActivity::class.java)
                                intent.putExtra("USER_ID", userId)
                                context.startActivity(intent)
                            }
                            ProfileOptionType.APP_SETTINGS -> {
                                onNavigateToAppSettings()
                            }
                            ProfileOptionType.ABOUT -> {
                                val intent = Intent(context, AboutActivity::class.java)
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
                        // Clear saved login credentials
                        val authPreferences = AuthPreferences(context)
                        authPreferences.logout()
                        
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
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.logout),
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
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
