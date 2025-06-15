package dev.jalves.estg.trabalhopratico.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PhotoCarousel(
    photos: List<T>,
    onPhotoClick: ((T) -> Unit)? = null,
    modifier: Modifier = Modifier,
    showDeleteButton: Boolean = false,
    onDeletePhoto: ((T) -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    handlePhotoViewInternally: Boolean = true
) {
    val carouselState = rememberCarouselState { photos.size }
    var selectedPhoto by remember { mutableStateOf<T?>(null) }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        modifier = modifier.then(Modifier.width(412.dp).height(221.dp)),
        preferredItemWidth = 186.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { page ->
        val photo = photos[page]

        Box(
            modifier = Modifier
                .fillMaxSize()
                .maskClip(shape)
        ) {
            AsyncImage(
                model = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .maskClip(shape)
                    .clickable {
                        if (handlePhotoViewInternally) {
                            selectedPhoto = photo
                        } else if (onPhotoClick != null) {
                            onPhotoClick(photo)
                        }
                    }
            )

            if (showDeleteButton && onDeletePhoto != null) {
                IconButton(
                    onClick = { onDeletePhoto(photo) },
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.TopEnd)
                        .padding(end = 12.dp, top = 8.dp)
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Remove",
                        tint = Color.White,
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(4.dp)
                            .size(24.dp)
                    )
                }
            }
        }
    }

    selectedPhoto?.let { photo ->
        PhotoViewerDialog(
            photoModel = photo,
            onDismiss = { selectedPhoto = null }
        )
    }
}

@Composable
fun <T> PhotoViewerDialog(
    photoModel: T,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)

        val maxOffset = if (scale > 1f) {
            (scale - 1f) * 500f
        } else {
            0f
        }

        offset = Offset(
            x = (offset.x + offsetChange.x).coerceIn(-maxOffset, maxOffset),
            y = (offset.y + offsetChange.y).coerceIn(-maxOffset, maxOffset)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            AsyncImage(
                model = photoModel,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = transformableState)
                    .clickable(enabled = false) { },
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            if (scale != 1f || offset != Offset.Zero) {
                FloatingActionButton(
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text("1:1", fontSize = 12.sp)
                }
            }
        }
    }
}
