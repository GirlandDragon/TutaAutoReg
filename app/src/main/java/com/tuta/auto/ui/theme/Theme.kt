package com.tuta.auto.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.tuta.auto.TutaApp
import com.tuta.auto.util.PreferenceManager

private val LightColorScheme = lightColorScheme(
    primary = lightPrimary,
    onPrimary = lightOnPrimary,
    primaryContainer = lightPrimaryContainer,
    surface = lightSurface,
    background = lightBackground,
    surfaceVariant = lightSurfaceVariant,
    outline = lightOutline,
)

private val DarkColorScheme = darkColorScheme(
    primary = darkPrimary,
    onPrimary = darkOnPrimary,
    primaryContainer = darkPrimaryContainer,
    surface = darkSurface,
    background = darkBackground,
    surfaceVariant = darkSurfaceVariant,
    outline = darkOutline,
)

@Composable
fun TutaAutoRegTheme(
    content: @Composable () -> Unit
) {
    val app = LocalContext.current.applicationContext as TutaApp
    val themeMode by app.preferenceManager.themeMode.collectAsState(initial = PreferenceManager.ThemeMode.SYSTEM)

    val useDarkTheme = when (themeMode) {
        PreferenceManager.ThemeMode.LIGHT -> false
        PreferenceManager.ThemeMode.DARK -> true
        PreferenceManager.ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        Build.VERSION.SDK_INT >= 31 && useDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
        Build.VERSION.SDK_INT >= 31 -> dynamicLightColorScheme(LocalContext.current)
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
