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
import dev.jalves.estg.trabalhopratico.dto.UpdateTask
import dev.jalves.estg.trabalhopratico.services.TaskService
import kotlinx.coroutines.launch

@Composable
fun EditTaskDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var TaskId by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        title = { Text("Edit Task") },
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
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Status") },
                )
                OutlinedTextField(
                    value = TaskId,
                    onValueChange = { TaskId = it },
                    label = { Text("TaskId") },
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
                            val dto = UpdateTask(
                                name = name,
                                description = description,
                                status = status
                            )

                            val result = TaskService.updateTask(dto, TaskId)

                            if (result.isSuccess) {
                                Toast.makeText(context, "Task Edited!", Toast.LENGTH_SHORT).show()
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
