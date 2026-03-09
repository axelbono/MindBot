package com.bono.mentalbot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Purple,
    secondary = PurpleLight,
    tertiary = PurpleDark,
    background = BackgroundDark,
    surface = BackgroundMedium,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Purple,
    secondary = PurpleLight,
    tertiary = PurpleDark,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)

/**
 * Tema de la aplicación que aplica una paleta clara u oscura.
 *
 * @param isDarkTheme Si es `true` se aplica el tema oscuro, de lo contrario el tema claro.
 * @param content Composable que se renderiza con el tema aplicado.
 */
@Composable
fun MentalBotTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}