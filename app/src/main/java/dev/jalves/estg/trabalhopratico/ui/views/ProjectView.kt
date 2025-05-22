package dev.jalves.estg.trabalhopratico.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.jalves.estg.trabalhopratico.ui.components.SearchBar
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.EditProjectDialog

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
    projectID: Int
) {
    var expanded by remember { mutableStateOf(false) }

    val openEditDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Profile")},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { MenuItem(Icons.Default.Edit, "Edit manager") },
                            onClick = {
                                expanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Default.Edit, "Edit") },
                            onClick = {
                                expanded = false
                                openEditDialog.value = true
                            }
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Default.Delete, "Delete") },
                            onClick = {
                                expanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Default.TableChart, "Export stats") },
                            onClick = {
                                expanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Default.Check, "Mark complete") },
                            onClick = {
                                expanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = { MenuItem(Icons.Default.Folder, "Archive project") },
                            onClick = {
                                expanded = false
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
            Text("Project $projectID", style = MaterialTheme.typography.titleLarge)
            Text(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi euismod  bibendum enim, sit amet porttitor odio accumsan et. Vestibulum ante  ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae;",
                style = MaterialTheme.typography.bodyMedium
            )
            ManagedBy()
            Tabs()
        }

        when {
            openEditDialog.value -> {
                EditProjectDialog(
                    onDismiss = { openEditDialog.value = false }
                )
            }
        }
    }
}

@Composable
fun ManagedBy() {
    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Person, contentDescription = "", Modifier.size(48.dp))
        Column {
            Text("Managed by", style = MaterialTheme.typography.labelSmall)
            Text("Project manager 1", style = MaterialTheme.typography.labelLarge)
            Text("Last edit: 27/04/2025 14:00", style = MaterialTheme.typography.labelSmall)
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
fun Tabs() {
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
                        Destination.EMPLOYEES -> EmployeesTab()
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
fun EmployeesTab() {
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