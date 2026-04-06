package ru.techgid.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.techgid.presentation.components.BottomNavBar
import ru.techgid.presentation.screen.auth.AuthScreen
import ru.techgid.presentation.screen.carselect.CarSelectScreen
import ru.techgid.presentation.screen.catalog.CatalogScreen
import ru.techgid.presentation.screen.diagnostic.DiagnosticScreen
import ru.techgid.presentation.screen.guide.GuideDetailScreen
import ru.techgid.presentation.screen.profile.ProfileScreen

@Composable
fun TechGidNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        NavRoute.CarSelect.route,
        NavRoute.Profile.route,
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute ?: "",
                    onNavigate = { route ->
                        when (route) {
                            "home" -> navController.navigate(NavRoute.CarSelect.route) {
                                popUpTo(NavRoute.CarSelect.route) { inclusive = true }
                            }
                            "profile" -> navController.navigate(NavRoute.Profile.route) {
                                popUpTo(NavRoute.CarSelect.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.CarSelect.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            // Car selection
            composable(NavRoute.CarSelect.route) {
                CarSelectScreen(
                    onShowGuides = { configurationId ->
                        navController.navigate(NavRoute.Catalog.create(configurationId))
                    },
                    onCatalog = {
                        navController.navigate(NavRoute.Catalog.create(0))
                    },
                    onDiagnostics = {
                        navController.navigate(NavRoute.Diagnostic.create(0))
                    },
                )
            }

            // Catalog
            composable(
                route = NavRoute.Catalog.route,
                arguments = listOf(navArgument("configurationId") { type = NavType.IntType })
            ) { backStackEntry ->
                val configurationId = backStackEntry.arguments?.getInt("configurationId") ?: return@composable
                CatalogScreen(
                    configurationId = configurationId,
                    onGuideClick = { guideId ->
                        navController.navigate(NavRoute.GuideDetail.create(guideId))
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            // Guide detail
            composable(
                route = NavRoute.GuideDetail.route,
                arguments = listOf(navArgument("guideId") { type = NavType.IntType })
            ) { backStackEntry ->
                val guideId = backStackEntry.arguments?.getInt("guideId") ?: return@composable
                GuideDetailScreen(
                    guideId = guideId,
                    onBack = { navController.popBackStack() },
                )
            }

            // Diagnostic
            composable(
                route = NavRoute.Diagnostic.route,
                arguments = listOf(navArgument("configurationId") { type = NavType.IntType })
            ) { backStackEntry ->
                val configurationId = backStackEntry.arguments?.getInt("configurationId") ?: return@composable
                DiagnosticScreen(
                    configurationId = configurationId,
                    onGuideClick = { guideId ->
                        navController.navigate(NavRoute.GuideDetail.create(guideId))
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            // Auth
            composable(NavRoute.Auth.route) {
                AuthScreen(
                    onAuthSuccess = { navController.popBackStack() },
                )
            }

            // Profile
            composable(NavRoute.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(NavRoute.CarSelect.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onLogin = {
                        navController.navigate(NavRoute.Auth.route)
                    },
                )
            }
        }
    }
}
