package ru.techgid.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.techgid.presentation.screen.auth.AuthScreen
import ru.techgid.presentation.screen.carselect.CarSelectScreen
import ru.techgid.presentation.screen.catalog.CatalogScreen
import ru.techgid.presentation.screen.diagnostic.DiagnosticScreen
import ru.techgid.presentation.screen.guide.GuideDetailScreen
import ru.techgid.presentation.screen.profile.ProfileScreen

@Composable
fun TechGidNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoute.CarSelect.route,
    ) {
        // ── Выбор автомобиля ───────────────────────────────────
        composable(NavRoute.CarSelect.route) {
            CarSelectScreen(
                onShowGuides = { configurationId ->
                    navController.navigate(NavRoute.Catalog.create(configurationId))
                }
            )
        }

        // ── Каталог инструкций ─────────────────────────────────
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

        // ── Детальная инструкция ───────────────────────────────
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

        // ── Диагностика ───────────────────────────────────────
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

        // ── Авторизация ────────────────────────────────────────
        composable(NavRoute.Auth.route) {
            AuthScreen(
                onAuthSuccess = { navController.popBackStack() },
            )
        }

        // ── Профиль ────────────────────────────────────────────
        composable(NavRoute.Profile.route) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(NavRoute.CarSelect.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
