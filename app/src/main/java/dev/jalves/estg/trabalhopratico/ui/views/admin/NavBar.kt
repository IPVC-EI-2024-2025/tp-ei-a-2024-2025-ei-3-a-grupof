package dev.jalves.estg.trabalhopratico.ui.views.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import dev.jalves.estg.trabalhopratico.ui.components.NavigationItem

@Composable
fun AdminNavBar(navController: NavController) {
    val navigationItems = listOf(
        NavigationItem(
            title = "Home",
            icon = Icons.Rounded.Home,
            route = "home"
        ),
        NavigationItem(
            title = "Projects",
            icon = Icons.Rounded.Work,
            route = "projects"
        ),
        NavigationItem(
            title = "Users",
            icon = Icons.Rounded.People,
            route = "users"
        ),
        NavigationItem(
            title = "Menu",
            icon = Icons.Rounded.Menu,
            route = "menu"
        ),
    )

    val selectedNavigationIndex = rememberSaveable {
        mutableIntStateOf(0)
    }

    NavigationBar {
        navigationItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedNavigationIndex.intValue == index,
                onClick = {
                    selectedNavigationIndex.intValue = index
                    navController.navigate(item.route)
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.title)
                },
                label = {
                    Text(
                        item.title,
                        color = if (index == selectedNavigationIndex.intValue)
                            Color.Black
                        else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.surface,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}