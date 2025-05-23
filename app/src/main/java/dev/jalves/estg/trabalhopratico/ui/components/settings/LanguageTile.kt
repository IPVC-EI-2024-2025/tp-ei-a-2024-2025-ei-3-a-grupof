package dev.jalves.estg.trabalhopratico.ui.components.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.jalves.estg.trabalhopratico.ui.components.Dropdown
import dev.jalves.estg.trabalhopratico.ui.components.SettingsItem

@Composable
fun LanguageTile(
    initialLanguage: String = "English",
    availableLanguages: List<String> = listOf("English", "Portuguese"),
    onLanguageChanged: ((String) -> Unit)? = null
) {
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    SettingsItem(
        icon = Icons.Rounded.Language,
        title = "Language",
        onClick = { dropdownExpanded = true }
    ) {
        Dropdown(
            text = selectedLanguage,
            expanded = dropdownExpanded,
            onDismiss = { dropdownExpanded = false },
            options = availableLanguages,
            onOptionSelected = { option ->
                selectedLanguage = option
                onLanguageChanged?.invoke(option)
                dropdownExpanded = false
            }
        )
    }
}