package dev.jalves.estg.trabalhopratico.ui.views.dialogs

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.UserService
import dev.jalves.estg.trabalhopratico.ui.components.SimpleUserListItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UserSelectionDialog(
    onDismiss: () -> Unit,
    onClick: (user: User) -> Unit
) {
    var employeeFilter by remember { mutableStateOf("") }
    var employees by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var fetchJob by remember { mutableStateOf<Job?>(null) }

    fun onSearchInputChanged(query: String) {
        employeeFilter = query

        fetchJob?.cancel()
        if (query.isNotBlank()) {
            fetchJob = coroutineScope.launch {
                delay(500)
                isLoading = true
                employees = UserService.fetchUsers(query)
                isLoading = false
            }
        } else {
            employees = emptyList()
        }
    }

    Dialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {}
                    ) { Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Close dialog") }
                    TextField(
                        value = employeeFilter,
                        onValueChange = { onSearchInputChanged(it) },
                        label = { Text("Search employee") }
                    )
                }

                Box(
                    modifier = Modifier.height(300.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        items(employees.size) { i ->
                            SimpleUserListItem(
                                name = "${employees[i].displayName} (${employees[i].username})",
                                onClick = { Log.d("USER", employees[i].toString());onClick(employees[i]) }
                            )
                        }
                    }
                }
            }
        }
    }
}