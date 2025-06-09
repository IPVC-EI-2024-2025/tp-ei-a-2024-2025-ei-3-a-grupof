package dev.jalves.estg.trabalhopratico.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jalves.estg.trabalhopratico.services.SupabaseAdminService
import dev.jalves.estg.trabalhopratico.services.UserCrud.disableUser
import dev.jalves.estg.trabalhopratico.services.UserCrud.getUsers
import dev.jalves.estg.trabalhopratico.ui.components.SearchBar
import dev.jalves.estg.trabalhopratico.ui.components.UserListItem
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.ConfirmDialog
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.EditUserDialog
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UserListView() {
    val openEditUserDialog = remember { mutableStateOf(false) }
    val openAddUserDialog = remember { mutableStateOf(false) }
    val openDeleteUserDialog = remember { mutableStateOf(false) }
    val users = remember { mutableStateOf<List<UserInfo>>(emptyList()) }
    val selectedUser = remember { mutableStateOf<UserInfo?>(null) }
    val isLoading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val adminClient = SupabaseAdminService.supabase

    LaunchedEffect(Unit) {
        isLoading.value = true
        try {
            val userList = withContext(Dispatchers.IO) {
                getUsers().getOrElse { emptyList() }
            }
            users.value = userList
        } catch (e: Exception) {
            error.value = e.localizedMessage ?: "Failed to load users"
        } finally {
            isLoading.value = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SearchBar(
                onSearch = { query ->  },
                onFilter = { }
            )

            LazyColumn {
                items(users.value) { user ->
                    user.email?.let {
                        UserListItem(
                            name = it,
                            onEditUser = {
                                selectedUser.value = user
                                openEditUserDialog.value = true
                            },
                            onDeleteUser = {
                                selectedUser.value = user
                                openDeleteUserDialog.value = true
                            }
                        )
                    }
                }
            }
        }

        when {
            openEditUserDialog.value -> {
                EditUserDialog(
                    onDismiss = { openEditUserDialog.value = false },
                    onSubmit = { openEditUserDialog.value = false },
                    user = selectedUser.value
                )

            }

            openAddUserDialog.value -> {
                EditUserDialog(
                    onDismiss = { openAddUserDialog.value = false },
                    onSubmit = { openAddUserDialog.value = false },
                    user = null
                )
            }

            openDeleteUserDialog.value -> {
                ConfirmDialog(
                    onDismiss = { openDeleteUserDialog.value = false },
                    onConfirm = {
                        selectedUser.value?.id?.let { userId ->
                            openDeleteUserDialog.value = false
                            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                SupabaseAdminService.initAdminSession()
                                disableUser(userId)
                            }
                        }
                    },
                    message = "Delete user?"

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
    }
}
