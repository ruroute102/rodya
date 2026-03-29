package ru.techgid.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.techgid.domain.model.Difficulty
import ru.techgid.domain.model.GuideListItem
import ru.techgid.presentation.theme.TechGidColors
import ru.techgid.presentation.theme.TechGidTheme

/**
 * Селектор параметра автомобиля (марка, модель, год...).
 * Чистый, минималистичный, как в референсе.
 */
@Composable
fun CarSelectorItem(
    label: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value ?: label,
                style = MaterialTheme.typography.titleMedium,
                color = if (value != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    TechGidTheme.extendedColors.textTertiary
                },
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = TechGidTheme.extendedColors.iconTint,
            )
        }
    }
}

/**
 * Карточка инструкции в каталоге.
 * Содержательная: изображение, название, рейтинг, сложность, метки.
 */
@Composable
fun GuideCard(
    guide: GuideListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            // Миниатюра
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (guide.thumbnailUrl != null) {
                    AsyncImage(
                        model = guide.thumbnailUrl,
                        contentDescription = guide.title,
                        modifier = Modifier.size(80.dp),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = guide.componentName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = TechGidTheme.extendedColors.textTertiary,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Текст и метки
            Column(
                modifier = Modifier.weight(1f),
            ) {
                // Название
                Text(
                    text = guide.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Узел + Сложность
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (guide.componentName.isNotEmpty()) {
                        Text(
                            text = guide.componentName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TechGidTheme.extendedColors.textTertiary,
                        )
                    }
                    DifficultyBadge(difficulty = guide.difficulty)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Рейтинг
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < guide.rating.toInt()) {
                                Icons.Filled.Star
                            } else {
                                Icons.Filled.StarBorder
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (index < guide.rating.toInt()) {
                                TechGidColors.StarFilled
                            } else {
                                TechGidColors.StarEmpty
                            },
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = guide.ratingCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TechGidTheme.extendedColors.textTertiary,
                    )

                    // Верифицирована
                    if (guide.isVerified) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Проверено",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Бейдж сложности.
 */
@Composable
fun DifficultyBadge(difficulty: Difficulty, modifier: Modifier = Modifier) {
    val (color, text) = when (difficulty) {
        Difficulty.EASY -> TechGidColors.DifficultyEasy to "Лёгкая"
        Difficulty.MEDIUM -> TechGidColors.DifficultyMedium to "Средняя"
        Difficulty.HARD -> TechGidColors.DifficultyHard to "Сложная"
        Difficulty.EXPERT -> TechGidColors.DifficultyExpert to "Экспертная"
    }
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}
