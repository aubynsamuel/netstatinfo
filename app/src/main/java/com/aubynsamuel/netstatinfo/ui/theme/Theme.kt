package com.aubynsamuel.netstatinfo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealNeon,
    onPrimary = Slate900,
    primaryContainer = Slate700,
    onPrimaryContainer = LightText,
    secondary = AmberNeon,
    onSecondary = Slate900,
    secondaryContainer = Slate800,
    onSecondaryContainer = LightText,
    tertiary = WifiColor,
    onTertiary = Slate900,
    background = Slate900,
    onBackground = LightText,
    surface = Slate800,
    onSurface = LightText,
    surfaceVariant = Slate700,
    onSurfaceVariant = LightText,
    outline = Slate700,
    error = MobileColor,
)

private val LightColorScheme = lightColorScheme(
    primary = TealDeep,
    onPrimary = White,
    primaryContainer = Slate100,
    onPrimaryContainer = DarkTextLight,
    secondary = AmberDeep,
    onSecondary = White,
    secondaryContainer = Slate50,
    onSecondaryContainer = DarkTextLight,
    tertiary = WifiColor,
    onTertiary = White,
    background = Slate50,
    onBackground = DarkTextLight,
    surface = White,
    onSurface = DarkTextLight,
    surfaceVariant = Slate100,
    onSurfaceVariant = LightTextLight,
    outline = Slate100,
    error = MobileColor,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NetStatInfoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled so our custom palette always applies
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
        motionScheme = MotionScheme.expressive()
    )
}