package dev.jalves.estg.trabalhopratico.ui.views

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import dev.jalves.estg.trabalhopratico.dto.ProjectDTO
import dev.jalves.estg.trabalhopratico.dto.UpdateProjectDTO
import dev.jalves.estg.trabalhopratico.dto.UserDTO
import dev.jalves.estg.trabalhopratico.services.ProjectService
import dev.jalves.estg.trabalhopratico.ui.components.SearchBar
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.ConfirmDialog
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.EditProjectDialog
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.UserSelectionDialog
import kotlinx.coroutines.launch
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.formatDate
import dev.jalves.estg.trabalhopratico.hasAccess
import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.objects.Task
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.AuthService
import dev.jalves.estg.trabalhopratico.services.ProjectService.addEmployeeToProject
import dev.jalves.estg.trabalhopratico.services.ProjectService.removeEmployeeFromProject
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import dev.jalves.estg.trabalhopratico.services.TaskService
import dev.jalves.estg.trabalhopratico.ui.components.MenuItem
import dev.jalves.estg.trabalhopratico.ui.components.ProfilePicture
import dev.jalves.estg.trabalhopratico.ui.components.TaskListItem
import dev.jalves.estg.trabalhopratico.ui.components.UserAction
import dev.jalves.estg.trabalhopratico.ui.components.UserListItem
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.CreateTaskDialog
import io.github.jan.supabase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectView(
    navController: NavHostController,
    projectID: String
) {
    var expanded by remember { mutableStateOf(false) }
    val openEditDialog = remember { mutableStateOf(false) }
    val openManagerSelectionDialog = remember { mutableStateOf(false) }
    val confirmCompleteDialog = remember { mutableStateOf(false) }
    val confirmArchiveDialog = remember { mutableStateOf(false) }
    var isExportingPDF by remember { mutableStateOf(false) }

    val projectViewModel: ProjectViewModel = viewModel()

    val user = supabase.auth.currentUserOrNull()!!

    val project by projectViewModel.project.collectAsState()
    val error by projectViewModel.error.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        projectViewModel.loadProject(projectID)
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun exportProjectToPDF() {
        if (isExportingPDF) return

        isExportingPDF = true
        showToast("Exporting PDF...")

        scope.launch {
            ProjectService.exportProjectStatsToPDF(
                context = context,
                projectId = projectID,
                onSuccess = { file ->
                    isExportingPDF = false
                    showToast("PDF exported successfully to Downloads folder: ${file.name}")
                },
                onError = { errorMessage ->
                    isExportingPDF = false
                    showToast("Export failed: $errorMessage")
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(stringResource(R.string.project))},
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
                    if (
                        user.hasAccess(Role.ADMIN)
                        || (project != null && project!!.manager != null && project!!.manager!!.id == user.id)
                    ) {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "Menu")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    MenuItem(
                                        Icons.Rounded.Person,
                                        stringResource(R.string.edit_manager)
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    openManagerSelectionDialog.value = true
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    MenuItem(
                                        Icons.Rounded.Edit,
                                        stringResource(R.string.edit)
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    openEditDialog.value = true
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    MenuItem(
                                        Icons.Rounded.TableChart,
                                        stringResource(R.string.export_stats)
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    exportProjectToPDF()
                                },
                                enabled = !isExportingPDF
                            )

                            DropdownMenuItem(
                                text = {
                                    MenuItem(
                                        Icons.Rounded.Check,
                                        stringResource(R.string.mark_complete)
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    confirmCompleteDialog.value = true
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    MenuItem(
                                        Icons.Rounded.Folder,
                                        stringResource(R.string.archive_project)
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    confirmArchiveDialog.value = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when {
                // todo: styling
                error != null -> {
                    Text("error")
                }
                project == null -> {
                    CircularProgressIndicator()
                }
                else -> {
                    Text(project!!.name, style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        project!!.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            "${stringResource(R.string.created)}: ${formatDate(project!!.startDate)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "${stringResource(R.string.due)}: ${formatDate(project!!.dueDate)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    ManagedBy(project!!)
                    Tabs(navController, project!!) {
                        projectViewModel.loadProject(projectID)
                    }
                }
            }
        }

        when {
            openEditDialog.value -> {
                EditProjectDialog(
                    onDismiss = {
                        openEditDialog.value = false
                        projectViewModel.loadProject(projectID)
                    },
                    project = project
                )
            }
            openManagerSelectionDialog.value -> {
                UserSelectionDialog(
                    onDismiss = { openManagerSelectionDialog.value = false },
                    onClick = { user ->
                        scope.launch {
                            ProjectService.updateProject(UpdateProjectDTO(
                                id = project!!.id,
                                managerID = user.id
                            ))
                            projectViewModel.loadProject(projectID)
                            openManagerSelectionDialog.value = false
                        }
                    },
                    userRole = Role.MANAGER,
                )
            }
            confirmCompleteDialog.value -> {
                ConfirmDialog(
                    message = "Mark project as complete?",
                    onConfirm = {
                        scope.launch {
                            ProjectService.updateProject(UpdateProjectDTO(
                                id = projectID,
                                status = "complete"
                            ))
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
                    message = "Archive project?",
                    onConfirm = {
                        scope.launch {
                            ProjectService.updateProject(UpdateProjectDTO(
                                id = projectID,
                                status = "archived"
                            ))
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

@Composable
fun ManagedBy(
    project: ProjectDTO
) {
    val manager = User(
        id = project.manager?.id ?: "",
        displayName = project.manager?.displayName ?: stringResource(R.string.no_manager),
        username = project.manager?.username ?: "",
        role = project.manager?.role ?: Role.EMPLOYEE
    )

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProfilePicture(
            manager
        )
        Column {
            Text(stringResource(R.string.managed_by), style = MaterialTheme.typography.labelSmall)
            when {
                project.manager == null -> {
                    Text(stringResource(R.string.no_manager), style = MaterialTheme.typography.labelLarge)
                }

                else -> {
                    Text(project.manager.displayName, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

enum class Destination(
    val route: String,
    val labelId: Int
) {
    TASKS("tasks", R.string.tasks),
    EMPLOYEES("employees", R.string.employees)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tabs(
    rootNavController: NavHostController,
    project: ProjectDTO,
    onProjectRefresh: () -> Unit
) {
    val startDestination = Destination.TASKS
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Column {
        PrimaryTabRow(selectedTabIndex = selectedDestination) {
            Destination.entries.forEachIndexed { index, destination ->
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
            Destination.TASKS.ordinal -> {
                TasksTab(rootNavController, project)
            }
            Destination.EMPLOYEES.ordinal -> {
                EmployeesTab(
                    employees = project.employees,
                    projectID = project.id,
                    onRefresh = onProjectRefresh
                )
            }
        }
    }
}

@Composable
fun TasksTab(
    rootNavController: NavHostController,
    project: ProjectDTO
) {
    val showCreateDialog = remember { mutableStateOf(false) }

    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val user = supabase.auth.currentUserOrNull()!!

    val scope = rememberCoroutineScope()

    LaunchedEffect(project.id) {
        scope.launch {
            try {
                isLoading = true
                error = null
                val result = TaskService.listProjectTasks(project.id)
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
    }

    val filteredTasks = remember(tasks, searchQuery) {
        if (searchQuery.isBlank()) {
            tasks
        } else {
            tasks.filter { task ->
                task.name.contains(searchQuery, ignoreCase = true) ||
                        task.description.contains(searchQuery, ignoreCase = true)
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
            SearchBar(
                onSearch = { query -> searchQuery = query },
                onFilter = {}
            )

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
                        text = stringResource(R.string.error_loading_tasks) + ": $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                filteredTasks.isEmpty() && searchQuery.isNotBlank() -> {
                    Text(
                        text = stringResource(R.string.no_tasks_matching_search, searchQuery),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                filteredTasks.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_tasks_yet),
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
                        items(filteredTasks) { task ->
                            TaskListItem(
                                task = task,
                                onClick = {
                                    rootNavController.navigate("task/${task.id}")
                                },
                            )
                        }
                    }
                }
            }
        }


        if(
            user.hasAccess(Role.ADMIN)
            || (project.manager != null && project.manager.id == user.id)
        ){
            FloatingActionButton(
                onClick = { showCreateDialog.value = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Create Task")
            }
        }

        if (showCreateDialog.value) {
            CreateTaskDialog(
                projectId = project.id,
                onDismiss = {
                    showCreateDialog.value = false
                    scope.launch {
                        try {
                            val result = TaskService.listProjectTasks(project.id)
                            result.fold(
                                onSuccess = { taskList -> tasks = taskList },
                                onFailure = { exception ->
                                    Log.e("TasksTab", "Failed to refresh tasks after dialog dismiss", exception)
                                    error = exception.message
                                }
                            )
                        } catch (e: Exception) {
                            Log.e("TasksTab", "Error refreshing tasks after dialog dismiss", e)
                            error = e.message
                        }
                    }
                },
                onSubmit = {
                    showCreateDialog.value = false
                    scope.launch {
                        try {
                            val result = TaskService.listProjectTasks(project.id)
                            result.fold(
                                onSuccess = { taskList -> tasks = taskList },
                                onFailure = { exception ->
                                    Log.e("TasksTab", "Failed to refresh tasks after task creation", exception)
                                    error = exception.message
                                }
                            )
                        } catch (e: Exception) {
                            Log.e("TasksTab", "Error refreshing tasks after task creation", e)
                            error = e.message
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun EmployeesTab(employees: List<UserDTO>, projectID: String, onRefresh: () -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    val showAddEmployeeDialog = remember { mutableStateOf(false) }
    val showRemoveConfirmDialog = remember { mutableStateOf(false) }
    val selectedEmployee = remember { mutableStateOf<UserDTO?>(null) }
    val scope = rememberCoroutineScope()

    var userRole by remember { mutableStateOf<Role?>(null) }

    val context = LocalContext.current

    LaunchedEffect(projectID) {
        userRole = AuthService.getCurrentUserRole()
    }

    val filteredEmployees = remember(employees, searchQuery) {
        if (searchQuery.isBlank()) {
            employees
        } else {
            employees.filter { employee ->
                employee.displayName.contains(searchQuery, ignoreCase = true)
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
                .padding(vertical = 8.dp)
                .padding(bottom = 80.dp)
        ) {
            SearchBar(
                onSearch = { query -> searchQuery = query },
                onFilter = {}
            )

            when {
                employees.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_employees_assigned),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                filteredEmployees.isEmpty() && searchQuery.isNotBlank() -> {
                    Text(
                        text = stringResource(R.string.no_employees_matching_search, searchQuery),
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
                            val user = User(
                                id = employee.id,
                                displayName = employee.displayName,
                                username = employee.username,
                                role = employee.role,
                            )

                            UserListItem(
                                user = user,
                                simple = userRole == Role.EMPLOYEE
                            ) {
                                if(userRole != Role.EMPLOYEE){
                                    UserAction(
                                        icon = Icons.Rounded.RemoveCircle,
                                        name = stringResource(R.string.remove_from_project),
                                        onClick = {
                                            selectedEmployee.value = employee
                                            showRemoveConfirmDialog.value = true
                                        }
                                    )

                                    UserAction(
                                        icon = Icons.Rounded.Download,
                                        name = stringResource(R.string.export_stats),
                                        onClick = {
                                            // TODO: Implement individual employee export stats functionality
                                            showToast("Individual employee export coming soon!")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if(userRole != Role.EMPLOYEE) {
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
                            val result = addEmployeeToProject(selectedUser.id, projectID)
                            result.fold(
                                onSuccess = {
                                    onRefresh()
                                    showToast("Employee added successfully!")
                                },
                                onFailure = { exception ->
                                    Log.e("EmployeesTab", "Failed to add employee to project", exception)
                                    showToast("Failed to add employee")
                                }
                            )
                        } catch (e: Exception) {
                            Log.e("EmployeesTab", "Error adding employee to project", e)
                            showToast("Error adding employee")
                        }
                    }
                    showAddEmployeeDialog.value = false
                },
                userRole = Role.EMPLOYEE,
                filterUsers = { allUsers ->
                    val assignedEmployeeIds = employees.map { it.id }.toSet()

                    allUsers.filter { user ->
                        !assignedEmployeeIds.contains(user.id)
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
                                val result = removeEmployeeFromProject(employee.id, projectID)
                                result.fold(
                                    onSuccess = {
                                        onRefresh()
                                        showToast("Employee removed successfully!")
                                    },
                                    onFailure = { exception ->
                                        Log.e("EmployeesTab", "Failed to remove employee", exception)
                                        showToast("Failed to remove employee")
                                    }
                                )
                            } catch (e: Exception) {
                                Log.e("EmployeesTab", "Error removing employee from project", e)
                                showToast("Error removing employee")
                            }
                        }
                    }
                    showRemoveConfirmDialog.value = false
                    selectedEmployee.value = null
                },
                message = "Are you sure you want to remove ${selectedEmployee.value?.displayName} from this project?"
            )
        }
    }
}