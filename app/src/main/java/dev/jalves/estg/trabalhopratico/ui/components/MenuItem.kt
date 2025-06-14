package dev.jalves.estg.trabalhopratico.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun MenuItem(
    icon: ImageVector,
    text: String
) {
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
        LocalTextStyle provides LocalTextStyle.current.copy(
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = text)
            Text(text)
        }
    }
}