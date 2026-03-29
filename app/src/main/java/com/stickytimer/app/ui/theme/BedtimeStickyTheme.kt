package com.stickytimer.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val CyberNightColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF39D6F9),
    onPrimary = Color(0xFF00131A),
    primaryContainer = Color(0xFF003947),
    onPrimaryContainer = Color(0xFFBDEFFF),
    secondary = Color(0xFF67E4CC),
    onSecondary = Color(0xFF002019),
    secondaryContainer = Color(0xFF103A33),
    onSecondaryContainer = Color(0xFF99F8E6),
    tertiary = Color(0xFF8AA9FF),
    onTertiary = Color(0xFF08133A),
    tertiaryContainer = Color(0xFF1A2A59),
    onTertiaryContainer = Color(0xFFDCE2FF),
    background = Color(0xFF070C17),
    onBackground = Color(0xFFE6EEF7),
    surface = Color(0xFF0D1628),
    onSurface = Color(0xFFE6EEF7),
    surfaceVariant = Color(0xFF1A2640),
    onSurfaceVariant = Color(0xFFB7C5DE),
    error = Color(0xFFFF6E7A),
    onError = Color(0xFF3C0008)
)

private val CyberNightTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.4.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.3.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    )
)

@Composable
fun BedtimeStickyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CyberNightColors,
        typography = CyberNightTypography,
        content = content
    )
}
