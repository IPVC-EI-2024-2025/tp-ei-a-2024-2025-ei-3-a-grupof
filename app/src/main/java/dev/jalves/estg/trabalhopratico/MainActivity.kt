package dev.jalves.estg.trabalhopratico

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import dev.jalves.estg.trabalhopratico.ui.theme.ThemedApp
import dev.jalves.estg.trabalhopratico.ui.views.IntroView
import dev.jalves.estg.trabalhopratico.ui.views.NewTaskLogView
import dev.jalves.estg.trabalhopratico.ui.views.MainView
import dev.jalves.estg.trabalhopratico.ui.views.ProfileView
import dev.jalves.estg.trabalhopratico.ui.views.ProfileViewModel
import dev.jalves.estg.trabalhopratico.ui.views.ProjectView
import dev.jalves.estg.trabalhopratico.ui.views.ProjectsViewModel
import dev.jalves.estg.trabalhopratico.ui.views.RegisterView
import dev.jalves.estg.trabalhopratico.ui.views.SettingsView
import dev.jalves.estg.trabalhopratico.ui.views.SignIn
import dev.jalves.estg.trabalhopratico.ui.views.TaskLogView
import dev.jalves.estg.trabalhopratico.ui.views.TaskView
import io.github.jan.supabase.auth.auth
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE)
        val langCode = prefs.getString("language", "en") ?: "en"
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val localizedContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(localizedContext)
    }

    private fun isFirstTimeUser(): Boolean {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return sharedPref.getBoolean("is_first_time", true)
    }

    private fun setFirstTimeComplete() {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("is_first_time", false)
            apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemedApp {
                val navController = rememberNavController()
                val bottomBarState = rememberSaveable { (mutableStateOf(false)) }

                val profileViewModel: ProfileViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = "init"
                ) {
                    composable(route = "init") {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LaunchedEffect(Unit) {
                                supabase.auth.awaitInitialization()

                                if (isFirstTimeUser()) {
                                    navController.navigate("intro") {
                                        popUpTo(0)
                                    }
                                } else {
                                    navController.navigate(
                                        if (supabase.auth.currentUserOrNull() == null)
                                            "login" else "main"
                                    ) {
                                        popUpTo(0)
                                    }
                                }
                            }

                            CircularProgressIndicator()
                        }
                    }

                    composable(route = "intro") {
                        IntroView(
                            onContinue = {
                                setFirstTimeComplete()
                                navController.navigate("login") {
                                    popUpTo(0)
                                }
                            }
                        )
                    }

                    composable(route = "login") {
                        SignIn(
                            onRegisterButtonClicked = {
                                navController.navigate("register")
                            },
                            onSuccessfulSignIn = {
                                navController.navigate("main") {
                                    popUpTo(0)
                                }
                                bottomBarState.value = true
                                profileViewModel.fetchData()
                            }
                        )
                    }

                    composable(route = "register") {
                        RegisterView(
                            onReturn = {
                                navController.navigateUp()
                            },
                            onSuccessfulRegister = {
                                navController.navigate("main")
                            }
                        )
                    }

                    composable(route = "main") {
                        MainView(navController, profileViewModel)
                    }

                    composable(route = "profile") {
                        ProfileView(navController, profileViewModel)
                    }

                    composable(route = "settings") {
                        SettingsView(navController)
                    }

                    composable(route = "project/{id}") { navBackStackEntry ->
                        val projectID = navBackStackEntry.arguments?.getString("id")

                        ProjectView(navController, projectID ?: "")
                    }

                    composable(route = "task/{id}") { navBackStackEntry ->
                        val taskID = navBackStackEntry.arguments?.getString("id")

                        TaskView(navController, taskID ?: "")
                    }

                    composable(route = "taskLog/{id}") { navBackStackEntry ->
                        val logId = navBackStackEntry.arguments?.getString("id")

                        TaskLogView (
                            logId = logId ?: "",
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(route = "newTaskLog/{taskId}") { navBackStackEntry ->
                        val taskId = navBackStackEntry.arguments?.getString("taskId")

                        NewTaskLogView(
                            taskId = taskId ?: "",
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}