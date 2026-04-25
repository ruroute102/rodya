package ru.techgid.presentation.screen.guide

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import ru.techgid.domain.model.Comment
import ru.techgid.presentation.components.WarningBlock
import ru.techgid.presentation.theme.TechGidColors
import ru.techgid.presentation.theme.TechGidTheme

@Composable
fun GuideDetailScreen(
    guideId: Int,
    viewModel: GuideDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.snackbarShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Назад",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (state.totalSteps > 0) {
                Text(
                    text = "Шаг ${state.currentStepIndex + 1}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = " из ${state.totalSteps}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.toggleFavorite() }) {
                Icon(
                    if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    "В избранное",
                    tint = if (state.isFavorite) MaterialTheme.colorScheme.error
                    else TechGidTheme.extendedColors.iconTint,
                )
            }
            IconButton(onClick = { viewModel.toggleOfflineSave() }) {
                Icon(
                    if (state.isSavedOffline) Icons.Filled.CloudDone else Icons.Filled.CloudDownload,
                    "Сохранить офлайн",
                    tint = if (state.isSavedOffline) MaterialTheme.colorScheme.primary
                    else TechGidTheme.extendedColors.iconTint,
                )
            }
        }

        // Step progress dots
        if (state.totalSteps > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(state.totalSteps) { index ->
                    val isDone = state.doneStepIds.contains(
                        state.guideDetail?.steps?.getOrNull(index)?.id ?: -1
                    )
                    val isCurrent = index == state.currentStepIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    isDone -> TechGidColors.DifficultyEasy
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    else -> TechGidTheme.extendedColors.cardBorder
                                }
                            ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null && state.guideDetail == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.error ?: "Ошибка загрузки",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            val currentStep = state.currentStep

            // Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Step title + description + done checkbox
                item {
                    if (currentStep != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentStep.title,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 32.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                            val isDone = currentStep.id in state.doneStepIds
                            IconButton(onClick = { viewModel.toggleStepDone(currentStep.id) }) {
                                Icon(
                                    imageVector = if (isDone) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                    contentDescription = if (isDone) "Шаг выполнен" else "Отметить выполненным",
                                    tint = if (isDone) TechGidColors.DifficultyEasy
                                    else TechGidTheme.extendedColors.iconTint,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentStep.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Illustration area with gradient
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (currentStep?.imageUrl != null) {
                            AsyncImage(
                                model = currentStep.imageUrl,
                                contentDescription = currentStep.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Build,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Иллюстрация · Шаг ${state.currentStepIndex + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TechGidTheme.extendedColors.textTertiary,
                                )
                            }
                        }
                    }
                }

                // Step navigation buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { if (state.canGoPrev) viewModel.prevStep() },
                            modifier = Modifier.weight(1f),
                            enabled = state.canGoPrev,
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Назад", style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = { if (state.canGoNext) viewModel.nextStep() },
                            modifier = Modifier.weight(1f),
                            enabled = state.canGoNext,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text("След. шаг", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                // Warnings
                if (currentStep != null) {
                    items(currentStep.warnings) { warning ->
                        WarningBlock(text = warning.text, severity = warning.severity)
                    }
                }

                // Tools
                if (currentStep != null && currentStep.tools.isNotEmpty()) {
                    item {
                        ExpandableSection(title = "Инструменты", icon = Icons.Filled.Build) {
                            currentStep.tools.forEach { tool ->
                                Row(
                                    modifier = Modifier.padding(start = 8.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Text("• ", style = MaterialTheme.typography.bodyMedium)
                                    Column {
                                        Text(
                                            text = buildString {
                                                append(tool.toolName)
                                                if (tool.toolSpec != null) append(" (${tool.toolSpec})")
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        if (!tool.isRequired) {
                                            Text(
                                                text = "необязательно",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TechGidTheme.extendedColors.textTertiary,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Consumables
                if (currentStep != null && currentStep.consumables.isNotEmpty()) {
                    item {
                        ExpandableSection(title = "Расходники и запчасти", icon = Icons.Filled.Inventory2) {
                            currentStep.consumables.forEach { consumable ->
                                Row(
                                    modifier = Modifier.padding(start = 8.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Text("• ", style = MaterialTheme.typography.bodyMedium)
                                    Column {
                                        Text(
                                            text = consumable.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Row {
                                            if (consumable.partNumber != null) {
                                                Text(
                                                    text = "Арт: ${consumable.partNumber}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                                Spacer(Modifier.width(8.dp))
                                            }
                                            if (consumable.quantity != null) {
                                                Text(
                                                    text = "x ${consumable.quantity}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TechGidTheme.extendedColors.textTertiary,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Checks
                if (currentStep != null && currentStep.checks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Проверки",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        currentStep.checks.forEach { check ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Icon(
                                    if (check.isPostRepair) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                                    null, Modifier.size(16.dp),
                                    tint = if (check.isPostRepair) TechGidColors.WarningCaution else TechGidColors.DifficultyEasy,
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = check.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    if (check.isPostRepair) {
                                        Text(
                                            text = "после завершения всех работ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TechGidColors.WarningCaution,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Divider before comments
                item {
                    HorizontalDivider(color = TechGidTheme.extendedColors.divider)
                }

                // Comments header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Комментарии",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "${state.comments.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                if (state.comments.isEmpty()) {
                    item {
                        Text(
                            text = "Пока нет комментариев к этому шагу",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TechGidTheme.extendedColors.textTertiary,
                        )
                    }
                } else {
                    items(state.comments, key = { it.id }) { comment ->
                        CommentItem(comment = comment)
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }

            // Bottom comment input - pinned at bottom
            HorizontalDivider(color = TechGidTheme.extendedColors.divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = state.commentText,
                    onValueChange = { viewModel.updateCommentText(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Добавить комментарий...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TechGidTheme.extendedColors.textTertiary,
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TechGidTheme.extendedColors.cardBackground,
                        unfocusedContainerColor = TechGidTheme.extendedColors.cardBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = TechGidTheme.extendedColors.cardBorder,
                    ),
                )
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = { viewModel.addComment() },
                    enabled = state.commentText.isNotBlank() && !state.isAddingComment,
                ) {
                    if (state.isAddingComment) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            "Отправить",
                            tint = if (state.commentText.isNotBlank()) MaterialTheme.colorScheme.primary
                            else TechGidTheme.extendedColors.textTertiary,
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TechGidTheme.extendedColors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null, Modifier.size(20.dp),
                    tint = TechGidTheme.extendedColors.textTertiary,
                )
            }
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                content()
            }
        }
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = comment.userName.take(1).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = comment.userName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = comment.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
            if (comment.userCarDisplay != null) {
                Text(
                    text = comment.userCarDisplay,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            comment.replies.forEach { reply ->
                Spacer(Modifier.height(8.dp))
                CommentItem(comment = reply)
            }
        }
    }
}
