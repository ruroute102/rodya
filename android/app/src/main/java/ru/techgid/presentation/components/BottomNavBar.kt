package ru.techgid.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ru.techgid.presentation.theme.TechGidTheme

/**
 * Нижняя навигация по референсу: 3 иконки.
 * Минималистичная, без подписей — только иконки.
 */
@Composable
fun BottomNavBar(
    onMenuClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    val items = listOf(
        BottomNavItem(Icons.Filled.Menu, "Меню"),
        BottomNavItem(Icons.Filled.Notifications, "Уведомления"),
        BottomNavItem(Icons.Outlined.ChatBubbleOutline, "Чат"),
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = {
                    selectedIndex = index
                    when (index) {
                        0 -> onMenuClick()
                        1 -> onNotificationsClick()
                        2 -> onChatClick()
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp),
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = TechGidTheme.extendedColors.textTertiary,
                    indicatorColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    }
}

private data class BottomNavItem(
    val icon: ImageVector,
    val label: String,
)
