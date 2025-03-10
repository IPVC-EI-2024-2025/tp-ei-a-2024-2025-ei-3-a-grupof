package dev.jalves.estg.trabalhopratico

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.jalves.estg.trabalhopratico.ui.theme.TrabalhoPraticoTheme
import dev.jalves.estg.trabalhopratico.ui.views.RegisterView
import dev.jalves.estg.trabalhopratico.ui.views.SignIn

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrabalhoPraticoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()

                    NavHost(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        startDestination = "login",
                    ) {
                        composable(route = "login") {
                            SignIn(
                                onRegisterButtonClicked = {
                                    navController.navigate("register")
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
                    }
                }
            }
        }
    }
}