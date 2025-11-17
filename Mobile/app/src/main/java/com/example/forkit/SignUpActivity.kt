package com.example.forkit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.example.forkit.data.RetrofitClient
import com.example.forkit.data.models.GoogleRegisterRequest
import com.example.forkit.ui.theme.ForkItTheme
import com.example.forkit.utils.navigateToDashboard
import com.example.forkit.utils.navigateToOnboarding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {
    private lateinit var googleFallbackLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        googleFallbackLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleLegacyGoogleSignUpResult(result)
        }
        
        // Load theme preference
        ThemeManager.loadThemeMode(this)
        
        setContent {
            ForkItTheme {
                SignUpScreen()
            }
        }
    }

    fun startGoogleSignUpFlow() {
        lifecycleScope.launch {
            signInWithGoogle(this@SignUpActivity, googleFallbackLauncher)
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
                        val trimmedEmail = email.trim()
                        if (trimmedEmail.isEmpty()) {
                            message = "Please enter an email address so we can create your account"
                            return@clickable
                        }

                        if (password != confirmPassword) {
                            message = "The passwords you entered do not match. Please make sure both password fields are identical"
                            return@clickable
                        }

                        if (password.length < 6) {
                            message = "Your password must be at least 6 characters long"
                            return@clickable
                        }

                        isLoading = true
                        message = ""

                        firebaseAuth
                            .createUserWithEmailAndPassword(trimmedEmail, password)
                            .addOnSuccessListener { result ->
                                val user = result.user
                                if (user != null) {
                                    user.getIdToken(true)
                                        .addOnSuccessListener { tokenResult ->
                                            val idToken = tokenResult.token
                                            if (idToken.isNullOrBlank()) {
                                                message = "We couldn't verify your new account. Please try signing up again."
                                                isLoading = false
                                                return@addOnSuccessListener
                                            }

                                            context.navigateToOnboarding(
                                                user.uid,
                                                idToken,
                                                trimmedEmail
                                            )
                                            message = "Account created successfully! Welcome to ForkIt"
                                            isLoading = false
                                        }
                                        .addOnFailureListener { tokenError ->
                                            Log.e("Auth", "Failed to fetch ID token for new account", tokenError)
                                            message = "Unable to verify your new account. Please try again."
                                            isLoading = false
                                        }
                                } else {
                                    message = "Account creation failed. Please try again."
                                    isLoading = false
                                }
                            }
                            .addOnFailureListener { error ->
                                Log.e("Auth", "Email sign-up failed", error)
                                message = error.toReadableSignUpMessage()
                                isLoading = false
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
                        (context as? SignUpActivity)?.startGoogleSignUpFlow()
                            ?: Toast.makeText(context, "Unable to start Google sign-up", Toast.LENGTH_SHORT).show()
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

private fun Exception.toReadableSignUpMessage(): String {
    return when (this) {
        is FirebaseAuthUserCollisionException -> "An account with this email already exists. Please sign in instead."
        is FirebaseAuthWeakPasswordException -> "Your password must be at least 6 characters long."
        is IllegalArgumentException -> "Please enter a valid email address."
        else -> localizedMessage ?: "We couldn't create your account. Please try again."
    }
}

private fun SignUpActivity.launchLegacyGoogleSignUp(launcher: ActivityResultLauncher<Intent>) {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(this, gso)
    googleSignInClient.signOut()
    launcher.launch(googleSignInClient.signInIntent)
}

private fun SignUpActivity.handleLegacyGoogleSignUpResult(result: ActivityResult) {
    if (result.resultCode != Activity.RESULT_OK) {
        Toast.makeText(this, "Google sign-in was cancelled", Toast.LENGTH_SHORT).show()
        return
    }

    val data = result.data ?: run {
        Toast.makeText(this, "Google sign-in was cancelled", Toast.LENGTH_SHORT).show()
        return
    }

    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
    try {
        val account = task.getResult(ApiException::class.java)
        val idToken = account.idToken
        if (idToken.isNullOrBlank()) {
            Toast.makeText(this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        completeGoogleSignUp(credential)
    } catch (e: ApiException) {
        Log.e("Auth", "Legacy Google sign-in failed", e)
        Toast.makeText(this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
    }
}

private fun SignUpActivity.completeGoogleSignUp(credential: AuthCredential) {
    val activity = this
    Firebase.auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, "Firebase auth failed", Toast.LENGTH_SHORT).show()
                Log.e("Auth", "Firebase auth failed", task.exception)
                return@addOnCompleteListener
            }

            val user = Firebase.auth.currentUser
            if (user == null) {
                Toast.makeText(this, "No Google account information found", Toast.LENGTH_SHORT).show()
                Log.e("Auth", "Firebase auth succeeded but user is null")
                return@addOnCompleteListener
            }

            val email = user.email
            if (email.isNullOrBlank()) {
                Toast.makeText(this, "No email found in Firebase user", Toast.LENGTH_SHORT).show()
                Log.e("Auth", "No email attached to Firebase Google account")
                return@addOnCompleteListener
            }

            user.getIdToken(true)
                .addOnSuccessListener { tokenResult ->
                    val firebaseIdToken = tokenResult.token
                    if (firebaseIdToken.isNullOrBlank()) {
                        Toast.makeText(this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
                        Log.e("Auth", "Firebase ID token is null or blank")
                        return@addOnSuccessListener
                    }

                    lifecycleScope.launch {
                        val registrationSucceeded = ensureGoogleUserRegistered(email, firebaseIdToken)
                        if (!registrationSucceeded) {
                            Firebase.auth.signOut()
                            return@launch
                        }

                        activity.navigateToOnboarding(user.uid, firebaseIdToken, email)
                        Toast.makeText(
                            activity,
                            "Account created successfully! Welcome to ForkIt",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Auth", "Failed to fetch Firebase ID token", exception)
                    Toast.makeText(this, "Failed to complete Google sign-in. Please try again.", Toast.LENGTH_SHORT).show()
                }
        }
}

private suspend fun SignUpActivity.ensureGoogleUserRegistered(
    email: String,
    firebaseIdToken: String
): Boolean {
    return try {
        val response = withContext(Dispatchers.IO) {
            RetrofitClient.api.registerGoogleUser(
                GoogleRegisterRequest(
                    email = email,
                    idToken = firebaseIdToken
                )
            )
        }

        if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true) {
                Log.d("Auth", "Google user registered in backend with uid=${body.uid}")
                true
            } else {
                val message = body?.message ?: "Unable to create your ForkIt account with Google."
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                false
            }
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Unable to create your ForkIt account with Google."
            Log.e("Auth", "registerGoogleUser failed: $errorMsg")
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            false
        }
    } catch (e: Exception) {
        Log.e("Auth", "Exception while registering Google user", e)
        Toast.makeText(
            this,
            "We couldn't finish creating your ForkIt account. Please try again.",
            Toast.LENGTH_LONG
        ).show()
        false
    }
}

private suspend fun signInWithGoogle(
    activity: SignUpActivity,
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
        val result = withContext(Dispatchers.IO) {
            credentialManager.getCredential(
                context = activity,
                request = request
            )
        }

        val googleIdTokenCredential =
            GoogleIdTokenCredential.createFrom(result.credential.data)
        val idToken = googleIdTokenCredential.idToken

        @Suppress("SENSELESS_COMPARISON")
        if (idToken == null) {
            Toast.makeText(activity, "Google sign-in failed: No ID token", Toast.LENGTH_SHORT).show()
            Log.e("Auth", "Google sign-in failed: No ID token")
            return
        }

        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        activity.completeGoogleSignUp(firebaseCredential)

    } catch (e: NoCredentialException) {
        activity.launchLegacyGoogleSignUp(fallbackLauncher)
    } catch (e: GetCredentialException) {
        Toast.makeText(activity, "Google sign-in failed. Please try again or use email sign-up", Toast.LENGTH_SHORT).show()
        Log.e("Auth", "Google Sign-In failed: ${e.message}", e)
    } catch (e: Exception) {
        Toast.makeText(activity, "An unexpected error occurred. Please try again", Toast.LENGTH_SHORT).show()
        Log.e("Auth", "Unexpected error during sign-in", e)
    }
}
