package dev.jalves.estg.trabalhopratico.ui.views.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.jalves.estg.trabalhopratico.dto.CreateProjectDTO
import dev.jalves.estg.trabalhopratico.dto.ProjectDTO
import dev.jalves.estg.trabalhopratico.dto.UpdateProjectDTO
import dev.jalves.estg.trabalhopratico.services.ProjectService
import dev.jalves.estg.trabalhopratico.ui.components.DatePickerInput
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProjectDialog(
    onDismiss: () -> Unit,
    project: ProjectDTO? = null
) {
    val scope = rememberCoroutineScope()

    var title = "New project"

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf("") }
    var dueDate by rememberSaveable { mutableStateOf("") }

    if (project != null) {
        title = "Edit project"
        name = project.name
        description = project.description
        startDate = project.startDate
        dueDate = project.dueDate
    }

    AlertDialog(
        title = {
            Text(text = title)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                )
                DatePickerInput(
                    label = "Start date",
                    selectedDate = startDate,
                    onDateSelected = { it -> startDate = it }
                )
                DatePickerInput(
                    label = "Due date",
                    selectedDate = dueDate,
                    onDateSelected = { it -> dueDate = it }
                )
            }
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        if (project == null) {
                            ProjectService.createProject(CreateProjectDTO(
                                name, description, startDate, dueDate
                            ))
                        } else {
                            ProjectService.updateProject(UpdateProjectDTO(
                                id = project.id, name, description, startDate, dueDate
                            ))
                        }
                    }.invokeOnCompletion {
                        onDismiss()
                    }
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}