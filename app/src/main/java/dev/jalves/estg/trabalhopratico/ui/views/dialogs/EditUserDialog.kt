package dev.jalves.estg.trabalhopratico.ui.views.dialogs

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.dto.UpdateUserDTO
import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.services.UserService
import dev.jalves.estg.trabalhopratico.ui.components.FormField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    user: User?
) {
    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var username by remember { mutableStateOf(user?.username ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(
        user?.role ?: Role.EMPLOYEE
    ) }
    var expanded by remember { mutableStateOf(false) }

    var usernameChanged by remember { mutableStateOf(user != null) }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val isEmailValid = email.isBlank() || Patterns.EMAIL_ADDRESS.matcher(email).matches()

    val isFormValid = displayName.isNotBlank() &&
            username.isNotBlank() &&
            email.isNotBlank() &&
            isEmailValid &&
            (user != null || password.isNotBlank()) // Only require password for new users

    LaunchedEffect(displayName) {
        if (!usernameChanged && displayName.isNotEmpty() && user == null) {
            username = displayName.lowercase()
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

    AlertDialog(
        title = {
            Text(text = if (user != null) stringResource(R.string.edit_user) else stringResource(R.string.add_user))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = stringResource(R.string.name_setting),
                    modifier = Modifier.fillMaxWidth()
                )

                FormField(
                    value = username,
                    onValueChange = { newUsername ->
                        val filteredUsername = newUsername.replace(" ", "")
                        username = filteredUsername
                        usernameChanged = true
                    },
                    label = stringResource(R.string.username),
                    isError = usernameError != null,
                    errorMessage = usernameError,
                    modifier = Modifier.fillMaxWidth()
                )

                FormField(
                    value = email,
                    onValueChange = { email = it },
                    label = stringResource(R.string.email),
                    keyboardType = KeyboardType.Email,
                    isError = emailError != null,
                    errorMessage = emailError,
                    modifier = Modifier.fillMaxWidth()
                )

                FormField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(R.string.password),
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    isError = passwordError != null,
                    errorMessage = passwordError,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = stringResource(selectedRole.descriptionId),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.role)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Role.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(stringResource(role.descriptionId)) },
                                onClick = {
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = !isLoading && isFormValid,
                onClick = {
                    isLoading = true
                    usernameError = null
                    emailError = null
                    passwordError = null

                    scope.launch {
                        try {
                            if (user == null) {
                                val result = UserService.createUser(CreateUserDTO(
                                    displayName = displayName,
                                    email = email,
                                    username = username,
                                    password = password,
                                    role = selectedRole
                                ))

                                result.fold(
                                    onSuccess = {
                                        Toast.makeText(context, "User created successfully!", Toast.LENGTH_SHORT).show()
                                        onSubmit()
                                    },
                                    onFailure = { exception ->
                                        when (exception.message) {
                                            "duplicate_email" -> {
                                                emailError = "An account with this email already exists"
                                                isLoading = false
                                            }
                                            "duplicate_username" -> {
                                                usernameError = "This username is already taken"
                                                isLoading = false
                                            }
                                            else -> {
                                                Toast.makeText(
                                                    context,
                                                    "Error: ${exception.message ?: "Unknown error"}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                isLoading = false
                                            }
                                        }
                                    }
                                )
                            } else {
                                val result = UserService.updateUser(UpdateUserDTO(
                                    id = user.id,
                                    displayName = displayName,
                                    email = email,
                                    username = username,
                                    password = password.ifBlank { null },
                                    role = selectedRole
                                ))

                                result.fold(
                                    onSuccess = {
                                        Toast.makeText(context, "Edit successful!", Toast.LENGTH_SHORT).show()
                                        onSubmit()
                                    },
                                    onFailure = { exception ->
                                        when (exception.message) {
                                            "duplicate_email" -> {
                                                emailError = "An account with this email already exists"
                                                isLoading = false
                                            }
                                            "duplicate_username" -> {
                                                usernameError = "This username is already taken"
                                                isLoading = false
                                            }
                                            else -> {
                                                Toast.makeText(
                                                    context,
                                                    "Error: ${exception.message ?: "Unknown error"}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                isLoading = false
                                            }
                                        }
                                    }
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("EditUserDialog", "Error saving user", e)
                            Toast.makeText(context, e.message ?: "Unknown error", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.submit))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}