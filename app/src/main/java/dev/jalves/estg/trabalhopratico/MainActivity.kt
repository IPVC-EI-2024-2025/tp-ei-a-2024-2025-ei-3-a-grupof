package dev.jalves.estg.trabalhopratico

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.jalves.estg.trabalhopratico.ui.theme.AppTheme
import dev.jalves.estg.trabalhopratico.ui.views.HomeView
import dev.jalves.estg.trabalhopratico.ui.views.IntroView
import dev.jalves.estg.trabalhopratico.ui.views.ProfileView
import dev.jalves.estg.trabalhopratico.ui.views.RegisterView
import dev.jalves.estg.trabalhopratico.ui.views.SignIn
import dev.jalves.estg.trabalhopratico.ui.views.admin.AdminMain

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val navController = rememberNavController()
                val bottomBarState = rememberSaveable { (mutableStateOf(false)) }

                NavHost(
                    navController = navController,
                    startDestination = "adminMain"
                ) {
                    composable(route = "intro") {
                        IntroView(
                            onContinue = {
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
                                navController.navigate("home") {
                                    popUpTo(0)
                                }
                                bottomBarState.value = true
                            }
                        )
                    }

                    composable(route = "register") {
                        RegisterView(
                            onReturn = {
                                navController.navigateUp()
                            }
                        )
                    }

                    composable(route = "home") {
                        HomeView()
                    }

                    composable(route = "adminMain") {
                        AdminMain(navController)
                    }

                    composable(route = "profile") {
                        ProfileView(navController)
                    }
                }
            }
        }
    }
}