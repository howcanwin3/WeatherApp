package com.example.weatherforecastapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WeatherPrimaryDark,
    secondary = WeatherSecondaryDark,
    tertiary = WeatherTertiaryDark,
    background = Color(0xFF081120),
    surface = Color(0xFF101B2D),
    onPrimary = Color(0xFF07111F),
    onSecondary = Color(0xFF07111F),
    onTertiary = Color(0xFF07111F),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = WeatherPrimary,
    secondary = WeatherSecondary,
    tertiary = WeatherTertiary,
    background = WeatherBackground,
    surface = WeatherSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF0B1B2A),
    onBackground = Color(0xFF102033),
    onSurface = Color(0xFF102033)
)

@Composable
fun WeatherForecastAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
