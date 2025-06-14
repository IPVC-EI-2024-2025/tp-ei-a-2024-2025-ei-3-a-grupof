package dev.jalves.estg.trabalhopratico.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowRight
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.jalves.estg.trabalhopratico.objects.User

@Composable
fun UserListItem(
    user: User,
    simple: Boolean = false,
    onClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(enabled = true) {
                if (simple) {
                    onClick?.invoke()
                } else {
                    expanded = !expanded
                }
            }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfilePicture(user = user, size = 36.dp)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            if (!simple) {
                UserRoleBadge(user.role)
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ArrowDropDown else Icons.AutoMirrored.Rounded.ArrowRight,
                    contentDescription = if (expanded) "Collapse details" else "Expand details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!simple) {
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    actions()
                }
            }
        }
    }
}

@Composable
fun UserAction(
    icon: ImageVector,
    name: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            icon,
            contentDescription = name,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Text(
            name,
            style = TextStyle(
                textAlign = TextAlign.Left,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
        )
    }
}