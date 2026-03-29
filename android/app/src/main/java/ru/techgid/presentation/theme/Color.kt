package ru.techgid.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Цветовая палитра ТехГид.
 *
 * Светлая тема: мягкая, воздушная, с лёгкой глубиной.
 * Тёмная тема: глубокий тёмный фон, синие акценты, визуальная глубина.
 */
object TechGidColors {

    // ── Основные акценты ───────────────────────────────────────
    val PrimaryBlue = Color(0xFF2B6CB0)
    val PrimaryBlueDark = Color(0xFF4A9EF5)
    val SecondaryAmber = Color(0xFFF59E0B)
    val SecondaryAmberDark = Color(0xFFFBBF24)

    // ── Светлая тема ───────────────────────────────────────────
    val LightBackground = Color(0xFFF8FAFC)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceVariant = Color(0xFFF1F5F9)
    val LightCardBackground = Color(0xFFFFFFFF)
    val LightCardBorder = Color(0xFFE2E8F0)
    val LightTextPrimary = Color(0xFF1A202C)
    val LightTextSecondary = Color(0xFF64748B)
    val LightTextTertiary = Color(0xFF94A3B8)
    val LightDivider = Color(0xFFE2E8F0)
    val LightIconTint = Color(0xFF475569)

    // ── Тёмная тема ────────────────────────────────────────────
    val DarkBackground = Color(0xFF0F172A)
    val DarkSurface = Color(0xFF1E293B)
    val DarkSurfaceVariant = Color(0xFF334155)
    val DarkCardBackground = Color(0xFF1E293B)
    val DarkCardBorder = Color(0xFF334155)
    val DarkTextPrimary = Color(0xFFF1F5F9)
    val DarkTextSecondary = Color(0xFF94A3B8)
    val DarkTextTertiary = Color(0xFF64748B)
    val DarkDivider = Color(0xFF334155)
    val DarkIconTint = Color(0xFF94A3B8)

    // ── Статусы и сложность ────────────────────────────────────
    val DifficultyEasy = Color(0xFF22C55E)
    val DifficultyMedium = Color(0xFFF59E0B)
    val DifficultyHard = Color(0xFFEF4444)
    val DifficultyExpert = Color(0xFF8B5CF6)

    val WarningInfo = Color(0xFF3B82F6)
    val WarningCaution = Color(0xFFF59E0B)
    val WarningDanger = Color(0xFFEF4444)
    val WarningSafety = Color(0xFFEF4444)

    val StarFilled = Color(0xFFF59E0B)
    val StarEmpty = Color(0xFFCBD5E1)

    // ── 3D-подсветка ───────────────────────────────────────────
    val HighlightGlow = Color(0xFF4A9EF5)
    val HighlightGlowAlpha = Color(0x664A9EF5)
}
