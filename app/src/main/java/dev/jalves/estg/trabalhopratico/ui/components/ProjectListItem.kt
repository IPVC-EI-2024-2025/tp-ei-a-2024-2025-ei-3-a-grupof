package dev.jalves.estg.trabalhopratico.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.jalves.estg.trabalhopratico.objects.Project
import dev.jalves.estg.trabalhopratico.services.UserService

@Composable
fun ProjectListItem(
    onClick: () -> Unit,
    project: Project
) {
    var profilePicUrl by remember { mutableStateOf<String?>(null) }
    var imageLoadFailed by remember { mutableStateOf(false) }
    var profilePicLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if(project.managerID == null) {
            imageLoadFailed = true
            profilePicLoading = false
        } else {
            try {
                profilePicUrl = UserService.getProfilePictureURL(
                    pictureSize = 32,
                    userId = project.managerID
                )
            } catch (_: Exception) {
                imageLoadFailed = true
                null
            } finally {
                profilePicLoading = false
            }
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
                    when {
                        profilePicLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        !imageLoadFailed && profilePicUrl != null -> {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profilePicUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile picture",
                                modifier = Modifier.size(16.dp).clip(CircleShape),
                                onError = {
                                    imageLoadFailed = true
                                }
                            )
                        }
                        else -> {
                            PlaceholderProfilePic(name = "?", size = 16.dp)
                        }
                    }
                    Text("•")
                    Text("? Tasks")
                    Text("•")
                    Text("Due ${project.dueDate}")
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