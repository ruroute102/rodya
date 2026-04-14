package ru.techgid.presentation.screen.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.techgid.domain.model.Difficulty
import ru.techgid.domain.model.WarningSeverity
import ru.techgid.presentation.components.GuideCard
import ru.techgid.presentation.components.PrimaryButton
import ru.techgid.presentation.components.WarningBlock
import ru.techgid.presentation.theme.TechGidTheme

@Composable
fun CatalogScreen(
    configurationId: Int,
    viewModel: CatalogViewModel = hiltViewModel(),
    onGuideClick: (guideId: Int) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Назад",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = "Каталог инструкций",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.search(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = {
                Text(
                    text = "Поиск инструкции...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Поиск",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = TechGidTheme.extendedColors.cardBackground,
                unfocusedContainerColor = TechGidTheme.extendedColors.cardBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = TechGidTheme.extendedColors.cardBorder,
            ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter pills — consistent with SearchScreen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CatalogFilterPill(
                text = "Все",
                selected = state.selectedDifficulty == null,
                onClick = { viewModel.setDifficulty(null) },
            )
            Difficulty.entries.forEach { difficulty ->
                CatalogFilterPill(
                    text = difficulty.label,
                    selected = state.selectedDifficulty == difficulty,
                    onClick = { viewModel.setDifficulty(difficulty) },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Guide list
        if (state.isLoading && state.guides.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (state.guides.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = TechGidTheme.extendedColors.textTertiary.copy(alpha = 0.5f),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Инструкции не найдены",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Попробуйте изменить фильтры",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechGidTheme.extendedColors.textTertiary,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.guides, key = { it.id }) { guide ->
                    GuideCard(
                        guide = guide,
                        onClick = { onGuideClick(guide.id) },
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    PrimaryButton(
                        text = "Предложить инструкцию",
                        onClick = { },
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    WarningBlock(
                        text = "Все инструкции проходят модерацию. Следуйте технике безопасности.",
                        severity = WarningSeverity.INFO,
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun CatalogFilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else TechGidTheme.extendedColors.cardBackground,
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}
