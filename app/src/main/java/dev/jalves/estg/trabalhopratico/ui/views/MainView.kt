package dev.jalves.estg.trabalhopratico.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.jalves.estg.trabalhopratico.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    rootNavController: NavHostController,
    profileViewModel: ProfileViewModel,
    projectsViewModel: ProjectsViewModel
) {
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
            NavBar(navController, rootNavController)
        }
    ) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = "home"
        ) {
            composable(route = "home") {
                HomeView(rootNavController, profileViewModel)
            }

            composable(route = "tasks") {
                TaskListView()
            }

            composable(route = "projects") {
                ProjectListView(rootNavController, projectsViewModel)
            }

            composable(route = "users") {
                UserListView()
            }

            composable(route = "menu") {
                MenuView(rootNavController)
            }
        }
    }
}