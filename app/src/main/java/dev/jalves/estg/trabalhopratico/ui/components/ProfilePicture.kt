package dev.jalves.estg.trabalhopratico.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.UserService

suspend fun fetchProfilePictureUrl(userId: String, pictureSize: Int = 100): String? {
    return try {
        UserService.getProfilePictureURL(
            pictureSize = pictureSize,
            userId = userId
        )
    } catch (_: Exception) {
        null
    }
}

@Composable
fun ProfilePicture(
    user: User,
    size: Dp = 64.dp,
    onClick: (() -> Unit)? = null,
    showLoadingIndicator: Boolean = true,
    modifier: Modifier = Modifier,
    externalProfilePicUrl: String? = null,
) {
    var profilePicUrl by remember(user.id) { mutableStateOf<String?>(null) }
    var imageLoadFailed by remember(user.id) { mutableStateOf(false) }
    var isLoading by remember(user.id) { mutableStateOf(true) }

    LaunchedEffect(user.id) {
        if (user.id.isNotEmpty()) {
            isLoading = true
            imageLoadFailed = false

            profilePicUrl = fetchProfilePictureUrl(user.id, size.value.toInt())

            isLoading = false
        }
    }

    LaunchedEffect(externalProfilePicUrl) {
        if (externalProfilePicUrl != null) {
            profilePicUrl = externalProfilePicUrl
            imageLoadFailed = false
            isLoading = false
        }
    }

    val boxModifier = modifier
        .size(size)
        .clip(CircleShape)
        .then(
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }
        )

    Box(
        contentAlignment = Alignment.Center,
        modifier = boxModifier
    ) {
        when {
            isLoading && showLoadingIndicator -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(size * 0.5f)
                )
            }
            !imageLoadFailed && profilePicUrl != null -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profilePicUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile picture for ${user.displayName}",
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                    onError = {
                        imageLoadFailed = true
                    }
                )
            }
            else -> {
                PlaceholderProfilePic(
                    name = user.displayName,
                    size = size
                )
            }
        }
    }
}