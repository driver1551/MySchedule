package com.example.projectnailsschedule.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val LightColorPalette = lightColorScheme(
    primary = AppPrimary,
    onPrimary = AppOnPrimary,
    primaryContainer = AppPrimaryVariant,
    onPrimaryContainer = AppOnPrimary,

    secondary = AppSecondary,
    onSecondary = AppOnSecondary,
    secondaryContainer = AppSecondaryVariant,
    onSecondaryContainer = AppOnSecondary,

    background = AppBackground,
    onBackground = AppOnBackground,

    surface = AppSurface,
    onSurface = AppOnSurface,
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val colors =  LightColorPalette

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
