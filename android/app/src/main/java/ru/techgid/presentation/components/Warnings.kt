package ru.techgid.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.techgid.domain.model.WarningSeverity
import ru.techgid.presentation.theme.TechGidColors

/**
 * Блок предупреждения / меры предосторожности.
 */
@Composable
fun WarningBlock(
    text: String,
    severity: WarningSeverity,
    modifier: Modifier = Modifier,
) {
    val (bgColor, iconColor, icon) = when (severity) {
        WarningSeverity.INFO -> Triple(
            TechGidColors.WarningInfo.copy(alpha = 0.1f),
            TechGidColors.WarningInfo,
            Icons.Filled.Info,
        )
        WarningSeverity.CAUTION -> Triple(
            TechGidColors.WarningCaution.copy(alpha = 0.1f),
            TechGidColors.WarningCaution,
            Icons.Filled.Warning,
        )
        WarningSeverity.WARNING -> Triple(
            TechGidColors.WarningDanger.copy(alpha = 0.08f),
            TechGidColors.WarningDanger,
            Icons.Filled.Warning,
        )
        WarningSeverity.DANGER -> Triple(
            TechGidColors.WarningSafety.copy(alpha = 0.12f),
            TechGidColors.WarningSafety,
            Icons.Filled.Error,
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor, MaterialTheme.shapes.medium)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconColor,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
