package ru.techgid.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ru.techgid.presentation.theme.TechGidTheme

@Composable
fun BottomNavBar(
    currentRoute: String = "",
    onNavigate: (String) -> Unit = {},
) {
    val items = listOf(
        BottomNavItem(Icons.Filled.Home, "Главная", "home", "car_select"),
        BottomNavItem(Icons.Filled.Notifications, "Уведомления", "notifications", ""),
        BottomNavItem(Icons.Filled.Person, "Профиль", "profile", "profile"),
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.matchRoute ||
                    (item.route == "home" && currentRoute == "car_select")

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = TechGidTheme.extendedColors.textTertiary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ),
            )
        }
    }
}

private data class BottomNavItem(
    val icon: ImageVector,
    val label: String,
    val route: String,
    val matchRoute: String,
)
