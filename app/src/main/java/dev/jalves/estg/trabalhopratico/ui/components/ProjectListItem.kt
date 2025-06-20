package dev.jalves.estg.trabalhopratico.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableIntStateOf
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
import dev.jalves.estg.trabalhopratico.objects.Project
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.UserService
import dev.jalves.estg.trabalhopratico.services.TaskService
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.formatDate

@Composable
fun ProjectListItem(
    onClick: () -> Unit,
    project: Project
) {
    var manager by remember { mutableStateOf<User?>(null) }
    var taskCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(project.id) {
        manager = project.managerID?.let { UserService.fetchUserById(it) }

        TaskService.getProjectTaskCount(project.id).onSuccess { count ->
            taskCount = count
        }.onFailure {
            taskCount = 0 // Default to 0 if we can't fetch the count
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            )
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(vertical = 10.dp).padding(start = 16.dp)
        ) {
            Text(project.name, style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            ))
            // Magic component that applies styling to every child
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                LocalTextStyle provides LocalTextStyle.current.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ProfilePicture(
                        user = manager,
                        size = 24.dp
                    )
                    Text("•")
                    Text("$taskCount ${stringResource(if (taskCount == 1) R.string.task else R.string.tasks)}")
                    Text("•")
                    Text("${stringResource(R.string.due)} ${formatDate(project.dueDate)}")
                }
            }
        }
        Column(
            modifier = Modifier.padding(end = 16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Open project",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}