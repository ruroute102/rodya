package ru.techgid.presentation.screen.comparison

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.techgid.presentation.theme.TechGidColors
import ru.techgid.presentation.theme.TechGidTheme

@Composable
fun ComparisonScreen(
    viewModel: ComparisonViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.CompareArrows,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Сравнение запчастей",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "OEM vs аналоги",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            itemsIndexed(state.items) { index, item ->
                ComparisonChip(
                    text = item.title,
                    subtitle = item.componentName,
                    selected = index == state.selectedIndex,
                    onClick = { viewModel.selectItem(index) },
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        AnimatedContent(
            targetState = state.current,
            label = "comparison_content",
        ) { item ->
            if (item != null) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            VariantCard(
                                variant = item.oem,
                                accentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                            )
                            VariantCard(
                                variant = item.aftermarket,
                                accentColor = TechGidColors.DifficultyMedium,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    item {
                        ComparisonRow(
                            label = "Цена",
                            oemValue = item.oem.priceRange,
                            altValue = item.aftermarket.priceRange,
                        )
                    }

                    item {
                        ComparisonRow(
                            label = "Гарантия",
                            oemValue = item.oem.warrantyMonths?.let { "$it мес." } ?: "—",
                            altValue = item.aftermarket.warrantyMonths?.let { "$it мес." } ?: "—",
                        )
                    }

                    item {
                        ComparisonRow(
                            label = "Ресурс",
                            oemValue = item.oem.durabilityKm ?: "—",
                            altValue = item.aftermarket.durabilityKm ?: "—",
                        )
                    }

                    item {
                        Text(
                            text = "Плюсы и минусы",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            ProsConsCard(
                                label = item.oem.label,
                                pros = item.oem.pros,
                                cons = item.oem.cons,
                                modifier = Modifier.weight(1f),
                            )
                            ProsConsCard(
                                label = item.aftermarket.label,
                                pros = item.aftermarket.pros,
                                cons = item.aftermarket.cons,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ComparisonChip(
    text: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary
    else TechGidTheme.extendedColors.cardBackground
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .then(
                if (!selected) Modifier.border(
                    1.dp,
                    TechGidTheme.extendedColors.cardBorder,
                    RoundedCornerShape(14.dp),
                ) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textColor,
                maxLines = 1,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun VariantCard(
    variant: RepairVariant,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = variant.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = accentColor,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = variant.partName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = variant.partNumber,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = TechGidColors.StarFilled,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "%.1f".format(variant.rating),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = variant.priceRange,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = accentColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    oemValue: String,
    altValue: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TechGidTheme.extendedColors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TechGidTheme.extendedColors.textTertiary,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "OEM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = oemValue,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Аналог",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechGidColors.DifficultyMedium,
                    )
                    Text(
                        text = altValue,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProsConsCard(
    label: String,
    pros: List<String>,
    cons: List<String>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TechGidTheme.extendedColors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TechGidTheme.extendedColors.textTertiary,
            )
            Spacer(Modifier.height(6.dp))
            pros.forEach { pro ->
                Row(
                    modifier = Modifier.padding(bottom = 3.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = TechGidColors.DifficultyEasy,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = pro,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            cons.forEach { con ->
                Row(
                    modifier = Modifier.padding(bottom = 3.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = TechGidColors.WarningDanger,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = con,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
