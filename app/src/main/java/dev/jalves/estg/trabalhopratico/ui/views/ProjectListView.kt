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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.services.AuthService
import dev.jalves.estg.trabalhopratico.ui.components.ProjectListItem
import dev.jalves.estg.trabalhopratico.ui.components.SearchBar
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.EditProjectDialog

@Composable
fun ProjectListView(
    rootNavController: NavHostController,
    projectsViewModel: ProjectsViewModel
) {
    val openAddProjectDialog = remember { mutableStateOf(false) }
    val projects by projectsViewModel.projects.collectAsState()
    val errorMessage by projectsViewModel.errorMessage.collectAsState()

    var userRole by remember { mutableStateOf<Role?>(null) }

    LaunchedEffect(projects) {
        userRole = AuthService.getCurrentUserRole()
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

            when {
                errorMessage != null -> {
                    Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                }
                projects == null -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    LazyColumn (
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(projects!!) { project ->
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
        }

        if(userRole != Role.EMPLOYEE)
            FloatingActionButton(
                onClick = {
                    openAddProjectDialog.value = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Create Project"
                )
            }
    }

    when {
        openAddProjectDialog.value -> {
            EditProjectDialog(
                onDismiss = {
                    openAddProjectDialog.value = false
                    projectsViewModel.fetchData()
                }
            )
        }
    }
}