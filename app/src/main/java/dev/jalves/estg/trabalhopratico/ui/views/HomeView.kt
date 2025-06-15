package dev.jalves.estg.trabalhopratico.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.hasAccess
import dev.jalves.estg.trabalhopratico.objects.Project
import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.services.ProjectService
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import dev.jalves.estg.trabalhopratico.ui.components.ProjectListItem
import dev.jalves.estg.trabalhopratico.ui.components.TaskListItem
import io.github.jan.supabase.auth.auth

@Composable
fun HomeView(
    rootNavController: NavHostController,
    navController: NavHostController,
    profileViewModel: ProfileViewModel
) {
    val profile by profileViewModel.profile.collectAsState()

    val user = supabase.auth.currentUserOrNull()!!

    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val tasksViewModel: TasksViewModel = viewModel()

    val tasks by tasksViewModel.tasks.collectAsState()
    val errorMessage by tasksViewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        tasksViewModel.fetchData()
    }

    LaunchedEffect(Unit) {
        val currentUserId = supabase.auth.currentUserOrNull()?.id
        if (currentUserId == null) return@LaunchedEffect
        val result = when {
            user.hasAccess(Role.ADMIN) -> ProjectService.listProjectsByCreator(currentUserId)
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            when {
                profile == null -> LinearProgressIndicator()
                else -> Text(
                    "${stringResource(R.string.hello)} ${profile!!.displayName}",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Rounded.Schedule, contentDescription = "Time")
                Text(java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date()))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.your_projects), style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ), modifier = Modifier.padding(start = 12.dp)
            )

            when {
                loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Text(
                        text = "Error loading projects: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(projects) { project ->
                            ProjectListItem(
                                project = project,
                                onClick = {
                                    rootNavController.navigate("project/${project.id}")
                                }
                            )
                        }
                    }

                    if (projects.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate("projects")
                                }
                            ) {
                                Text("${stringResource(R.string.see_all)} (${projects.size}+)")
                            }
                        }
                    } else {
                        Text(
                            stringResource(R.string.no_projects),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(start = 10.dp)
                        )                    }
                }
            }
        }

        if(user.hasAccess(Role.EMPLOYEE)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    stringResource(R.string.your_tasks), style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ), modifier = Modifier.padding(start = 12.dp)
                )

                when {
                    tasks == null -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    errorMessage != null -> {
                        Text(
                            text = "Error loading tasks: $error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tasks!!) { task ->
                                TaskListItem(
                                    task = task,
                                    onClick = {
                                        rootNavController.navigate("task/${task.id}")
                                    }
                                )
                            }
                        }

                        if (tasks!!.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = {
                                        navController.navigate("tasks")
                                    }
                                ) {
                                    Text("${stringResource(R.string.see_all)} (${tasks!!.size}+)")
                                }
                            }
                        } else {
                            Text(
                                stringResource(R.string.no_tasks),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(start = 10.dp)
                            )                    }
                    }
                }
            }
        }
    }
}