package dev.jalves.estg.trabalhopratico.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jalves.estg.trabalhopratico.ui.components.SearchBar
import dev.jalves.estg.trabalhopratico.ui.components.UserListItem
import dev.jalves.estg.trabalhopratico.ui.views.dialogs.EditUserDialog

@Composable
fun UserListView() {
    var openEditUserDialog = remember { mutableStateOf(false) }

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
                onEditUser = { openEditUserDialog.value = true }
            )
        }
    }

    when {
        openEditUserDialog.value -> {
            EditUserDialog(
                onDismiss = { openEditUserDialog.value = false }
            )
        }
    }
}