package ru.techgid.presentation.screen.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.techgid.domain.model.Difficulty
import ru.techgid.domain.model.GuideListItem
import ru.techgid.presentation.components.ActionChipButton
import ru.techgid.presentation.components.GuideCard
import ru.techgid.presentation.theme.TechGidTheme

/**
 * Экран каталога / поиска инструкций.
 *
 * Референс: поиск + фильтры-чипы + карточки инструкций.
 * Внизу: кнопки "Найти возле" и "Повернуть камеру".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    configurationId: Int,
    onGuideClick: (guideId: Int) -> Unit,
    onBack: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }

    // Демо-данные
    val demoGuides = remember {
        listOf(
            GuideListItem(
                id = 1,
                title = "Модуль топливного насоса (в баке)",
                slug = "fuel-pump-module",
                difficulty = Difficulty.MEDIUM,
                estimatedTimeMin = 90,
                isVerified = true,
                viewsCount = 1240,
                rating = 4.2f,
                ratingCount = 18,
                componentName = "Топливная система",
            ),
            GuideListItem(
                id = 2,
                title = "Насос низкого давления",
                slug = "low-pressure-pump",
                difficulty = Difficulty.MEDIUM,
                estimatedTimeMin = 60,
                viewsCount = 860,
                rating = 3.8f,
                ratingCount = 12,
                componentName = "Топливная система",
            ),
            GuideListItem(
                id = 3,
                title = "Замена масла и фильтра",
                slug = "oil-change",
                difficulty = Difficulty.EASY,
                estimatedTimeMin = 30,
                isVerified = true,
                viewsCount = 5600,
                rating = 4.8f,
                ratingCount = 92,
                componentName = "Двигатель",
            ),
            GuideListItem(
                id = 4,
                title = "Замена тормозных колодок",
                slug = "brake-pads",
                difficulty = Difficulty.EASY,
                estimatedTimeMin = 45,
                viewsCount = 3200,
                rating = 4.5f,
                ratingCount = 45,
                componentName = "Тормозная система",
            ),
        )
    }

    val categories = listOf("All", "Быстрый доступ", "Средняя", "Сохранённые")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── Поиск ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Поиск...",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Поиск",
                        modifier = Modifier.size(20.dp),
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TechGidTheme.extendedColors.cardBackground,
                    unfocusedContainerColor = TechGidTheme.extendedColors.cardBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = TechGidTheme.extendedColors.cardBorder,
                ),
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { /* TODO: фильтры */ }) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = "Фильтры",
                    tint = TechGidTheme.extendedColors.iconTint,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Фильтры-чипы ──────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Список карточек ────────────────────────────────────
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val filtered = demoGuides.filter { guide ->
                (searchQuery.isBlank() || guide.title.contains(searchQuery, ignoreCase = true))
                        && (selectedDifficulty == null || guide.difficulty == selectedDifficulty)
            }
            items(filtered, key = { it.id }) { guide ->
                GuideCard(
                    guide = guide,
                    onClick = { onGuideClick(guide.id) },
                )
            }
        }

        // ── Нижние кнопки действий ─────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActionChipButton(
                text = "Найти возле",
                onClick = { /* TODO: 3D-навигация */ },
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp),
                    )
                },
            )
            ActionChipButton(
                text = "Повернуть камеру",
                onClick = { /* TODO: 3D-камера */ },
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp),
                    )
                },
            )
        }
    }
}
