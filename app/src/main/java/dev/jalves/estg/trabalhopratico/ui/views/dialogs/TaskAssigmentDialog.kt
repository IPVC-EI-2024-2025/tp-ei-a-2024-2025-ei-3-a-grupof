package dev.jalves.estg.trabalhopratico.ui.views.dialogs

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.objects.Task
import dev.jalves.estg.trabalhopratico.services.TaskService
import dev.jalves.estg.trabalhopratico.ui.components.TaskListItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TaskAssignmentDialog(
    projectId: String,
    onDismiss: () -> Unit,
    onTaskSelected: (task: Task) -> Unit,
) {
    var taskFilter by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var fetchJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        try {
            val result = TaskService.listProjectTasks(projectId)
            result.fold(
                onSuccess = { taskList ->
                    tasks = taskList
                    isLoading = false
                },
                onFailure = { exception ->
                    error = exception.message
                    isLoading = false
                }
            )
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    fun onSearchInputChanged(query: String) {
        taskFilter = query

        fetchJob?.cancel()
        fetchJob = coroutineScope.launch {
            delay(300)

            if (query.isBlank()) {
                try {
                    isLoading = true
                    val result = TaskService.listProjectTasks(projectId)
                    result.fold(
                        onSuccess = { taskList ->
                            tasks = taskList
                            isLoading = false
                        },
                        onFailure = { exception ->
                            error = exception.message
                            isLoading = false
                        }
                    )
                } catch (e: Exception) {
                    error = e.message
                    isLoading = false
                }
            } else {
                tasks = tasks.filter { task ->
                    task.name.contains(query, ignoreCase = true) ||
                            task.description.contains(query, ignoreCase = true)
                }
            }
        }
    }

    val filteredTasks = remember(tasks, taskFilter) {
        if (taskFilter.isBlank()) {
            tasks
        } else {
            tasks.filter { task ->
                task.name.contains(taskFilter, ignoreCase = true) ||
                        task.description.contains(taskFilter, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onDismiss() }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Close dialog"
                        )
                    }
                    TextField(
                        value = taskFilter,
                        onValueChange = { onSearchInputChanged(it) },
                        label = { Text(stringResource(R.string.search)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Box(
                    modifier = Modifier.height(400.dp)
                ) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        error != null -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Error loading tasks: $error",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        filteredTasks.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (taskFilter.isBlank()) {
                                        "No tasks available in this project"
                                    } else {
                                        "No tasks found matching \"$taskFilter\""
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredTasks.size) { i ->
                                    TaskListItem(
                                        task = filteredTasks[i],
                                        onClick = {
                                            Log.d("TASK_ASSIGNMENT", "Selected task: ${filteredTasks[i]}")
                                            onTaskSelected(filteredTasks[i])
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}