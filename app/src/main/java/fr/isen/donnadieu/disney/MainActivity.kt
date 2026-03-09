package fr.isen.donnadieu.disney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.isen.donnadieu.disney.auth.AuthViewModel
import fr.isen.donnadieu.disney.auth.LoginScreen
import fr.isen.donnadieu.disney.auth.RegisterScreen
import fr.isen.donnadieu.disney.ui.theme.DisneyTheme
import fr.isen.donnadieu.disney.ui.universe.UniverseScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DisneyTheme {
                val viewModel: AuthViewModel = viewModel()
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onGoToRegister = { navController.navigate("register") }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            viewModel = viewModel,
                            onRegisterSuccess = {
                                navController.navigate("home") {
                                    popUpTo("register") { inclusive = true }
                                }
                            },
                            onGoToLogin = { navController.popBackStack() }
                        )
                    }
                    composable("home") {
                        UniverseScreen(
                            onFranchiseClick = { franchiseName ->
                                navController.navigate("films/$franchiseName")
                            }
                        )
                    }

                    composable("films/{franchiseName}") { backStackEntry ->
                        val franchiseName = backStackEntry.arguments?.getString("franchiseName") ?: ""
                        Text("Films de $franchiseName - à faire") // on remplacera ça après
                    }
                }
            }
        }
    }
}