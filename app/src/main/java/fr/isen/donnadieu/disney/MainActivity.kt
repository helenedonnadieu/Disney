package fr.isen.donnadieu.disney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.isen.donnadieu.disney.auth.AuthViewModel
import fr.isen.donnadieu.disney.auth.LoginScreen
import fr.isen.donnadieu.disney.auth.RegisterScreen
import fr.isen.donnadieu.disney.notifications.MarketplaceNotifier
import fr.isen.donnadieu.disney.notifications.NotificationHelper
import fr.isen.donnadieu.disney.ui.films.FilmListScreen
import fr.isen.donnadieu.disney.ui.profile.ProfileScreen
import fr.isen.donnadieu.disney.ui.theme.DisneyTheme
import fr.isen.donnadieu.disney.ui.universe.UniverseScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // ← AJOUTER CES DEUX LIGNES
        NotificationHelper.createChannel(this)
        MarketplaceNotifier.checkPendingNotifications(this)
        setContent {
            DisneyTheme {
                val viewModel: AuthViewModel = viewModel()
                val navController = rememberNavController()

                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                val showBottomBar = currentRoute == "home"
                        || currentRoute == "profile"
                        || currentRoute?.startsWith("films/") == true

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "profile",
                                    onClick = {
                                        if (viewModel.isLoggedIn()) {
                                            navController.navigate("profile")
                                        } else {
                                            navController.navigate("login_profile")
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                    label = { Text("Profile") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onGoToRegister = { navController.navigate("register") },
                                onBack = { navController.popBackStack() } // ← AJOUTÉ
                            )
                        }
//                        val context = LocalContext.current
//                        Text(context.getString(R.string.app_name), color = colorResource(R.color.purple_200))
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
                            FilmListScreen(
                                franchiseName = franchiseName,
                                onBack = { navController.popBackStack() },
                                onRequireLogin = {
                                    navController.navigate("login_profile")
                                }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                onLogout = {
                                    navController.navigate("home") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("login_profile") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    navController.navigate("profile") {
                                        popUpTo("login_profile") { inclusive = true }
                                    }
                                },
                                onGoToRegister = { navController.navigate("register") },
                                onBack = { navController.popBackStack() } // ← AJOUTÉ
                            )
                        }
                    }
                }
            }
        }
    }
}