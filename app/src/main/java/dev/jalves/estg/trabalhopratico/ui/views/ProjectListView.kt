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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.jalves.estg.trabalhopratico.hasAccess
import dev.jalves.estg.trabalhopratico.objects.Project
import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.services.AuthService
import dev.jalves.estg.trabalhopratico.services.ProjectService
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import dev.jalves.estg.trabalhopratico.ui.components.ProjectListItem
import dev.jalves.estg.trabalhopratico.ui.components.SearchBar
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.EditProjectDialog
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun ProjectListView(
    rootNavController: NavHostController,
) {
    val openAddProjectDialog = remember { mutableStateOf(false) }

    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var userRole by remember { mutableStateOf<Role?>(null) }
    val user = supabase.auth.currentUserOrNull()!!

    val scope = rememberCoroutineScope()

    suspend fun fetchData() {
        val currentUserId = supabase.auth.currentUserOrNull()?.id
        if (currentUserId == null) return
        val result = when {
            user.hasAccess(Role.ADMIN) -> ProjectService.listProjects()
            user.hasAccess(Role.MANAGER) -> ProjectService.listProjectsByManager(currentUserId)
            else -> ProjectService.listProjectsByEmployee(currentUserId)
        }
        result.onSuccess {
            projects = it
            loading = false
        }.onFailure { exception ->
            error = exception.message ?: "Unknown error"
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        fetchData()
    }

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
                error != null -> {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }
                loading -> {
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
        }

        if(user.hasAccess(Role.ADMIN, Role.MANAGER))
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
                    scope.launch { fetchData() }
                }
            )
        }
    }
}