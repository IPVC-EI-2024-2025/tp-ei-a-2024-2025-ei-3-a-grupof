package dev.jalves.estg.trabalhopratico.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jalves.estg.trabalhopratico.objects.Task
import dev.jalves.estg.trabalhopratico.objects.TaskStatus
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.TaskService
import kotlinx.coroutines.launch

@Composable
fun TaskListItem(
    onClick: () -> Unit,
    task: Task,
) {
    var assignedEmployees by remember { mutableStateOf<List<User>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(task.id) {
        scope.launch {
            TaskService.getTaskEmployees(task.id).fold(
                onSuccess = { employees ->
                    assignedEmployees = employees
                },
                onFailure = {
                    assignedEmployees = emptyList()
                }
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = task.name,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                    LocalTextStyle provides LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                ) {
                    TaskStatusBadge(status = task.status)
                    AssignedEmployeesPreview(employees = assignedEmployees)
                }
            }
        }

        Icon(
            Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = "Open task",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun AssignedEmployeesPreview(employees: List<User>) {
    if (employees.isNotEmpty()) {
        Text("•")
        Row(
            horizontalArrangement = Arrangement.spacedBy((-8).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (employees.size) {
                1 -> {
                    ProfilePicture(
                        user = employees[0],
                        size = 20.dp
                    )
                }
                2 -> {
                    ProfilePicture(
                        user = employees[0],
                        size = 20.dp
                    )
                    ProfilePicture(
                        user = employees[1],
                        size = 20.dp
                    )
                }
                else -> {
                    ProfilePicture(
                        user = employees[0],
                        size = 20.dp
                    )
                    ProfilePicture(
                        user = employees[1],
                        size = 20.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${employees.size - 2}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskStatusBadge(
    status: TaskStatus,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = status.color,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.value,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.hsl(122f, 0.46f, 0.33f)
            )
        )
    }
}