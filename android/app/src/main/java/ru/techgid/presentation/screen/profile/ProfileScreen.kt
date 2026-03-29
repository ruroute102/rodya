package ru.techgid.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ru.techgid.presentation.theme.TechGidTheme

/**
 * Экран профиля / личного кабинета.
 * Автомобили, комментарии, офлайн-данные, роль, настройки.
 */
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {},
) {
    // Демо
    val userName = "Алексей Петров"
    val userPhone = "+7 900 123-45-67"
    val userRole = "Пользователь"
    val userCar = "Audi Q3 2011 · 2.0 TFSI"
    val offlineCount = 3
    val commentsCount = 12

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // ── Аватар и имя ───────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEditProfile, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Редактировать",
                        modifier = Modifier.size(16.dp),
                        tint = TechGidTheme.extendedColors.iconTint,
                    )
                }
            }

            Text(
                text = userPhone,
                style = MaterialTheme.typography.bodyMedium,
                color = TechGidTheme.extendedColors.textTertiary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = userRole,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Мой автомобиль ─────────────────────────────────────
        ProfileSection(title = "Мой автомобиль") {
            ProfileInfoRow(
                icon = Icons.Filled.DirectionsCar,
                label = userCar,
                trailingText = "основной",
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Активность ─────────────────────────────────────────
        ProfileSection(title = "Активность") {
            ProfileInfoRow(
                icon = Icons.AutoMirrored.Filled.Comment,
                label = "Мои комментарии",
                trailingText = "$commentsCount",
            )
            HorizontalDivider(color = TechGidTheme.extendedColors.divider)
            ProfileInfoRow(
                icon = Icons.Filled.Star,
                label = "Мои оценки",
                trailingText = "5",
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Офлайн-данные ──────────────────────────────────────
        ProfileSection(title = "Офлайн-данные") {
            ProfileInfoRow(
                icon = Icons.Filled.CloudDownload,
                label = "Сохранённые инструкции",
                trailingText = "$offlineCount",
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Настройки ──────────────────────────────────────────
        ProfileSection(title = "Настройки") {
            ProfileInfoRow(
                icon = Icons.Filled.Settings,
                label = "Настройки приложения",
            )
            HorizontalDivider(color = TechGidTheme.extendedColors.divider)
            ProfileInfoRow(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                label = "Выйти",
                tintColor = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = TechGidTheme.extendedColors.textTertiary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = TechGidTheme.extendedColors.cardBackground,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    trailingText: String? = null,
    tintColor: androidx.compose.ui.graphics.Color = TechGidTheme.extendedColors.iconTint,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = tintColor,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (tintColor == MaterialTheme.colorScheme.error) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f),
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelMedium,
                color = TechGidTheme.extendedColors.textTertiary,
            )
        }
    }
}
