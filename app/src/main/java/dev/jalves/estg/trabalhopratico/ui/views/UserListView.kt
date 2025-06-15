package dev.jalves.estg.trabalhopratico.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.UserService
import dev.jalves.estg.trabalhopratico.ui.components.SearchBar
import dev.jalves.estg.trabalhopratico.ui.components.UserListItem
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.ConfirmDialog
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.EditUserDialog
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.UserFilterDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.dto.UpdateUserDTO
import dev.jalves.estg.trabalhopratico.ui.components.UserAction
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.UserFilter

@Composable
fun UserListView() {
    val openEditUserDialog = remember { mutableStateOf(false) }
    val openAddUserDialog = remember { mutableStateOf(false) }
    val openDeleteUserDialog = remember { mutableStateOf(false) }
    val openFilterDialog = remember { mutableStateOf(false) }

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    val selectedUser = remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var userFilter by remember { mutableStateOf(UserFilter()) }

    val coroutineScope = rememberCoroutineScope()
    var fetchJob by remember { mutableStateOf<Job?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    fun fetchUsers(query: String = searchQuery, filter: UserFilter = userFilter) {
        fetchJob?.cancel()
        fetchJob = coroutineScope.launch {
            if (query.isNotEmpty()) {
                delay(500)
            }

            isLoading = true
            try {
                val userList = withContext(Dispatchers.IO) {
                    UserService.fetchUsersByQuery(query, filter.role, filter.status)
                }

                users = userList
            } catch (e: Exception) {
                error.value = e.localizedMessage ?: "Failed to load users"
            } finally {
                isLoading = false
            }
        }
    }

    fun exportUserStats(user: User) {
        coroutineScope.launch {
            UserService.exportUserStatsToPDF(
                context = context,
                userId = user.id,
                onSuccess = { file ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Stats exported successfully to ${file.name}",
                            duration = androidx.compose.material3.SnackbarDuration.Long
                        )
                    }
                },
                onError = { errorMessage ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Export failed: $errorMessage",
                            duration = androidx.compose.material3.SnackbarDuration.Long
                        )
                    }
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        fetchUsers("", UserFilter())
    }

    LaunchedEffect(userFilter) {
        fetchUsers()
    }

    fun onSearchInputChanged(query: String) {
        searchQuery = query
        fetchUsers(query)
    }

    fun onFilterChanged(newFilter: UserFilter) {
        userFilter = newFilter
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SearchBar(
                onSearch = { query -> onSearchInputChanged(query) },
                onFilter = { openFilterDialog.value = true }
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(users) { user ->
                    UserListItem(user = user) {
                        UserAction(
                            icon = Icons.Rounded.Edit,
                            name = stringResource(R.string.edit),
                            onClick = {
                                selectedUser.value = user
                                openEditUserDialog.value = true
                            }
                        )

                        UserAction(
                            icon = if (user.status) Icons.Rounded.Cancel else Icons.Rounded.CheckCircle,
                            name = stringResource(if (user.status) R.string.disable else R.string.enable),
                            onClick = {
                                selectedUser.value = user
                                openDeleteUserDialog.value = true
                            }
                        )

                        UserAction(
                            icon = Icons.Rounded.Download,
                            name = stringResource(R.string.export_stats),
                            onClick = {
                                exportUserStats(user)
                            }
                        )
                    }
                }
            }
        }

        when {
            openEditUserDialog.value -> {
                EditUserDialog(
                    onDismiss = {
                        openEditUserDialog.value = false
                        fetchUsers()
                    },
                    onSubmit = {
                        openEditUserDialog.value = false
                        fetchUsers()
                    },
                    user = selectedUser.value
                )
            }

            openAddUserDialog.value -> {
                EditUserDialog(
                    onDismiss = {
                        openAddUserDialog.value = false
                        fetchUsers()
                    },
                    onSubmit = {
                        openAddUserDialog.value = false
                        fetchUsers()
                    },
                    user = null
                )
            }

            openDeleteUserDialog.value -> {
                ConfirmDialog(
                    onDismiss = { openDeleteUserDialog.value = false },
                    onConfirm = {
                        selectedUser.value?.id?.let { userId ->
                            openDeleteUserDialog.value = false
                            coroutineScope.launch(Dispatchers.IO) {
                                UserService.updateUser(UpdateUserDTO(
                                    id = userId,
                                    status = !(selectedUser.value!!.status)
                                ))
                                withContext(Dispatchers.Main) {
                                    fetchUsers()
                                }
                            }
                        }
                    },
                    message = if (selectedUser.value?.status == true) {
                        stringResource(R.string.confirm_disable_user)
                    } else {
                        stringResource(R.string.confirm_enable_user)
                    }
                )
            }

            openFilterDialog.value -> {
                UserFilterDialog(
                    currentFilter = userFilter,
                    onDismiss = { openFilterDialog.value = false },
                    onApplyFilter = { newFilter ->
                        onFilterChanged(newFilter)
                        openFilterDialog.value = false
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = { openAddUserDialog.value = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add user")
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}