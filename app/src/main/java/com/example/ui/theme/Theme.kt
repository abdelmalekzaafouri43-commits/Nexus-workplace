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
                onPrimary = TextWhite,
                onSecondary = TextWhite,
                onBackground = TextWhite,
                onSurface = TextWhite
            )
            AppThemeMode.EMERALD -> darkColorScheme(
                primary = EmeraldPrimary,
                secondary = EmeraldSecondary,
                tertiary = EmeraldAccent,
                background = EmeraldBackground,
                surface = EmeraldSurface,
                onPrimary = TextWhite,
                onSecondary = TextWhite,
                onBackground = TextWhite,
                onSurface = TextWhite
            )
            AppThemeMode.AMETHYST -> darkColorScheme(
                primary = AmethystPrimary,
                secondary = AmethystSecondary,
                tertiary = AmethystAccent,
                background = AmethystBackground,
                surface = AmethystSurface,
                onPrimary = TextWhite,
                onSecondary = TextWhite,
                onBackground = TextWhite,
                onSurface = TextWhite
            )
        }
    } else {
        when (themeMode) {
            AppThemeMode.SAPPHIRE -> lightColorScheme(
                primary = SapphirePrimary,
                secondary = SapphireSecondary,
                tertiary = Color(0xFF0284C7),
                background = Color(0xFFF8FAFC),
                surface = Color(0xFFFFFFFF),
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFF0F172A),
                onSurface = Color(0xFF0F172A)
            )
            AppThemeMode.EMERALD -> lightColorScheme(
                primary = EmeraldPrimary,
                secondary = EmeraldSecondary,
                tertiary = Color(0xFF047857),
                background = Color(0xFFF0FDF4),
                surface = Color(0xFFFFFFFF),
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFF022C22),
                onSurface = Color(0xFF022C22)
            )
            AppThemeMode.AMETHYST -> lightColorScheme(
                primary = AmethystPrimary,
                secondary = AmethystSecondary,
                tertiary = Color(0xFF6D28D9),
                background = Color(0xFFFBF5FF),
                surface = Color(0xFFFFFFFF),
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFF2E1065),
                onSurface = Color(0xFF2E1065)
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

