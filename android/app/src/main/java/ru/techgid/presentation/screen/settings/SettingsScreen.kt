package ru.techgid.presentation.screen.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.techgid.presentation.theme.TechGidTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val darkTheme = state.darkTheme
    val notifications = state.notifications
    val maintenanceReminders = state.maintenanceReminders
    val offlineSync = state.offlineSync
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showLanguageDialog by remember { mutableStateOf(false) }
    val comingSoon: () -> Unit = {
        scope.launch { snackbarHostState.showSnackbar("Будет доступно в следующей версии") }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Text(
                    text = "Настройки",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Внешний вид")
            SettingsGroup {
                SwitchRow(
                    icon = Icons.Filled.DarkMode,
                    title = "Тёмная тема",
                    subtitle = "Использовать тёмное оформление",
                    checked = darkTheme,
                    onCheckedChange = viewModel::toggleDarkTheme,
                )
                DividerItem()
                ClickableRow(
                    icon = Icons.Filled.Language,
                    title = "Язык",
                    subtitle = if (state.language == "ru") "Русский" else "English",
                    onClick = { showLanguageDialog = true },
                )
            }

            SectionHeader("Уведомления")
            SettingsGroup {
                SwitchRow(
                    icon = Icons.Filled.Notifications,
                    title = "Push-уведомления",
                    subtitle = "Новые инструкции, ответы",
                    checked = notifications,
                    onCheckedChange = viewModel::toggleNotifications,
                )
                DividerItem()
                SwitchRow(
                    icon = Icons.Filled.NotificationsActive,
                    title = "Напоминания о ТО",
                    subtitle = "По пробегу и времени",
                    checked = maintenanceReminders,
                    onCheckedChange = viewModel::toggleMaintenanceReminders,
                )
            }

            SectionHeader("Данные и хранилище")
            SettingsGroup {
                SwitchRow(
                    icon = Icons.Filled.CloudDownload,
                    title = "Автосинхронизация офлайн",
                    subtitle = "Скачивать обновления по Wi-Fi",
                    checked = offlineSync,
                    onCheckedChange = viewModel::toggleOfflineSync,
                )
                DividerItem()
                ClickableRow(
                    icon = Icons.Filled.Storage,
                    title = "Использовано места",
                    subtitle = "124 МБ из 1 ГБ",
                    onClick = comingSoon,
                )
                DividerItem()
                ClickableRow(
                    icon = Icons.Filled.Storage,
                    title = "Очистить кэш",
                    subtitle = "Освободить ~32 МБ",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Кэш очищен (32 МБ)")
                        }
                    },
                )
            }

            SectionHeader("Безопасность")
            SettingsGroup {
                ClickableRow(
                    icon = Icons.Filled.Lock,
                    title = "Сменить пароль",
                    subtitle = null,
                    onClick = comingSoon,
                )
                DividerItem()
                ClickableRow(
                    icon = Icons.Filled.PrivacyTip,
                    title = "Конфиденциальность",
                    subtitle = null,
                    onClick = comingSoon,
                )
            }

            SectionHeader("О приложении")
            SettingsGroup {
                ClickableRow(
                    icon = Icons.Filled.Star,
                    title = "Оценить приложение",
                    subtitle = null,
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Спасибо! Перенаправляем в Google Play…")
                        }
                    },
                )
                DividerItem()
                ClickableRow(
                    icon = Icons.Filled.Info,
                    title = "Версия",
                    subtitle = "1.0.0 (build 1)",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("ТехГид v1.0.0 · Ремонт и обслуживание авто")
                        }
                    },
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showLanguageDialog) {
        val languages = listOf("ru" to "Русский", "en" to "English")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Выберите язык") },
            text = {
                Column {
                    languages.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = state.language == code,
                                onClick = {
                                    viewModel.setLanguage(code)
                                    showLanguageDialog = false
                                },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Закрыть")
                }
            },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = TechGidTheme.extendedColors.textTertiary,
        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
    ) {
        Column { content() }
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LeadingIconBox(icon)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

@Composable
private fun ClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LeadingIconBox(icon)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TechGidTheme.extendedColors.textTertiary,
            modifier = Modifier.size(12.dp),
        )
    }
}

@Composable
private fun LeadingIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun DividerItem() {
    HorizontalDivider(
        color = TechGidTheme.extendedColors.divider,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}
