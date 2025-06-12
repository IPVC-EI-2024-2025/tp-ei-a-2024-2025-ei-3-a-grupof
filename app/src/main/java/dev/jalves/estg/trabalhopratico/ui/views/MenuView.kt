package dev.jalves.estg.trabalhopratico.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import dev.jalves.estg.trabalhopratico.R

@Composable
fun MenuView(rootNavController: NavController) {
    val scope = rememberCoroutineScope()

    Column {
        ListItem(
            headlineContent = { Text(stringResource(R.string.profile)) },
            leadingContent = {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = "Profile",
                )
            },
            modifier = Modifier.clickable {
                rootNavController.navigate("profile")
            },
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.notifications)) },
            leadingContent = {
                Icon(
                    Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                )
            },
            modifier = Modifier.clickable {

            },
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings)) },
            leadingContent = {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = "Settings",
                )
            },
            modifier = Modifier.clickable {
                rootNavController.navigate("settings")
            },
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.log_out)) },
            leadingContent = {
                Icon(
                    Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = "Log out",
                )
            },
            modifier = Modifier.clickable {
                scope.launch {
                    supabase.auth.signOut(SignOutScope.LOCAL)
                    rootNavController.navigate("login") {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            },
        )
    }
}