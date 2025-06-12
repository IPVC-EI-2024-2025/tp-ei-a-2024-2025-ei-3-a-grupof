package dev.jalves.estg.trabalhopratico.ui.views.dialogs

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.UserCrud
import kotlinx.coroutines.launch

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
    var role by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        title = {
            Text(text = if (user != null) "Edit user" else "Add user")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display name") },
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                )
                if (user == null) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                    }
                if (user == null) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            val dto = CreateUserDTO(
                                displayName = displayName,
                                email = email,
                                username = username,
                                password = password
                            )

                            if (user == null) {
                                val result = UserCrud.createUser(dto)

                                if (result.isSuccess) {
                                    Toast.makeText(context, "User created!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                                    Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                val result = UserCrud.updateUser(user.id, dto)

                                if (result.isSuccess) {
                                    Toast.makeText(context, "User created!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                                    Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                                }
                            }

                            onSubmit()
                        } catch (e: Exception) {
                            Log.e("EditUserDialog", "Error saving user", e)
                            Toast.makeText(context, e.message ?: "Unknown error", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }


        }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
