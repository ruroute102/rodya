package ru.techgid.presentation.screen.reminders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.techgid.data.local.entity.ReminderEntity
import ru.techgid.presentation.theme.TechGidColors
import ru.techgid.presentation.theme.TechGidTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var deleteTargetId by remember { mutableIntStateOf(-1) }

    if (deleteTargetId >= 0) {
        AlertDialog(
            onDismissRequest = { deleteTargetId = -1 },
            title = { Text("Удалить напоминание?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(deleteTargetId)
                    deleteTargetId = -1
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTargetId = -1 }) {
                    Text("Отмена")
                }
            },
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = "Напоминания о ТО",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    val overdueCount = state.items.count { !it.isDone && isOverdue(it) }
                    val activeCount = state.items.count { !it.isDone }
                    Text(
                        text = if (overdueCount > 0) "$activeCount активных · $overdueCount просрочено"
                        else "$activeCount активных",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (overdueCount > 0) TechGidColors.WarningDanger
                        else TechGidTheme.extendedColors.textTertiary,
                    )
                }
            }

            if (state.items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(40.dp),
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Пока нет напоминаний",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Нажмите +, чтобы добавить напоминание о замене масла, фильтра или сезонных шин",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TechGidTheme.extendedColors.textTertiary,
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.items, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onToggle = { viewModel.toggleDone(reminder) },
                            onDelete = { deleteTargetId = reminder.id },
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ) {
            AddReminderForm(
                onAdd = { title, mileage, date ->
                    viewModel.add(title, mileage, date)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false },
            )
        }
    }
}

private fun isOverdue(reminder: ReminderEntity): Boolean {
    val dateStr = reminder.dueDateIso ?: return false
    return runCatching {
        val due = LocalDate.parse(dateStr)
        due.isBefore(LocalDate.now())
    }.getOrDefault(false)
}

private fun daysUntilDue(reminder: ReminderEntity): Long? {
    val dateStr = reminder.dueDateIso ?: return null
    return runCatching {
        val due = LocalDate.parse(dateStr)
        ChronoUnit.DAYS.between(LocalDate.now(), due)
    }.getOrNull()
}

@Composable
private fun ReminderCard(
    reminder: ReminderEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val overdue = !reminder.isDone && isOverdue(reminder)
    val daysLeft = if (!reminder.isDone) daysUntilDue(reminder) else null
    val soonDue = daysLeft != null && daysLeft in 0..7

    val borderColor = when {
        overdue -> TechGidColors.WarningDanger.copy(alpha = 0.5f)
        soonDue -> TechGidColors.WarningCaution.copy(alpha = 0.5f)
        else -> TechGidTheme.extendedColors.cardBorder
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                overdue -> TechGidColors.WarningDanger.copy(alpha = 0.05f)
                soonDue -> TechGidColors.WarningCaution.copy(alpha = 0.05f)
                else -> TechGidTheme.extendedColors.cardBackground
            },
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = reminder.isDone,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = if (overdue) TechGidColors.WarningDanger
                    else TechGidTheme.extendedColors.textTertiary,
                ),
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (reminder.isDone) TechGidTheme.extendedColors.textTertiary
                        else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (reminder.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    )
                    if (overdue && !reminder.isDone) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Просрочено",
                            modifier = Modifier.size(16.dp),
                            tint = TechGidColors.WarningDanger,
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                val subtitle = buildString {
                    reminder.dueMileage?.let { append("по пробегу: ${formatKm(it)} км") }
                    if (reminder.dueMileage != null && reminder.dueDateIso != null) append(" · ")
                    reminder.dueDateIso?.let { append("до ${formatDate(it)}") }
                    if (reminder.dueMileage == null && reminder.dueDateIso == null) append("без срока")
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (overdue) TechGidColors.WarningDanger
                    else TechGidTheme.extendedColors.textTertiary,
                )
                if (daysLeft != null && !reminder.isDone) {
                    val statusText = when {
                        daysLeft < 0 -> "Просрочено на ${-daysLeft} дн."
                        daysLeft == 0L -> "Сегодня!"
                        daysLeft <= 7 -> "Через $daysLeft дн."
                        else -> null
                    }
                    val statusColor = when {
                        daysLeft < 0 -> TechGidColors.WarningDanger
                        daysLeft <= 3 -> TechGidColors.WarningCaution
                        else -> MaterialTheme.colorScheme.primary
                    }
                    if (statusText != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = statusColor,
                        )
                    }
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun AddReminderForm(
    onAdd: (title: String, dueMileage: Int?, dueDateIso: String?) -> Unit,
    onCancel: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Новое напоминание",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(onClick = onCancel) {
                Icon(Icons.Filled.Close, contentDescription = "Закрыть")
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Что напомнить") },
            placeholder = { Text("Замена масла") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = mileage,
            onValueChange = { mileage = it.filter(Char::isDigit).take(7) },
            label = { Text("По пробегу, км (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Дата, ГГГГ-ММ-ДД (необязательно)") },
            placeholder = { Text("2026-10-01") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = date.isNotEmpty() && !isValidDate(date),
            supportingText = if (date.isNotEmpty() && !isValidDate(date)) {
                { Text("Формат: ГГГГ-ММ-ДД") }
            } else null,
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                onAdd(
                    title.trim(),
                    mileage.toIntOrNull(),
                    date.trim().ifBlank { null },
                )
            },
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Сохранить",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun isValidDate(s: String): Boolean = runCatching {
    LocalDate.parse(s)
}.isSuccess

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
