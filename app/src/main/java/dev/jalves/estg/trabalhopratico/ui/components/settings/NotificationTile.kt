package dev.jalves.estg.trabalhopratico.ui.components.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.jalves.estg.trabalhopratico.ui.components.SettingsItem

@Composable
fun NotificationsTile(
    initialEnabled: Boolean = true,
    onNotificationsChanged: ((Boolean) -> Unit)? = null
) {
    var notificationsEnabled by remember { mutableStateOf(initialEnabled) }

    SettingsItem(
        icon = Icons.Rounded.Notifications,
        title = "Notifications",
        onClick = {
            val newValue = !notificationsEnabled
            notificationsEnabled = newValue
            onNotificationsChanged?.invoke(newValue)
        }
    ) {
        Switch(
            checked = notificationsEnabled,
            onCheckedChange = { newValue ->
                notificationsEnabled = newValue
                onNotificationsChanged?.invoke(newValue)
            }
        )
    }
}