package ru.techgid.presentation.navigation

/**
 * Маршруты навигации приложения.
 */
sealed class NavRoute(val route: String) {
    data object CarSelect : NavRoute("car_select")
    data object Catalog : NavRoute("catalog/{configurationId}") {
        fun create(configurationId: Int) = "catalog/$configurationId"
    }
    data object Search : NavRoute("search/{configurationId}") {
        fun create(configurationId: Int) = "search/$configurationId"
    }
    data object GuideDetail : NavRoute("guide/{guideId}") {
        fun create(guideId: Int) = "guide/$guideId"
    }
    data object Viewer3D : NavRoute("viewer3d/{configurationId}") {
        fun create(configurationId: Int) = "viewer3d/$configurationId"
    }
    data object Diagnostic : NavRoute("diagnostic/{configurationId}") {
        fun create(configurationId: Int) = "diagnostic/$configurationId"
    }
    data object Profile : NavRoute("profile")
    data object Auth : NavRoute("auth")
}
