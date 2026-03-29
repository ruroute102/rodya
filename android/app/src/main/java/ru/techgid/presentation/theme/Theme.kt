package ru.techgid.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Расширенная цветовая схема ТехГид — дополнительные цвета,
 * не входящие в Material 3 ColorScheme.
 */
@Immutable
data class TechGidExtendedColors(
    val cardBackground: Color,
    val cardBorder: Color,
    val textTertiary: Color,
    val divider: Color,
    val iconTint: Color,
    val difficultyEasy: Color = TechGidColors.DifficultyEasy,
    val difficultyMedium: Color = TechGidColors.DifficultyMedium,
    val difficultyHard: Color = TechGidColors.DifficultyHard,
    val difficultyExpert: Color = TechGidColors.DifficultyExpert,
    val warningInfo: Color = TechGidColors.WarningInfo,
    val warningCaution: Color = TechGidColors.WarningCaution,
    val warningDanger: Color = TechGidColors.WarningDanger,
    val starFilled: Color = TechGidColors.StarFilled,
    val starEmpty: Color = TechGidColors.StarEmpty,
    val highlightGlow: Color = TechGidColors.HighlightGlow,
    val accentAmber: Color,
)

val LocalTechGidColors = staticCompositionLocalOf {
    TechGidExtendedColors(
        cardBackground = Color.White,
        cardBorder = Color.LightGray,
        textTertiary = Color.Gray,
        divider = Color.LightGray,
        iconTint = Color.Gray,
        accentAmber = TechGidColors.SecondaryAmber,
    )
}

private val LightColorScheme = lightColorScheme(
    primary = TechGidColors.PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001A40),
    secondary = TechGidColors.SecondaryAmber,
    onSecondary = Color.White,
    background = TechGidColors.LightBackground,
    onBackground = TechGidColors.LightTextPrimary,
    surface = TechGidColors.LightSurface,
    onSurface = TechGidColors.LightTextPrimary,
    surfaceVariant = TechGidColors.LightSurfaceVariant,
    onSurfaceVariant = TechGidColors.LightTextSecondary,
    outline = TechGidColors.LightCardBorder,
    outlineVariant = TechGidColors.LightDivider,
)

private val DarkColorScheme = darkColorScheme(
    primary = TechGidColors.PrimaryBlueDark,
    onPrimary = Color(0xFF001A40),
    primaryContainer = Color(0xFF1A3A5C),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = TechGidColors.SecondaryAmberDark,
    onSecondary = Color(0xFF3D2800),
    background = TechGidColors.DarkBackground,
    onBackground = TechGidColors.DarkTextPrimary,
    surface = TechGidColors.DarkSurface,
    onSurface = TechGidColors.DarkTextPrimary,
    surfaceVariant = TechGidColors.DarkSurfaceVariant,
    onSurfaceVariant = TechGidColors.DarkTextSecondary,
    outline = TechGidColors.DarkCardBorder,
    outlineVariant = TechGidColors.DarkDivider,
)

private val LightExtendedColors = TechGidExtendedColors(
    cardBackground = TechGidColors.LightCardBackground,
    cardBorder = TechGidColors.LightCardBorder,
    textTertiary = TechGidColors.LightTextTertiary,
    divider = TechGidColors.LightDivider,
    iconTint = TechGidColors.LightIconTint,
    accentAmber = TechGidColors.SecondaryAmber,
)

private val DarkExtendedColors = TechGidExtendedColors(
    cardBackground = TechGidColors.DarkCardBackground,
    cardBorder = TechGidColors.DarkCardBorder,
    textTertiary = TechGidColors.DarkTextTertiary,
    divider = TechGidColors.DarkDivider,
    iconTint = TechGidColors.DarkIconTint,
    accentAmber = TechGidColors.SecondaryAmberDark,
)

@Composable
fun TechGidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalTechGidColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TechGidTypography,
            shapes = TechGidShapes,
            content = content,
        )
    }
}

/**
 * Доступ к расширенным цветам: TechGidTheme.extendedColors.cardBackground
 */
object TechGidTheme {
    val extendedColors: TechGidExtendedColors
        @Composable
        get() = LocalTechGidColors.current
}
