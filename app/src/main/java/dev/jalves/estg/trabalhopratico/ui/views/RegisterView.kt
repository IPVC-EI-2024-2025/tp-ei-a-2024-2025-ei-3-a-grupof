package dev.jalves.estg.trabalhopratico.ui.views

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import dev.jalves.estg.trabalhopratico.services.AuthService
import dev.jalves.estg.trabalhopratico.ui.components.FormField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterView(
    onReturn: () -> Unit,
    onSuccessfulRegister: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var usernameChanged by remember { mutableStateOf(false) }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    val isEmailValid = email.isBlank() || Patterns.EMAIL_ADDRESS.matcher(email).matches()

    val isFormValid = name.isNotBlank() &&
            username.isNotBlank() &&
            email.isNotBlank() &&
            isEmailValid &&
            password.isNotBlank()

    LaunchedEffect(name) {
        if (!usernameChanged && name.isNotEmpty()) {
            username = name.lowercase()
                .replace(" ", ".")
                .replace(Regex("[^a-z0-9.]"), "")
        }
    }

    LaunchedEffect(username) {
        if (usernameError != null) usernameError = null
    }

    LaunchedEffect(email) {
        if (emailError != null) emailError = null
        if (email.isNotBlank() && !isEmailValid) {
            emailError = "Please enter a valid email address"
        }
    }

    LaunchedEffect(password) {
        if (passwordError != null) passwordError = null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onReturn
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Return")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Sign up",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            FormField(
                value = name,
                onValueChange = { name = it },
                label = "Display name"
            )

            FormField(
                value = username,
                onValueChange = { newUsername ->
                    val filteredUsername = newUsername.replace(" ", "")
                    username = filteredUsername
                    usernameChanged = true
                },
                label = "Username",
                isError = usernameError != null,
                errorMessage = usernameError
            )

            FormField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
                isError = emailError != null,
                errorMessage = emailError
            )

            FormField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                isError = passwordError != null,
                errorMessage = passwordError
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    modifier = Modifier.height(64.dp).padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    enabled = !isLoading && isFormValid,
                    onClick = {
                        isLoading = true
                        usernameError = null
                        emailError = null
                        passwordError = null

                        scope.launch {
                            val result = AuthService.signUp(CreateUserDTO(
                                name, email, username, password
                            ))

                            result.fold(
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Account created successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onSuccessfulRegister()
                                },
                                onFailure = { exception ->
                                    Log.e("SignUp", "Failed to sign up", exception)

                                    when (exception.message) {
                                        "duplicate_email" -> {
                                            emailError = "An account with this email already exists"
                                        }
                                        "duplicate_username" -> {
                                            usernameError = "This username is already taken"
                                        }
                                        else -> {
                                            Toast.makeText(
                                                context,
                                                "Registration failed: ${exception.message ?: "Unknown error"}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            )

                            isLoading = false
                        }
                    },
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Submit")
                    }
                }
            }
        }
    }
}