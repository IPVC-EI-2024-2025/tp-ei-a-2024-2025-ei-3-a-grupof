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
import androidx.compose.ui.res.stringResource
import dev.jalves.estg.trabalhopratico.R
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

    var title = stringResource(R.string.new_project)

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf("") }
    var dueDate by rememberSaveable { mutableStateOf("") }

    if (project != null) {
        title = stringResource(R.string.edit_project)
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
                    label = { Text(stringResource(R.string.name)) },
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description)) },
                )
                DatePickerInput(
                    label = stringResource(R.string.start_date),
                    selectedDate = startDate,
                    onDateSelected = { it -> startDate = it }
                )
                DatePickerInput(
                    label = stringResource(R.string.due_date),
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
                Text(stringResource(R.string.submit))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}