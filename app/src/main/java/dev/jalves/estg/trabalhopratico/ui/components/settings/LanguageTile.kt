package dev.jalves.estg.trabalhopratico.ui.components.settings

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.ui.components.Dropdown
import dev.jalves.estg.trabalhopratico.ui.components.SettingsItem
import java.util.Locale

fun Context.setLocale(locale: Locale): Context {
    Locale.setDefault(locale)

    val config = Configuration(resources.configuration)
    config.setLocale(locale)

    return createConfigurationContext(config)
}

fun changeAppLanguage(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    context.setLocale(locale)

    if (context is Activity) {
        context.recreate()
    }
}


@Composable
fun LanguageTile(
    availableLanguages: List<String> = listOf("English", "Português")
) {
    val context = LocalContext.current

    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val savedLanguage = prefs.getString("language", "en") ?: "en"
    val initialLanguage = when (savedLanguage) {
        "pt" -> "Português"
        else -> "English"
    }

    var selectedLanguage by remember { mutableStateOf(initialLanguage) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    SettingsItem(
        icon = Icons.Rounded.Language,
        title = stringResource(R.string.language),
        onClick = { dropdownExpanded = true }
    ) {
        Dropdown(
            text = selectedLanguage,
            expanded = dropdownExpanded,
            onDismiss = { dropdownExpanded = false },
            options = availableLanguages,
            onOptionSelected = { option ->
                selectedLanguage = option
                val langCode = when (option) {
                    "Português" -> "pt"
                    else -> "en"
                }

                prefs.edit().putString("language", langCode).apply()
                changeAppLanguage(context, langCode)
                dropdownExpanded = false
            }
        )
    }
}