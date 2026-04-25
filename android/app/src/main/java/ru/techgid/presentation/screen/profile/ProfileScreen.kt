package ru.techgid.presentation.screen.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.techgid.presentation.theme.TechGidTheme

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLogin: () -> Unit = {},
    onSettings: () -> Unit = {},
    onServiceHistory: () -> Unit = {},
    onFavorites: () -> Unit = {},
    onReminders: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выход из аккаунта") },
            text = { Text("Вы уверены, что хотите выйти? Офлайн-данные сохранятся на устройстве.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                    onLogout()
                }) {
                    Text("Выйти", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Отмена")
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Avatar + Name
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.primaryContainer,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoggedIn) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.userName.ifBlank { "Пользователь" },
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onEditProfile, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Редактировать",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                if (state.userPhone.isNotBlank()) {
                    Text(
                        text = state.userPhone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechGidTheme.extendedColors.textTertiary,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = state.userRole.ifBlank { "Пользователь" },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                Text(
                    text = "Гость",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Войдите, чтобы сохранять инструкции\nи оставлять комментарии",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Stats row
        if (state.isLoggedIn) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "${state.favoritesCount}",
                    label = "Избранное",
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "${state.historyCount}",
                    label = "Записей ТО",
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "${state.offlineCount}",
                    label = "Офлайн",
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // My car
        ProfileSection(title = "Мой автомобиль") {
            ProfileInfoRow(
                icon = Icons.Filled.DirectionsCar,
                label = state.userCar.ifBlank { "Не выбран" },
                trailingText = if (state.userCar.isNotBlank()) "основной" else null,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Activity
        ProfileSection(title = "Активность") {
            ProfileInfoRow(
                icon = Icons.Filled.Star,
                label = "Избранные инструкции",
                trailingText = "${state.favoritesCount}",
                onClick = onFavorites,
            )
            HorizontalDivider(
                color = TechGidTheme.extendedColors.divider,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            ProfileInfoRow(
                icon = Icons.Filled.History,
                label = "История обслуживания",
                trailingText = "${state.historyCount}",
                onClick = onServiceHistory,
            )
            HorizontalDivider(
                color = TechGidTheme.extendedColors.divider,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            ProfileInfoRow(
                icon = Icons.Filled.Notifications,
                label = "Напоминания о ТО",
                onClick = onReminders,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Offline data
        ProfileSection(title = "Офлайн-данные") {
            ProfileInfoRow(
                icon = Icons.Filled.CloudDownload,
                label = "Сохранённые инструкции",
                trailingText = "${state.offlineCount}",
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Settings
        ProfileSection(title = "Настройки") {
            ProfileInfoRow(
                icon = Icons.Filled.Settings,
                label = "Настройки приложения",
                onClick = onSettings,
            )
            HorizontalDivider(
                color = TechGidTheme.extendedColors.divider,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            if (state.isLoggedIn) {
                ProfileInfoRow(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    label = "Выйти",
                    tintColor = MaterialTheme.colorScheme.error,
                    onClick = { showLogoutDialog = true },
                )
            } else {
                ProfileInfoRow(
                    icon = Icons.Filled.Person,
                    label = "Войти в аккаунт",
                    tintColor = MaterialTheme.colorScheme.primary,
                    onClick = onLogin,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TechGidTheme.extendedColors.textTertiary,
            )
        }
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = TechGidTheme.extendedColors.cardBackground,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
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
    tintColor: Color = TechGidTheme.extendedColors.iconTint,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when (tintColor) {
                        MaterialTheme.colorScheme.error -> tintColor.copy(alpha = 0.1f)
                        MaterialTheme.colorScheme.primary -> tintColor.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = when (tintColor) {
                    MaterialTheme.colorScheme.error -> tintColor
                    MaterialTheme.colorScheme.primary -> tintColor
                    else -> MaterialTheme.colorScheme.primary
                },
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = when (tintColor) {
                MaterialTheme.colorScheme.error -> MaterialTheme.colorScheme.error
                MaterialTheme.colorScheme.primary -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f),
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelMedium,
                color = TechGidTheme.extendedColors.textTertiary,
            )
            Spacer(Modifier.width(4.dp))
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = TechGidTheme.extendedColors.textTertiary,
            )
        }
    }
}
