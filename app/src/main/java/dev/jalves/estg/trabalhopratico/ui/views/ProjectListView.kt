package dev.jalves.estg.trabalhopratico.ui.views

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.jalves.estg.trabalhopratico.objects.Project
import dev.jalves.estg.trabalhopratico.services.ProjectService
import dev.jalves.estg.trabalhopratico.ui.components.ProjectListItem
import dev.jalves.estg.trabalhopratico.ui.components.SearchBar
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.EditProjectDialog

@Composable
fun ProjectListView(
    rootNavController: NavHostController
) {
    val openAddProjectDialog = remember { mutableStateOf(false) }

    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val result = ProjectService.listProjects()
        result.onSuccess {
            projects = it
            loading = false
        }.onFailure {
            error = it.message ?: "Unknown error"
            loading = false
        }
    }

    Box (
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SearchBar(
                onSearch = {query -> },
                onFilter = {}
            )

            if (loading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Text("Error loading projects: $error")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projects) { project ->
                        ProjectListItem(
                            onClick = {
                                rootNavController.navigate("project/${project.id}")
                            },
                            project
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                openAddProjectDialog.value = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Project"
            )
        }
    }

    when {
        openAddProjectDialog.value -> {
            EditProjectDialog(
                onDismiss = { openAddProjectDialog.value = false }
            )
        }
    }
}