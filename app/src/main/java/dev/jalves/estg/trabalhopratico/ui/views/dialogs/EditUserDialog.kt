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
import dev.jalves.estg.trabalhopratico.services.UserCrud
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun EditUserDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    user: UserInfo?
) {
    var displayName by remember { mutableStateOf(user?.userMetadata?.get("display_name")?.jsonPrimitive?.content ?: "") }
    var username by remember { mutableStateOf(user?.userMetadata?.get("username")?.jsonPrimitive?.content ?: "") }
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
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
                Text("TODO: Add dropdown for role")
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
                                // Create new user
                                UserCrud.CreateUser(dto)
                                Toast.makeText(context, "User created successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                // Update existing user
                                UserCrud.UpdateUser(dto, user.id)
                                Toast.makeText(context, "User updated successfully!", Toast.LENGTH_SHORT).show()
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
