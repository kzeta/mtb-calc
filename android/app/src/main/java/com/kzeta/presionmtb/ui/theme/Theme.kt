package com.kzeta.presionmtb.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.kzeta.presionmtb.R

val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
)

data class Palette(
    val bg: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val ink: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    val line: Color,
    val lime: Color,
    val onLime: Color,
    val isDark: Boolean,
)

private val Lime = Color(0xFFC6F54A)
private val OnLime = Color(0xFF1A2208)

val DarkPalette = Palette(
    bg = Color(0xFF0E1110),
    surface = Color(0xFF171C1A),
    surfaceHigh = Color(0xFF222826),
    ink = Color(0xFFF4F7F2),
    muted = Color(0xFFB7C0B6),
    accent = Lime,
    onAccent = OnLime,
    line = Color(0xFF3A433C),
    lime = Lime,
    onLime = OnLime,
    isDark = true,
)

val LightPalette = Palette(
    bg = Color(0xFFF3F5EF),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFE7EBE3),
    ink = Color(0xFF141816),
    muted = Color(0xFF5C675F),
    accent = Color(0xFF4E6B12),
    onAccent = Color(0xFFF6FBE6),
    line = Color(0xFFD5DBD0),
    lime = Lime,
    onLime = OnLime,
    isDark = false,
)

val LocalPalette = staticCompositionLocalOf { DarkPalette }

private val AppType = Typography(
    displayLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 56.sp, letterSpacing = (-1).sp),
    headlineMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
)

@Composable
fun PresionTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val palette = if (dark) DarkPalette else LightPalette
    val scheme = if (dark) {
        darkColorScheme(
            primary = palette.lime,
            onPrimary = palette.onLime,
            background = palette.bg,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.surfaceHigh,
            onSurfaceVariant = palette.muted,
            outline = palette.line,
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            background = palette.bg,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.surfaceHigh,
            onSurfaceVariant = palette.muted,
            outline = palette.line,
        )
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(colorScheme = scheme, typography = AppType, content = content)
    }
}
