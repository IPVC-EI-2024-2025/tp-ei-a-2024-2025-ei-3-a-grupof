package dev.jalves.estg.trabalhopratico.ui.components.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.jalves.estg.trabalhopratico.ui.components.Dropdown
import dev.jalves.estg.trabalhopratico.ui.components.SettingsItem

@Composable
fun ThemeTile(
    initialTheme: String = "System",
    onThemeChanged: ((String) -> Unit)? = null
) {
    var selectedTheme by remember { mutableStateOf(initialTheme) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    SettingsItem(
        icon = Icons.Default.DarkMode,
        title = "Theme",
        onClick = { dropdownExpanded = true }
    ) {
        Dropdown(
            text = selectedTheme,
            expanded = dropdownExpanded,
            onDismiss = { dropdownExpanded = false },
            options = listOf("System", "Light", "Dark"),
            onOptionSelected = { option ->
                selectedTheme = option
                onThemeChanged?.invoke(option)
                dropdownExpanded = false
            }
        )
    }
}