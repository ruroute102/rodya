package ru.techgid.presentation.screen.home

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.techgid.presentation.theme.TechGidColors
import ru.techgid.presentation.theme.TechGidTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSelectCar: () -> Unit = {},
    onCatalog: () -> Unit = {},
    onDiagnostics: () -> Unit = {},
    onSearch: () -> Unit = {},
    onTechSpecs: () -> Unit = {},
    onViewer3D: () -> Unit = {},
    onServiceHistory: () -> Unit = {},
    onReminders: () -> Unit = {},
    onGuideClick: (Int) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        // Header с переключателем темы
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "ТехГид",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Ремонт и обслуживание автомобиля",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // Переключатель темы
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    contentDescription = "Тема",
                    tint = if (isDark) TechGidColors.SecondaryAmberDark else TechGidColors.SecondaryAmber,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(4.dp))
                Switch(
                    checked = isDark,
                    onCheckedChange = { viewModel.toggleTheme(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Текущий автомобиль
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectCar() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Мой автомобиль",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = state.carName,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Статус: пробег + последняя запись + ближайшее напоминание
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.History,
                title = "Последнее ТО",
                primary = state.lastRecord?.title ?: "Нет записей",
                secondary = state.lastRecord?.let {
                    "${formatKm(it.mileageKm)} км · ${formatDate(it.dateIso)}"
                } ?: "Добавьте первую запись",
                onClick = onServiceHistory,
            )
            StatusCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Notifications,
                title = "Ближайшее ТО",
                primary = state.nextReminder?.title ?: "Нет напоминаний",
                secondary = state.nextReminder?.let { rem ->
                    buildString {
                        rem.dueMileage?.let { append("${formatKm(it)} км") }
                        if (rem.dueMileage != null && rem.dueDateIso != null) append(" · ")
                        rem.dueDateIso?.let { append(formatDate(it)) }
                        if (rem.dueMileage == null && rem.dueDateIso == null) append("без срока")
                    }
                } ?: "Создайте напоминание",
                onClick = onReminders,
            )
        }

        Spacer(Modifier.height(24.dp))

        // Быстрые действия — 2x3 сетка
        Text(
            text = "Быстрые действия",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(Modifier.weight(1f), Icons.AutoMirrored.Filled.MenuBook, "Инструкции", "Каталог ремонтов", onCatalog)
            QuickActionCard(Modifier.weight(1f), Icons.Filled.Info, "Диагностика", "По симптомам", onDiagnostics)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(Modifier.weight(1f), Icons.Filled.Search, "Поиск", "Везде", onSearch)
            QuickActionCard(Modifier.weight(1f), Icons.Filled.Speed, "Техданные", "Спецификации", onTechSpecs)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(Modifier.weight(1f), Icons.Filled.DirectionsCar, "3D-модель", "Узлы авто", onViewer3D)
            QuickActionCard(Modifier.weight(1f), Icons.Filled.History, "История ТО", "Журнал", onServiceHistory)
        }

        Spacer(Modifier.height(24.dp))

        // Популярные ремонты — карточки с миниатюрой, звёздами, описанием
        Text(
            text = "Популярные ремонты",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))

        state.popularGuides.forEach { guide ->
            PopularGuideCard(
                title = guide.title,
                meta = guide.meta,
                difficulty = guide.difficulty,
                rating = 4,
                ratingCount = 12,
                onClick = { onGuideClick(guide.id) },
            )
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ─── Карточка быстрого действия ──────────────────────────────

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 11.sp, color = TechGidTheme.extendedColors.textTertiary)
            }
        }
    }
}

// ─── Карточка статуса ────────────────────────────────────────

@Composable
private fun StatusCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    primary: String,
    secondary: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = TechGidTheme.extendedColors.cardBackground),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 12.sp, color = TechGidTheme.extendedColors.textTertiary)
            }
            Spacer(Modifier.height(8.dp))
            Text(primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(secondary, fontSize = 11.sp, color = TechGidTheme.extendedColors.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ─── Карточка популярного гайда (с миниатюрой, звёздами) ─────

@Composable
private fun PopularGuideCard(
    title: String,
    meta: String,
    difficulty: String,
    rating: Int = 4,
    ratingCount: Int = 12,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = TechGidTheme.extendedColors.cardBackground),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            // Текст слева
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = meta,
                    fontSize = 12.sp,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
                Spacer(Modifier.height(4.dp))
                // Сложность
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(difficultyColor(difficulty).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = difficulty,
                        fontSize = 11.sp,
                        color = difficultyColor(difficulty),
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(Modifier.height(6.dp))
                // Звёзды
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (index < rating) TechGidColors.StarFilled else TechGidColors.StarEmpty,
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "$ratingCount",
                        fontSize = 11.sp,
                        color = TechGidTheme.extendedColors.textTertiary,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            // Миниатюра справа
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                )
            }
        }
    }
}

private fun difficultyColor(difficulty: String) = when (difficulty) {
    "Лёгкая" -> TechGidColors.DifficultyEasy
    "Средняя" -> TechGidColors.DifficultyMedium
    "Сложная" -> TechGidColors.DifficultyHard
    "Экспертная" -> TechGidColors.DifficultyExpert
    else -> TechGidColors.DifficultyMedium
}

private fun formatKm(value: Int): String {
    val str = value.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in str.length - 1 downTo 0) {
        sb.insert(0, str[i])
        count++
        if (count % 3 == 0 && i > 0) sb.insert(0, ' ')
    }
    return sb.toString()
}

private fun formatDate(iso: String): String {
    return runCatching {
        val (y, m, d) = iso.split("-")
        "$d.$m.$y"
    }.getOrDefault(iso)
}
