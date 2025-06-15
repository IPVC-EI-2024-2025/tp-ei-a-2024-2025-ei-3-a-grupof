package dev.jalves.estg.trabalhopratico.ui.views

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.dto.UserDTO
import dev.jalves.estg.trabalhopratico.hasAccess
import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.objects.Task
import dev.jalves.estg.trabalhopratico.objects.TaskLog
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.ProjectService
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import dev.jalves.estg.trabalhopratico.services.TaskLogService
import dev.jalves.estg.trabalhopratico.services.TaskService
import dev.jalves.estg.trabalhopratico.ui.components.MenuItem
import dev.jalves.estg.trabalhopratico.ui.components.ProfilePicture
import dev.jalves.estg.trabalhopratico.ui.components.TaskLogItem
import dev.jalves.estg.trabalhopratico.ui.components.UserAction
import dev.jalves.estg.trabalhopratico.ui.components.UserListItem
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.ConfirmDialog
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.UserSelectionDialog
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskView(
    navController: NavHostController,
    taskID: String
) {
    var expanded by remember { mutableStateOf(false) }
    val openEditDialog = remember { mutableStateOf(false) }
    val openAssigneeSelectionDialog = remember { mutableStateOf(false) }
    val confirmCompleteDialog = remember { mutableStateOf(false) }
    val confirmArchiveDialog = remember { mutableStateOf(false) }
    var isExportingPdf by remember { mutableStateOf(false) }

    val taskViewModel: TaskViewModel = viewModel()

    val task by taskViewModel.task.collectAsState()
    val employees by taskViewModel.assignedEmployees.collectAsState()
    val error by taskViewModel.error.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun handlePdfExport() {
        isExportingPdf = true
        exportTaskPdf(taskID, context, scope) { success, message ->
            isExportingPdf = false
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        taskViewModel.loadTask(taskID)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.task)) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { MenuItem(Icons.Rounded.Edit, stringResource(R.string.edit)) },
                            onClick = {
                                expanded = false
                                openEditDialog.value = true
                            }
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Rounded.TableChart, stringResource(R.string.export_stats)) },
                            onClick = {
                                expanded = false
                                handlePdfExport()
                            },
                            enabled = !isExportingPdf
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Rounded.Check, stringResource(R.string.mark_complete)) },
                            onClick = {
                                expanded = false
                                confirmCompleteDialog.value = true
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when {
                error != null -> {
                    Text("Error loading task")
                }
                task == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            task!!.name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            task!!.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    AssignedTo()
                    Tabs(navController, task!!, employees) {
                        taskViewModel.loadTask(taskID)
                    }
                }
            }

            if (isExportingPdf) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text("Exporting PDF...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        when {
            openEditDialog.value -> {
            }
            openAssigneeSelectionDialog.value -> {
                UserSelectionDialog(
                    onDismiss = { openAssigneeSelectionDialog.value = false },
                    onClick = {
                        scope.launch {
                            taskViewModel.loadTask(taskID)
                            openAssigneeSelectionDialog.value = false
                        }
                    },
                    userRole = Role.EMPLOYEE,
                )
            }
            confirmCompleteDialog.value -> {
                ConfirmDialog(
                    message = "Mark task as complete?",
                    onConfirm = {
                        scope.launch {
                            TaskService.markTaskComplete(taskID)
                            confirmCompleteDialog.value = false
                        }
                    },
                    onDismiss = {
                        confirmCompleteDialog.value = false
                    }
                )
            }
            confirmArchiveDialog.value -> {
                ConfirmDialog(
                    message = "Archive task?",
                    onConfirm = {
                        scope.launch {
                            confirmArchiveDialog.value = false
                        }
                    },
                    onDismiss = {
                        confirmArchiveDialog.value = false
                    }
                )
            }
        }
    }
}

private fun exportTaskPdf(
    taskID: String,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onResult: (Boolean, String) -> Unit
) {
    scope.launch {
        TaskService.exportTaskStatsToPDF(
            context = context,
            taskId = taskID,
            onSuccess = { file ->
                onResult(true, "PDF exported successfully to Downloads folder: ${file.name}")
            },
            onError = { errorMessage ->
                onResult(false, "Export failed: $errorMessage")
                Log.e("TaskView", "PDF export failed: $errorMessage")
            }
        )
    }
}

@Composable
fun AssignedTo() {
    val taskViewModel: TaskViewModel = viewModel()
    val projectName by taskViewModel.projectName.collectAsState()
    val assignedEmployees by taskViewModel.assignedEmployees.collectAsState()

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy((-8).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                assignedEmployees.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Default.Assignment, contentDescription = null)
                    }
                }

                assignedEmployees.size == 1 -> {
                    ProfilePicture(
                        user = assignedEmployees[0],
                        size = 36.dp
                    )
                }

                assignedEmployees.size == 2 -> {
                    ProfilePicture(
                        user = assignedEmployees[0],
                        size = 36.dp
                    )
                    ProfilePicture(
                        user = assignedEmployees[1],
                        size = 36.dp
                    )
                }

                else -> {
                    ProfilePicture(
                        user = assignedEmployees[0],
                        size = 36.dp
                    )
                    ProfilePicture(
                        user = assignedEmployees[1],
                        size = 36.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${assignedEmployees.size - 2}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        Column {
            Text("Part of", style = MaterialTheme.typography.labelSmall)
            Text(
                projectName ?: "Loading project...",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

enum class TaskDestination(
    val route: String,
    val labelId: Int
) {
    LOGS("logs", R.string.logs),
    EMPLOYEES("employees", R.string.employees)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tabs(navController: NavHostController, task: Task, employees: List<User>, onRefresh: () -> Unit) {
    val startDestination = TaskDestination.LOGS
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Column {
        PrimaryTabRow(selectedTabIndex = selectedDestination) {
            TaskDestination.entries.forEachIndexed { index, destination ->
                Tab(
                    selected = selectedDestination == index,
                    onClick = {
                        selectedDestination = index
                    },
                    text = {
                        Text(
                            text = stringResource(destination.labelId),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }

        when (selectedDestination) {
            TaskDestination.LOGS.ordinal -> {
                LogsTab(navController, task.id)
            }
            TaskDestination.EMPLOYEES.ordinal -> {
                TaskEmployeesTab(employees, task.id, task.projectId, onRefresh)
            }
        }
    }
}

@Composable
fun LogsTab(navController: NavHostController, taskID: String) {
    var logs by remember { mutableStateOf<List<TaskLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val user = supabase.auth.currentUserOrNull()!!

    LaunchedEffect(taskID) {
        scope.launch {
            try {
                isLoading = true
                error = null

                val result = TaskLogService.getTaskLogsByTaskId(taskID)
                result.fold(
                    onSuccess = { fetchedLogs ->
                        logs = fetchedLogs
                    },
                    onFailure = { exception ->
                        error = exception.message
                        Log.e("LogsTab", "Failed to fetch task logs", exception)
                    }
                )
                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
                Log.e("LogsTab", "Exception while fetching logs", e)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(bottom = 80.dp)
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
                    Text(
                        text = "Error loading logs: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                logs.isEmpty() -> {
                    Text(
                        text = "No logs yet for this task",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        items(logs) { log ->
                            TaskLogItem(
                                log = log,
                                onClick = { logId ->
                                    navController.navigate("taskLog/$logId")
                                }
                            )
                        }
                    }
                }
            }
        }

        if (user.hasAccess(Role.EMPLOYEE)) {
            FloatingActionButton(
                onClick = {
                    navController.navigate("newTaskLog/$taskID")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Log")
            }
        }
    }
}

@Composable
fun TaskEmployeesTab(
    employees: List<User> = emptyList(),
    taskID: String,
    projectID: String,
    onRefresh: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val showAddEmployeeDialog = remember { mutableStateOf(false) }
    val showRemoveConfirmDialog = remember { mutableStateOf(false) }
    val selectedEmployee = remember { mutableStateOf<User?>(null) }
    var projectEmployees by remember { mutableStateOf<List<UserDTO>>(emptyList()) }
    var isLoadingProjectEmployees by remember { mutableStateOf(false) }

    val user = supabase.auth.currentUserOrNull()!!

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val filteredEmployees = remember(employees, searchQuery) {
        if (searchQuery.isBlank()) {
            employees
        } else {
            employees.filter { employee ->
                employee.displayName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(projectID) {
        isLoadingProjectEmployees = true
        scope.launch {
            try {
                val result = ProjectService.getProjectByID(projectID)
                result.fold(
                    onSuccess = { projectDto ->
                        projectEmployees = projectDto.employees
                    },
                    onFailure = { exception ->
                        Log.e("TaskEmployeesTab", "Failed to load project employees", exception)
                    }
                )
            } catch (e: Exception) {
                Log.e("TaskEmployeesTab", "Error loading project employees", e)
            } finally {
                isLoadingProjectEmployees = false
            }
        }
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 6.dp)
                .padding(bottom = 80.dp)
        ) {
            when {
                employees.isEmpty() -> {
                    Text(
                        text = "No employees assigned to this task",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                filteredEmployees.isEmpty() && searchQuery.isNotBlank() -> {
                    Text(
                        text = "No employees found matching \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        items(filteredEmployees) { employee ->
                            UserListItem(
                                user = employee,
                                simple = user.hasAccess(Role.EMPLOYEE)
                            ) {
                                if (user.hasAccess(Role.MANAGER, Role.ADMIN)) {
                                    UserAction(
                                        icon = Icons.Rounded.RemoveCircle,
                                        name = stringResource(R.string.remove_from_task),
                                        onClick = {
                                            selectedEmployee.value = employee
                                            showRemoveConfirmDialog.value = true
                                        }
                                    )

                                    UserAction(
                                        icon = Icons.Rounded.Download,
                                        name = stringResource(R.string.export_stats),
                                        onClick = {
                                            showToast("Individual employee export not implemented yet")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (user.hasAccess(Role.MANAGER, Role.ADMIN)) {
            FloatingActionButton(
                onClick = {
                    showAddEmployeeDialog.value = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Employee"
                )
            }
        }

        if (showAddEmployeeDialog.value) {
            UserSelectionDialog(
                onDismiss = {
                    showAddEmployeeDialog.value = false
                },
                onClick = { selectedUser ->
                    scope.launch {
                        try {
                            val result = TaskService.assignTaskToEmployee(selectedUser.id, taskID)
                            result.fold(
                                onSuccess = {
                                    onRefresh()
                                    showToast("Employee assigned to task successfully!")
                                },
                                onFailure = { exception ->
                                    Log.e("TaskEmployeesTab", "Failed to assign employee to task", exception)
                                    showToast("Failed to assign employee to task")
                                }
                            )
                        } catch (e: Exception) {
                            Log.e("TaskEmployeesTab", "Error assigning employee to task", e)
                            showToast("Error assigning employee to task")
                        }
                    }
                    showAddEmployeeDialog.value = false
                },
                userRole = Role.EMPLOYEE,
                filterUsers = { allUsers ->
                    val assignedEmployeeIds = employees.map { it.id }.toSet()
                    val projectEmployeeIds = projectEmployees.map { it.id }.toSet()

                    allUsers.filter { user ->
                        projectEmployeeIds.contains(user.id) && !assignedEmployeeIds.contains(user.id)
                    }
                }
            )
        }

        if (showRemoveConfirmDialog.value && selectedEmployee.value != null) {
            ConfirmDialog(
                onDismiss = {
                    showRemoveConfirmDialog.value = false
                    selectedEmployee.value = null
                },
                onConfirm = {
                    selectedEmployee.value?.let { employee ->
                        scope.launch {
                            try {
                                val result = TaskService.removeEmployeeFromTask(taskID, employee.id)
                                result.fold(
                                    onSuccess = {
                                        onRefresh()
                                        showToast("Employee removed from task successfully!")
                                    },
                                    onFailure = { exception ->
                                        Log.e("TaskEmployeesTab", "Failed to remove employee from task", exception)
                                        showToast("Failed to remove employee from task")
                                    }
                                )
                            } catch (e: Exception) {
                                Log.e("TaskEmployeesTab", "Error removing employee from task", e)
                                showToast("Error removing employee from task")
                            }
                        }
                    }
                    showRemoveConfirmDialog.value = false
                    selectedEmployee.value = null
                },
                message = "Are you sure you want to remove ${selectedEmployee.value?.displayName} from this task?"
            )
        }
    }
}