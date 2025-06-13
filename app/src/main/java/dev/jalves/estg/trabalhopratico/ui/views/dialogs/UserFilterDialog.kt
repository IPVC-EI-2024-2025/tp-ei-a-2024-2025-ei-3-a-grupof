package dev.jalves.estg.trabalhopratico.ui.views.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.objects.Role

data class UserFilter(
    val role: Role? = null,
    val status: Boolean? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFilterDialog(
    currentFilter: UserFilter,
    onDismiss: () -> Unit,
    onApplyFilter: (UserFilter) -> Unit
) {
    var selectedRole by remember { mutableStateOf(currentFilter.role) }
    var selectedStatus by remember { mutableStateOf(currentFilter.status) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.filter_users),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
                ) {
                    TextField(
                        value = selectedRole?.let { stringResource(it.descriptionId) } ?: stringResource(R.string.all_roles),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(R.string.filter_by_role)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.all_roles)) },
                            onClick = {
                                selectedRole = null
                                roleDropdownExpanded = false
                            }
                        )

                        Role.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(stringResource(role.descriptionId)) },
                                onClick = {
                                    selectedRole = role
                                    roleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Status Filter
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                ) {
                    TextField(
                        value = when (selectedStatus) {
                            true -> stringResource(R.string.active_users)
                            false -> stringResource(R.string.inactive_users)
                            null -> stringResource(R.string.all_statuses)
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(R.string.filter_by_status)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.all_statuses)) },
                            onClick = {
                                selectedStatus = null
                                statusDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.active_users)) },
                            onClick = {
                                selectedStatus = true
                                statusDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.inactive_users)) },
                            onClick = {
                                selectedStatus = false
                                statusDropdownExpanded = false
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedRole = null
                            selectedStatus = null
                            onApplyFilter(UserFilter())
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.clear_filters))
                    }

                    Button(
                        onClick = {
                            onApplyFilter(UserFilter(role = selectedRole, status = selectedStatus))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.apply_filters))
                    }
                }
            }
        }
    }
}