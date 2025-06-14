package dev.jalves.estg.trabalhopratico.ui.views

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.jalves.estg.trabalhopratico.dto.ProjectDTO
import dev.jalves.estg.trabalhopratico.dto.UpdateProjectDTO
import dev.jalves.estg.trabalhopratico.dto.UserDTO
import dev.jalves.estg.trabalhopratico.services.ProjectService
import dev.jalves.estg.trabalhopratico.services.UserService
import dev.jalves.estg.trabalhopratico.ui.components.PlaceholderProfilePic
import dev.jalves.estg.trabalhopratico.ui.components.SearchBar
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.ConfirmDialog
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.EditProjectDialog
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.UserSelectionDialog
import kotlinx.coroutines.launch
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.formatDate
import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.objects.Task
import dev.jalves.estg.trabalhopratico.services.TaskService
import dev.jalves.estg.trabalhopratico.ui.components.TaskListItem
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.CreateTaskDialog
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.EditTaskDialog

@Composable
fun MenuItem(
    icon: ImageVector,
    text: String
) {
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
        LocalTextStyle provides LocalTextStyle.current.copy(
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = text)
            Text(text)
        }
    }
}

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

    val projectViewModel: ProjectViewModel = viewModel()

    val project by projectViewModel.project.collectAsState()
    val error by projectViewModel.error.collectAsState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        projectViewModel.loadProject(projectID)
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
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { MenuItem(Icons.Rounded.Person, stringResource(R.string.edit_manager)) },
                            onClick = {
                                expanded = false
                                openManagerSelectionDialog.value = true
                            }
                        )

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
                            }
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Rounded.Check, stringResource(R.string.mark_complete)) },
                            onClick = {
                                expanded = false
                                confirmCompleteDialog.value = true
                            }
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Rounded.Folder, stringResource(R.string.archive_project)) },
                            onClick = {
                                expanded = false
                                confirmArchiveDialog.value = true
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 8.dp),
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
                    Tabs(project!!)
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
fun ManagedBy(project: ProjectDTO) {
    var profilePicUrl by remember { mutableStateOf<String?>(null) }
    var imageLoadFailed by remember { mutableStateOf(false) }
    var profilePicLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if(project.manager == null) {
            imageLoadFailed = true
            profilePicLoading = false
        } else {
            try {
                profilePicUrl = UserService.getProfilePictureURL(
                    pictureSize = 128,
                    userId = project.manager.id
                )
            } catch (_: Exception) {
                imageLoadFailed = true
                null
            } finally {
                profilePicLoading = false
            }
        }
    }

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            project.manager == null -> {
                PlaceholderProfilePic(name = "?", size = 48.dp)
            }
            profilePicLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
            }
            !imageLoadFailed && profilePicUrl != null -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profilePicUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile picture",
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    onError = {
                        imageLoadFailed = true
                    }
                )
            }
            else -> {
                PlaceholderProfilePic(name = project.manager.displayName, size = 48.dp)
            }
        }
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
fun Tabs(project: ProjectDTO) {
    val navController = rememberNavController()
    val startDestination = Destination.TASKS
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Column {
        PrimaryTabRow(selectedTabIndex = selectedDestination) {
            Destination.entries.forEachIndexed { index, destination ->
                Tab(
                    selected = selectedDestination == index,
                    onClick = {
                        navController.navigate(route = destination.route)
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
        NavHost(
            navController,
            startDestination = startDestination.route
        ) {
            Destination.entries.forEach { destination ->
                composable(destination.route) {
                    when (destination) {
                        Destination.TASKS -> TasksTab(project.id)
                        Destination.EMPLOYEES -> EmployeesTab(project.employees)
                    }
                }
            }
        }
    }
}

@Composable
fun TasksTab(projectID: String) {
    val showCreateDialog = remember { mutableStateOf(false) }

    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(projectID) {
        scope.launch {
            try {
                isLoading = true
                error = null
                val result = TaskService.listProjectTasks(projectID)
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

    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
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
                        text = "Error loading tasks: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                filteredTasks.isEmpty() && searchQuery.isNotBlank() -> {
                    Text(
                        text = "No tasks found matching \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                filteredTasks.isEmpty() -> {
                    Text(
                        text = "No tasks yet. Create your first task!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTasks) { task ->
                            TaskListItem(
                                task = task,
                                onClick = {

                                }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog.value = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Create Task")
        }

        if (showCreateDialog.value) {
            CreateTaskDialog(
                projectId = projectID,
                onDismiss = {
                    showCreateDialog.value = false
                    scope.launch {
                        try {
                            val result = TaskService.listProjectTasks(projectID)
                            result.fold(
                                onSuccess = { taskList -> tasks = taskList },
                                onFailure = {  }
                            )
                        } catch (e: Exception) {
                        }
                    }
                },
                onSubmit = {
                    showCreateDialog.value = false
                    scope.launch {
                        try {
                            val result = TaskService.listProjectTasks(projectID)
                            result.fold(
                                onSuccess = { taskList -> tasks = taskList },
                                onFailure = {  }
                            )
                        } catch (e: Exception) {
                        }
                    }
                }
            )
        }
    }
}
@Composable
fun EmployeesTab(employees: List<UserDTO>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        SearchBar(
            onSearch = {query -> },
            onFilter = {}
        )
        for(employee in employees)
            Text(employee.displayName)
    }
}