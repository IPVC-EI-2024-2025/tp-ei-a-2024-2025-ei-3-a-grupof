package dev.jalves.estg.trabalhopratico.ui.components.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.jalves.estg.trabalhopratico.ui.components.Dropdown
import dev.jalves.estg.trabalhopratico.ui.components.SettingsItem
import dev.jalves.estg.trabalhopratico.ui.theme.LocalThemeManager

@Composable
fun ThemeTile() {
    val themeManager = LocalThemeManager.current
    val currentTheme by themeManager.themeMode.collectAsState()
    val materialYouEnabled by themeManager.materialYouEnabled.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }

    SettingsItem(
        icon = Icons.Rounded.DarkMode,
        title = "Theme",
        onClick = { dropdownExpanded = true }
    ) {
        Dropdown(
            text = currentTheme,
            expanded = dropdownExpanded,
            onDismiss = { dropdownExpanded = false },
            options = listOf("System", "Light", "Dark"),
            onOptionSelected = { option ->
                themeManager.setThemeMode(option)
                dropdownExpanded = false
            }
        )
    }

    SettingsItem(
        icon = Icons.Rounded.Palette,
        title = "Material You",
        onClick = {
            themeManager.setMaterialYouEnabled(!materialYouEnabled)
        }
    ) {
        Switch(
            checked = materialYouEnabled,
            onCheckedChange = { newValue ->
                themeManager.setMaterialYouEnabled(newValue)
            }
        )
    }
}