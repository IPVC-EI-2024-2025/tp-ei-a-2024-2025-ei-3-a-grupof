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
import dev.jalves.estg.trabalhopratico.dto.CreateUserPerformanceDTO
import dev.jalves.estg.trabalhopratico.objects.TaskStatus
import dev.jalves.estg.trabalhopratico.objects.UserPerformance
import dev.jalves.estg.trabalhopratico.services.TaskService
import dev.jalves.estg.trabalhopratico.services.UserService
import kotlinx.coroutines.launch

@Composable
fun UserPerformanceDialog(
    UserId: String,
    ManagerId: String,
    ProjectId: String,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    var rating by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var isRatingError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        title = { Text("Add UserPerformance") },
        text = {
            Column {
                OutlinedTextField(
                    value = rating,
                    onValueChange = {
                        rating = it
                        isRatingError = it.toFloatOrNull() == null
                    },
                    label = { Text("Rating") },
                    isError = isRatingError
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                )

                if (isRatingError && rating.isNotEmpty()) {
                    Text("Please enter a valid number", color = androidx.compose.ui.graphics.Color.Red)
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val ratingFloat = rating.toFloatOrNull()
                    if (ratingFloat == null) {
                        isRatingError = true
                        Toast.makeText(context, "Invalid rating!", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }

                    isLoading = true
                    scope.launch {
                        try {
                            val dto = CreateUserPerformanceDTO(
                                projectId = ProjectId,
                                userId = UserId,
                                rating = ratingFloat,
                                comments = comment,
                                evaluatedBy = ManagerId
                            )

                            val result = UserService.createUserPerformance(dto)

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
