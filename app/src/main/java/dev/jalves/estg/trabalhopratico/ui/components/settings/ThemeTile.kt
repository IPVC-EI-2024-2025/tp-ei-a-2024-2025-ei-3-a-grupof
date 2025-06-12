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
import androidx.compose.ui.res.stringResource
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.ui.components.Dropdown
import dev.jalves.estg.trabalhopratico.ui.components.SettingsItem
import dev.jalves.estg.trabalhopratico.ui.theme.LocalThemeManager

@Composable
fun ThemeTile() {
    val themeManager = LocalThemeManager.current
    val currentTheme by themeManager.themeMode.collectAsState()
    val materialYouEnabled by themeManager.materialYouEnabled.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }

    val themeOptions = listOf("System", "Light", "Dark")
    val displayOptions = listOf(
        stringResource(R.string.system),
        stringResource(R.string.light),
        stringResource(R.string.dark)
    )

    val currentThemeDisplay = when (currentTheme) {
        "Light" -> stringResource(R.string.light)
        "Dark" -> stringResource(R.string.dark)
        else -> stringResource(R.string.system)
    }

    SettingsItem(
        icon = Icons.Rounded.DarkMode,
        title = stringResource(R.string.theme),
        onClick = { dropdownExpanded = true }
    ) {
        Dropdown(
            text = currentThemeDisplay,
            expanded = dropdownExpanded,
            onDismiss = { dropdownExpanded = false },
            options = displayOptions,
            onOptionSelected = { selectedLabel ->
                val selectedIndex = displayOptions.indexOf(selectedLabel)
                val selectedInternal = themeOptions.getOrNull(selectedIndex) ?: "System"
                themeManager.setThemeMode(selectedInternal)
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