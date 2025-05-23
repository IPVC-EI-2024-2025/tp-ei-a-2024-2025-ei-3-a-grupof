package dev.jalves.estg.trabalhopratico.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jalves.estg.trabalhopratico.ui.components.SearchBar
import dev.jalves.estg.trabalhopratico.ui.components.UserListItem
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.ConfirmDialog
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.EditUserDialog

@Composable
fun UserListView() {
    var openEditUserDialog = remember { mutableStateOf(false) }
    var openAddUserDialog = remember { mutableStateOf(false) }
    var openDeleteUserDialog = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SearchBar(
                onSearch = {query -> },
                onFilter = {}
            )

            for (i in 1..8) {
                UserListItem(
                    onEditUser = { openEditUserDialog.value = true },
                    onDeleteUser = { openDeleteUserDialog.value = true }
                )
            }
        }

        when {
            openEditUserDialog.value -> {
                EditUserDialog(
                    onDismiss = { openEditUserDialog.value = false },
                    onSubmit = { openEditUserDialog.value = false },
                    user = true
                )
            }

            openAddUserDialog.value -> {
                EditUserDialog(
                    onDismiss = { openAddUserDialog.value = false },
                    onSubmit = { openAddUserDialog.value = false },
                    user = false
                )
            }

            openDeleteUserDialog.value -> {
                ConfirmDialog (
                    onDismiss = { openDeleteUserDialog.value = false },
                    onConfirm = { openDeleteUserDialog.value = false },
                    message = "Delete user?"
                )
            }
        }

        FloatingActionButton(
            onClick = {
                openAddUserDialog.value = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add user")
        }
    }
}