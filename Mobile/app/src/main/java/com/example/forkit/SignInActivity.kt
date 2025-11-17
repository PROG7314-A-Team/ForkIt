package com.example.forkit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.forkit.ui.theme.ForkItTheme
import com.example.forkit.utils.navigateToDashboard
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val GOOGLE_TAG = "GoogleSignIn"

class SignInActivity : AppCompatActivity() {
    private lateinit var googleFallbackLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        googleFallbackLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleLegacyGoogleResult(result)
        }
        
        // Load theme preference
        ThemeManager.loadThemeMode(this)
        
        val prefilledEmail = intent.getStringExtra("EMAIL") ?: ""
        
        setContent {
            ForkItTheme {
                SignInScreen(prefilledEmail = prefilledEmail)
            }
        }
    }

    fun startGoogleSignInFlow() {
        lifecycleScope.launch {
            Log.d(GOOGLE_TAG, "Starting Credential Manager Google sign-in flow")
            signInWithGoogle(this@SignInActivity, googleFallbackLauncher)
        }
    }
}

@Composable
fun SignInScreen(prefilledEmail: String = "") {
    val context = LocalContext.current
    var email by remember { mutableStateOf(prefilledEmail) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val firebaseAuth = remember { FirebaseAuth.getInstance() }

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

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Box(
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
                        val trimmedEmail = email.trim()
                        if (trimmedEmail.isEmpty() || password.isEmpty()) {
                            message = "Please enter both your email address and password"
                            return@clickable
                        }

                        isLoading = true
                        message = ""

                        firebaseAuth.signInWithEmailAndPassword(trimmedEmail, password)
                            .addOnSuccessListener { result ->
                                val user = result.user
                                if (user != null) {
                                    user.getIdToken(true)
                                        .addOnSuccessListener { tokenResult ->
                                            val token = tokenResult.token
                                            if (token.isNullOrBlank()) {
                                                message = "Unable to verify your credentials. Please try again."
                                                isLoading = false
                                                return@addOnSuccessListener
                                            }

                                            context.navigateToDashboard(
                                                user.uid,
                                                token,
                                                trimmedEmail
                                            )
                                            message = "Welcome back! You have successfully signed in"
                                            isLoading = false
                                        }
                                        .addOnFailureListener { tokenError ->
                                            Log.e("Auth", "Failed to fetch Firebase ID token", tokenError)
                                            message = "Unable to verify your credentials. Please try again."
                                            isLoading = false
                                        }
                                } else {
                                    message = "Unable to complete sign-in. Please try again."
                                    isLoading = false
                                }
                            }
                            .addOnFailureListener { error ->
                                Log.e("Auth", "Email sign-in failed", error)
                                message = error.toReadableMessage()
                                isLoading = false
                            }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(
                        text = stringResource(id = R.string.login),
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

            // Sign Up Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.dont_have_account),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.sign_up),
                    color = Color(0xFF1E9ECD),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        val intent = Intent(context, SignUpActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Google Login Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF1E9ECD),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        (context as? SignInActivity)?.startGoogleSignInFlow()
                            ?: Toast.makeText(context, "Unable to start Google sign-in", Toast.LENGTH_SHORT).show()
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
                        text = stringResource(id = R.string.continue_with_google),
                        color = Color(0xFF1E9ECD),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


private suspend fun signInWithGoogle(
    activity: SignInActivity,
    fallbackLauncher: ActivityResultLauncher<Intent>
) {
    val credentialManager = CredentialManager.create(activity)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(activity.getString(R.string.default_web_client_id))
        .setFilterByAuthorizedAccounts(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        Log.d(GOOGLE_TAG, "Requesting Google credential via Credential Manager")
        val result = withContext(Dispatchers.IO) {
            credentialManager.getCredential(activity, request)
        }

        val googleIdTokenCredential =
            GoogleIdTokenCredential.createFrom(result.credential.data)
        val idToken = googleIdTokenCredential.idToken

        @Suppress("SENSELESS_COMPARISON")
        if (idToken == null) {
            Toast.makeText(activity, "Google sign-in failed: No ID token", Toast.LENGTH_SHORT).show()
            Log.w(GOOGLE_TAG, "Credential Manager returned null ID token")
            return
        }

        Log.d(GOOGLE_TAG, "Received Google ID token from Credential Manager")
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        activity.completeGoogleSignIn(firebaseCredential)

    } catch (e: NoCredentialException) {
        Log.w(GOOGLE_TAG, "Credential Manager had no stored credentials, launching fallback", e)
        activity.launchLegacyGoogleSignIn(fallbackLauncher)
    } catch (e: GetCredentialException) {
        Toast.makeText(activity, "❌ Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
        Log.e(GOOGLE_TAG, "Google Sign-In failed via Credential Manager", e)
    } catch (e: Exception) {
        Toast.makeText(activity, "❌ Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
        Log.e(GOOGLE_TAG, "Unexpected error during Google sign-in", e)
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    ForkItTheme {
        SignInScreen()
    }
}

private fun Exception.toReadableMessage(): String {
    return when (this) {
        is FirebaseAuthInvalidUserException -> "We couldn't find an account with that email address."
        is FirebaseAuthInvalidCredentialsException -> "The email or password you entered is incorrect. Please try again."
        else -> localizedMessage ?: "Something went wrong. Please try again."
    }
}

private fun SignInActivity.launchLegacyGoogleSignIn(launcher: ActivityResultLauncher<Intent>) {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(this, gso)
    Log.d(GOOGLE_TAG, "Launching legacy Google Sign-In intent")
    googleSignInClient.signOut()
    launcher.launch(googleSignInClient.signInIntent)
}

private fun SignInActivity.handleLegacyGoogleResult(result: ActivityResult) {
    if (result.resultCode != Activity.RESULT_OK) {
        Log.w(GOOGLE_TAG, "Legacy Google Sign-In cancelled, resultCode=${result.resultCode}")
        Toast.makeText(this, "Google sign-in was cancelled", Toast.LENGTH_SHORT).show()
        return
    }

    val data = result.data ?: run {
        Log.w(GOOGLE_TAG, "Legacy Google Sign-In returned null intent")
        Toast.makeText(this, "Google sign-in was cancelled", Toast.LENGTH_SHORT).show()
        return
    }

    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
    try {
        val account = task.getResult(ApiException::class.java)
        val idToken = account.idToken
        if (idToken.isNullOrBlank()) {
            Log.e(GOOGLE_TAG, "Legacy Google Sign-In returned empty ID token")
            Toast.makeText(this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d(GOOGLE_TAG, "Legacy Google Sign-In succeeded, passing credential to Firebase")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        completeGoogleSignIn(credential)
    } catch (e: ApiException) {
        Log.e(GOOGLE_TAG, "Legacy Google sign-in failed", e)
        Toast.makeText(this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
    }
}

private fun SignInActivity.completeGoogleSignIn(credential: AuthCredential) {
    Firebase.auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val emailAddress = user.email
                    if (emailAddress.isNullOrBlank()) {
                        Log.e(GOOGLE_TAG, "Firebase user missing email after Google sign-in")
                        Toast.makeText(this, "❌ No email found in Google account", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }
                    Log.d(GOOGLE_TAG, "Firebase sign-in success, fetching ID token")
                    user.getIdToken(true)
                        .addOnSuccessListener { tokenResult ->
                            val firebaseIdToken = tokenResult.token
                            if (firebaseIdToken.isNullOrBlank()) {
                                Log.e(GOOGLE_TAG, "Firebase returned empty ID token after Google sign-in")
                                Toast.makeText(this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }
                            Log.d(GOOGLE_TAG, "Firebase ID token retrieved, navigating to dashboard")
                            navigateToDashboard(
                                user.uid,
                                firebaseIdToken,
                                emailAddress
                            )
                            Toast.makeText(this, "Welcome back! You have successfully signed in with Google", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { tokenError ->
                            Log.e(GOOGLE_TAG, "Failed to get Firebase ID token", tokenError)
                            Toast.makeText(this, "❌ Authentication failed. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e(GOOGLE_TAG, "Firebase sign-in task succeeded but currentUser is null")
                    Toast.makeText(this, "❌ No account information returned from Google", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "❌ Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
                Log.e(GOOGLE_TAG, "Firebase Google sign-in task failed", task.exception)
            }
        }
}
