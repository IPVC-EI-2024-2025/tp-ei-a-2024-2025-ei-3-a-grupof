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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

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

@Composable
fun Tabs() {

}