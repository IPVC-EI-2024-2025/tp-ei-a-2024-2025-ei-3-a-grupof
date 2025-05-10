package dev.jalves.estg.trabalhopratico.ui.views.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.jalves.estg.trabalhopratico.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMain() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Image(
                    painter = painterResource(id = R.drawable.logo_h),
                    contentDescription = "TaskSync logo",
                    modifier = Modifier.size(128.dp)
                ) }
            )
        },
        bottomBar = {
            AdminNavBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = "home",
        ) {
            composable(route = "home") {
                Text(text = "Home")
            }

            composable(route = "projects") {
                Text(text = "Projects")
            }

            composable(route = "users") {
                Text(text = "Users")
            }

            composable(route = "menu") {
                Text(text = "Menu")
            }
        }
    }
}