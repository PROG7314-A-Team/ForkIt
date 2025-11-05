package com.example.forkit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.forkit.ui.theme.ForkItTheme
import com.example.forkit.data.models.RegisterRequest
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.GoogleRegisterRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Load theme preference
        ThemeManager.loadThemeMode(this)
        
        setContent {
            ForkItTheme {
                SignUpScreen()
            }
        }
    }
}


@Composable
fun SignUpScreen(/*navController: NavController*/) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = stringResource(id = R.string.start_tracking),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF22B27D),
                            Color(0xFF1E9ECD)
                        )
                    )
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = stringResource(id = R.string.create_account_here),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.email)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E9ECD),
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    focusedLabelColor = Color(0xFF1E9ECD),
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.password)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E9ECD),
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    focusedLabelColor = Color(0xFF1E9ECD),
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(id = R.string.confirm_password)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E9ECD),
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    focusedLabelColor = Color(0xFF1E9ECD),
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Text(
                            text = if (confirmPasswordVisible) "Hide" else "Show",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up Button
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF22B27D),
                                Color(0xFF1E9ECD)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        if (password != confirmPassword) {
                            message = "The passwords you entered do not match. Please make sure both password fields are identical"
                            return@clickable
                        }

                        isLoading = true
                        message = ""

                        scope.launch {
                            try {
                                val response = RetrofitClient.api.registerUser(
                                    RegisterRequest(email, password)
                                )
                                if (response.isSuccessful) {
                                    val body = response.body()
                                    message = body?.message ?: "Account created successfully! Welcome to ForkIt"
                                    // Navigate to onboarding flow for new users
                                    val intent = Intent(context, TellUsAboutYourselfActivity::class.java)
                                    intent.putExtra("USER_ID", body?.uid)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                    (context as? ComponentActivity)?.finish()
                                } else {
                                    message = "Unable to create account. Please check your email format and try again"
                                }
                            } catch (e: Exception) {
                                message = "Unable to connect to the server. Please check your internet connection and try again"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(
                        text = stringResource(id = R.string.sign_up),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show message
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = if (message.contains("success", ignoreCase = true)) Color(0xFF22B27D) else Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Login Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.already_have_account),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.login),
                    color = Color(0xFF1E9ECD),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        val intent = Intent(context, SignInActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Google Sign Up Button
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF1E9ECD),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                    /* TODO: Add Google sign up */
                        scope.launch{
                            signInWithGoogle(context/*, navController*/)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.sign_up_with_google),
                        color = Color(0xFF1E9ECD),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    ForkItTheme {
        SignUpScreen()
    }
}


private suspend fun signInWithGoogle(context: Context) {
    val credentialManager = CredentialManager.create(context)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(context.getString(com.example.forkit.R.string.default_web_client_id))
        .setFilterByAuthorizedAccounts(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(
            context = context,
            request = request
        )

        val googleIdTokenCredential =
            GoogleIdTokenCredential.createFrom(result.credential.data)
        val idToken = googleIdTokenCredential.idToken

        @Suppress("SENSELESS_COMPARISON")
        if (idToken == null) {
            Toast.makeText(context, "Google sign-in failed: No ID token", Toast.LENGTH_SHORT).show()
            Log.e("Auth", "Google sign-in failed: No ID token")
            return
        }

        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = Firebase.auth.currentUser
                    val email = user?.email

                    if (email != null) {
                        Log.d("Auth", "Firebase sign-in successful: $email")

                        // Safely use lifecycleScope for coroutine
                        (context as? ComponentActivity)?.lifecycleScope?.launch {
                            try {
                                // Call backend to register Google user
                                val response = RetrofitClient.api.registerGoogleUser(
                                    GoogleRegisterRequest(email)
                                )

                                if (response.isSuccessful) {
                                    val body = response.body()
                                    val success = body?.success ?: false
                                    val message = body?.message ?: "Account created successfully! Welcome to ForkIt"
                                    val uid = body?.uid

                                    Log.d(
                                        "Auth",
                                        "Google user registered successfully: success=$success, message=$message, userId=$uid"
                                    )

                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                                    // Navigate to onboarding flow for new users
                                    val intent = Intent(context, TellUsAboutYourselfActivity::class.java)
                                    intent.putExtra("USER_ID", body?.uid)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                    (context as? ComponentActivity)?.finish()

                                } else {
                                    val errorMsg =
                                        response.errorBody()?.string() ?: "Unknown registration error"
                                    Log.e("Auth", "Google registration failed: $errorMsg")
                                    Toast.makeText(
                                        context,
                                        "Registration failed: $errorMsg",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.e("Auth", "Error registering Google user", e)
                                Toast.makeText(
                                    context,
                                    "Error registering Google user: ${e.localizedMessage}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "No email found in Firebase user", Toast.LENGTH_SHORT)
                            .show()
                        Log.e("Auth", "No email found in Firebase user")
                    }
                } else {
                    Toast.makeText(context, "Firebase auth failed", Toast.LENGTH_SHORT).show()
                    Log.e("Auth", "Firebase auth failed", task.exception)
                }
            }

    } catch (e: GetCredentialException) {
        Toast.makeText(context, "Google sign-in failed. Please try again or use email sign-up", Toast.LENGTH_SHORT).show()
        Log.e("Auth", "Google Sign-In failed: ${e.message}", e)
    } catch (e: Exception) {
        Toast.makeText(context, "An unexpected error occurred. Please try again", Toast.LENGTH_SHORT).show()
        Log.e("Auth", "Unexpected error during sign-in", e)
    }
}
