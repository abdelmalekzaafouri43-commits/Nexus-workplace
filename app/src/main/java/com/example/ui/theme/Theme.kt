package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.AppThemeMode

@Composable
fun EduGenTheme(
    themeMode: AppThemeMode = AppThemeMode.SAPPHIRE,
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkMode) {
        when (themeMode) {
            AppThemeMode.SAPPHIRE -> darkColorScheme(
                primary = SapphirePrimary,
                secondary = SapphireSecondary,
                tertiary = SapphireAccent,
                background = SapphireBackground,
                surface = SapphireSurface,
                surfaceVariant = Color(0xFF1E293B),
                outline = DarkBorder,
                outlineVariant = Color(0x1FFFFFFF),
                onPrimary = TextWhite,
                onSecondary = TextWhite,
                onBackground = TextWhite,
                onSurface = TextWhite,
                onSurfaceVariant = TextMuted
            )
            AppThemeMode.EMERALD -> darkColorScheme(
                primary = EmeraldPrimary,
                secondary = EmeraldSecondary,
                tertiary = EmeraldAccent,
                background = EmeraldBackground,
                surface = EmeraldSurface,
                surfaceVariant = Color(0xFF064E3B),
                outline = DarkBorder,
                outlineVariant = Color(0x1FFFFFFF),
                onPrimary = TextWhite,
                onSecondary = TextWhite,
                onBackground = TextWhite,
                onSurface = TextWhite,
                onSurfaceVariant = TextMuted
            )
            AppThemeMode.AMETHYST -> darkColorScheme(
                primary = AmethystPrimary,
                secondary = AmethystSecondary,
                tertiary = AmethystAccent,
                background = AmethystBackground,
                surface = AmethystSurface,
                surfaceVariant = Color(0xFF3B0764),
                outline = DarkBorder,
                outlineVariant = Color(0x1FFFFFFF),
                onPrimary = TextWhite,
                onSecondary = TextWhite,
                onBackground = TextWhite,
                onSurface = TextWhite,
                onSurfaceVariant = TextMuted
            )
        }
    } else {
        when (themeMode) {
            AppThemeMode.SAPPHIRE -> lightColorScheme(
                primary = SapphirePrimary,
                secondary = SapphireSecondary,
                tertiary = Color(0xFF0284C7),
                background = LightBackground,
                surface = LightSurface,
                surfaceVariant = Color(0xFFF8FAFC),
                outline = LightBorder,
                outlineVariant = LightBorderStrong,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = LightTextMain,
                onSurface = LightTextMain,
                onSurfaceVariant = LightTextMuted
            )
            AppThemeMode.EMERALD -> lightColorScheme(
                primary = EmeraldPrimary,
                secondary = EmeraldSecondary,
                tertiary = Color(0xFF047857),
                background = Color(0xFFF0FDF4),
                surface = LightSurface,
                surfaceVariant = Color(0xFFF8FAFC),
                outline = Color(0xFFA7F3D0),
                outlineVariant = Color(0xFF6EE7B7),
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFF022C22),
                onSurface = Color(0xFF022C22),
                onSurfaceVariant = Color(0xFF065F46)
            )
            AppThemeMode.AMETHYST -> lightColorScheme(
                primary = AmethystPrimary,
                secondary = AmethystSecondary,
                tertiary = Color(0xFF6D28D9),
                background = Color(0xFFFAF5FF),
                surface = LightSurface,
                surfaceVariant = Color(0xFFF8FAFC),
                outline = Color(0xFFE9D5FF),
                outlineVariant = Color(0xFFD8B4FE),
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFF2E1065),
                onSurface = Color(0xFF2E1065),
                onSurfaceVariant = Color(0xFF581C87)
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


