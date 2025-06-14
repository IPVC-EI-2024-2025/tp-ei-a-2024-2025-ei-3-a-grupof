package dev.jalves.estg.trabalhopratico.ui.views.dialogs

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import dev.jalves.estg.trabalhopratico.dto.CreateTaskDTO
import dev.jalves.estg.trabalhopratico.objects.TaskStatus
import dev.jalves.estg.trabalhopratico.services.TaskService
import kotlinx.coroutines.launch

@Composable
fun CreateTaskDialog(
    projectId: String,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Pending") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        title = { Text("Add Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Task Name") },
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                )

            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            val dto = CreateTaskDTO(
                                name = name,
                                description = description,
                                status = TaskStatus.IN_PROGRESS
                            )

                            val result = TaskService.createTask(dto, projectId)

                            if (result.isSuccess) {
                                Toast.makeText(context, "Task created!", Toast.LENGTH_SHORT).show()
                                onSubmit()
                            } else {
                                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
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
