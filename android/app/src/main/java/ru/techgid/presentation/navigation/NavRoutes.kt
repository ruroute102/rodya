package ru.techgid.presentation.navigation

/**
 * Маршруты навигации приложения.
 */
sealed class NavRoute(val route: String) {
    data object Home : NavRoute("home")
    data object CarSelect : NavRoute("car_select")
    data object Catalog : NavRoute("catalog/{configurationId}") {
        fun create(configurationId: Int) = "catalog/$configurationId"
    }
    data object Search : NavRoute("search")
    data object TechSpecs : NavRoute("techspecs")
    data object ServiceHistory : NavRoute("service_history")
    data object Settings : NavRoute("settings")
    data object GuideDetail : NavRoute("guide/{guideId}") {
        fun create(guideId: Int) = "guide/$guideId"
    }
    data object Viewer3D : NavRoute("viewer3d")
    data object Diagnostic : NavRoute("diagnostic/{configurationId}") {
        fun create(configurationId: Int) = "diagnostic/$configurationId"
    }
    data object Profile : NavRoute("profile")
    data object Auth : NavRoute("auth")
    data object Favorites : NavRoute("favorites")
    data object EditProfile : NavRoute("edit_profile")
    data object Reminders : NavRoute("reminders")
    data object Consumables : NavRoute("consumables")
}
