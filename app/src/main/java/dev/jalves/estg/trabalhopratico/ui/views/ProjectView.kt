package dev.jalves.estg.trabalhopratico.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.jalves.estg.trabalhopratico.dto.ProjectDTO
import dev.jalves.estg.trabalhopratico.dto.UpdateProjectDTO
import dev.jalves.estg.trabalhopratico.dto.UserDTO
import dev.jalves.estg.trabalhopratico.services.ProjectService
import dev.jalves.estg.trabalhopratico.ui.components.SearchBar
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.ConfirmDialog
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.EditProjectDialog
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.UserSelectionDialog
import kotlinx.coroutines.launch

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
    val confirmDisableDialog = remember { mutableStateOf(false) }
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
                title = {Text("Project")},
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
                            text = { MenuItem(Icons.Rounded.Person, "Edit manager") },
                            onClick = {
                                expanded = false
                                openManagerSelectionDialog.value = true
                            }
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Rounded.Edit, "Edit") },
                            onClick = {
                                expanded = false
                                openEditDialog.value = true
                            }
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Rounded.Cancel, "Disable") },
                            onClick = {
                                expanded = false
                                confirmDisableDialog.value = true
                            }
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Rounded.TableChart, "Export stats") },
                            onClick = {
                                expanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Rounded.Check, "Mark complete") },
                            onClick = {
                                expanded = false
                                confirmCompleteDialog.value = true
                            }
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Rounded.Folder, "Archive project") },
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
                    Text(project!!.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        project!!.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Created: " + project!!.startDate,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Due: " + project!!.dueDate,
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
                    }
                )
            }
            confirmDisableDialog.value -> {
                ConfirmDialog(
                    message = "Disable project?",
                    onConfirm = {
                        scope.launch {
                            ProjectService.updateProject(UpdateProjectDTO(
                                id = projectID,
                                status = "disabled"
                            ))
                            confirmDisableDialog.value = false
                        }
                    },
                    onDismiss = {
                        confirmDisableDialog.value = false
                    }
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
    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Rounded.Person, contentDescription = "", Modifier.size(48.dp))
        Column {
            Text("Managed by", style = MaterialTheme.typography.labelSmall)
            when {
                project.manager == null -> {
                    Text("No manager assigned", style = MaterialTheme.typography.labelLarge)
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
    val label: String
) {
    TASKS("tasks", "Tasks"),
    EMPLOYEES("employees", "Employees")
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
                            text = destination.label,
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
                        Destination.TASKS -> TasksTab()
                        Destination.EMPLOYEES -> EmployeesTab(project.employees)
                    }
                }
            }
        }
    }
}

@Composable
fun TasksTab() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        SearchBar(
            onSearch = {query -> },
            onFilter = {}
        )
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