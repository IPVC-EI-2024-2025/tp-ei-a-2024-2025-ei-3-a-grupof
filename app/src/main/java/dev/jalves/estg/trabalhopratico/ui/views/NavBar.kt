package dev.jalves.estg.trabalhopratico.ui.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import dev.jalves.estg.trabalhopratico.R
import dev.jalves.estg.trabalhopratico.ui.components.NavigationItem

@Composable
fun NavBar(navController: NavController) {
    val navigationItems = listOf(
        NavigationItem(
            title = stringResource(R.string.nav_home),
            icon = Icons.Rounded.Home,
            route = "home"
        ),
        NavigationItem(
            title = stringResource(R.string.projects),
            icon = Icons.Rounded.Work,
            route = "projects"
        ),
        NavigationItem(
            title = stringResource(R.string.users),
            icon = Icons.Rounded.People,
            route = "users"
        ),
        NavigationItem(
            title = stringResource(R.string.menu),
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
                        item.title
                    )
                }
            )
        }
    }
}