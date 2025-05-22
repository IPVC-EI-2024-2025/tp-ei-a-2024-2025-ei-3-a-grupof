package dev.jalves.estg.trabalhopratico.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun MenuView(rootNavController: NavController) {
    Column {
        ListItem(
            headlineContent = { Text("Profile") },
            leadingContent = {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "Profile",
                )
            },
            modifier = Modifier.clickable {
                rootNavController.navigate("profile")
            },
        )

        ListItem(
            headlineContent = { Text("Notifications") },
            leadingContent = {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                )
            },
            modifier = Modifier.clickable {

            },
        )

        ListItem(
            headlineContent = { Text("Settings") },
            leadingContent = {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Settings",
                )
            },
            modifier = Modifier.clickable {
                rootNavController.navigate("settings")
            },
        )

        ListItem(
            headlineContent = { Text("Log out") },
            leadingContent = {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Log out",
                )
            },
            modifier = Modifier.clickable {

            },
        )
    }
}