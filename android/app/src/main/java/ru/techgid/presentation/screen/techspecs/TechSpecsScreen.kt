package ru.techgid.presentation.screen.techspecs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.techgid.presentation.theme.TechGidTheme

private data class TechSpecCategory(
    val icon: ImageVector,
    val name: String,
    val count: Int,
    val specs: List<TechSpec>,
)

private data class TechSpec(
    val key: String,
    val value: String,
    val unit: String,
    val range: String? = null,
    val notes: String? = null,
)

@Composable
fun TechSpecsScreen(
    viewModel: TechSpecsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val carName by viewModel.carName.collectAsState()
    val categories = remember {
        listOf(
            TechSpecCategory(
                icon = Icons.Filled.Info,
                name = "Жидкости и объёмы",
                count = 6,
                specs = listOf(
                    TechSpec("Масло двигателя", "4.6", "л", "5W-30 / 5W-40"),
                    TechSpec("Масло АКПП", "7.0", "л", "VAG G 060 162"),
                    TechSpec("Антифриз", "8.5", "л", "G12++ / G13"),
                    TechSpec("Тормозная жидкость", "1.0", "л", "DOT 4"),
                    TechSpec("Бак", "60", "л"),
                    TechSpec("Омыватель", "5.5", "л"),
                ),
            ),
            TechSpecCategory(
                icon = Icons.Filled.Speed,
                name = "Давления",
                count = 4,
                specs = listOf(
                    TechSpec("Топливная рампа", "4.5", "бар", "4.0–5.0 бар"),
                    TechSpec("Давление масла (хол. ход.)", "1.5", "бар"),
                    TechSpec("Давление масла (3000 об/мин)", "3.5", "бар"),
                    TechSpec("Давление в шинах", "2.3", "бар", "Передние/задние"),
                ),
            ),
            TechSpecCategory(
                icon = Icons.Filled.Build,
                name = "Моменты затяжки",
                count = 5,
                specs = listOf(
                    TechSpec("Болты ГБЦ (1 этап)", "40", "Нм"),
                    TechSpec("Болты ГБЦ (2 этап)", "+90°", "поворот"),
                    TechSpec("Болты колеса", "120", "Нм"),
                    TechSpec("Свечи зажигания", "25", "Нм"),
                    TechSpec("Сливная пробка масла", "30", "Нм"),
                ),
            ),
            TechSpecCategory(
                icon = Icons.Filled.Settings,
                name = "Зазоры",
                count = 3,
                specs = listOf(
                    TechSpec("Зазор свечей зажигания", "0.8", "мм"),
                    TechSpec("Зазор клапанов (впуск)", "0.20", "мм", "холодный двигатель"),
                    TechSpec("Зазор клапанов (выпуск)", "0.30", "мм", "холодный двигатель"),
                ),
            ),
            TechSpecCategory(
                icon = Icons.Filled.Star,
                name = "Электрика",
                count = 3,
                specs = listOf(
                    TechSpec("Аккумулятор", "70", "А·ч", "EN 680A"),
                    TechSpec("Генератор", "140", "А"),
                    TechSpec("Напряжение зарядки", "14.2", "В", "13.8–14.6 В"),
                ),
            ),
        )
    }

    var selectedCategory by remember { mutableStateOf<TechSpecCategory?>(null) }

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
            IconButton(onClick = {
                if (selectedCategory != null) selectedCategory = null else onBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = selectedCategory?.name ?: "Технические данные",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = carName,
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
        }

        if (selectedCategory == null) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { selectedCategory = category },
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(selectedCategory!!.specs) { spec ->
                    SpecRow(spec = spec)
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(category: TechSpecCategory, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${category.count} параметров",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TechGidTheme.extendedColors.textTertiary,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun SpecRow(spec: TechSpec) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = spec.key,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${spec.value} ${spec.unit}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            spec.notes?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
            spec.range?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Норма: $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
        }
    }
}
