package com.example.weatherforecastapp.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.weatherforecastapp.R

internal fun weatherEmoji(description: String): String {
    return when {
        description.contains("雷") -> "⛈"
        description.contains("雪") -> "❄"
        description.contains("雨") -> "🌧"
        description.contains("云") || description.contains("阴") -> "☁"
        description.contains("雾") || description.contains("霾") -> "🌫"
        description.contains("晴") -> "☀"
        else -> "🌤"
    }
}

@Composable
internal fun WeatherSceneBackground(
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val backdrop = backdropForDescription(description)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(weatherCardGradient(description)))
    ) {
        Image(
            painter = painterResource(id = backdrop.imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = backdrop.imageAlpha,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backdrop.overlayBrush)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent,
                            Color(0xFF8FB0E8).copy(alpha = 0.18f),
                        )
                    )
                )
        )
        content()
    }
}

@Composable
internal fun ManagerSceneBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0C1424),
                        Color(0xFF17233B),
                        Color(0xFF203155),
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_weather_cloudy),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.34f,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xE60A1220),
                            Color(0xCC111D30),
                            Color(0xB8253760),
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        radius = 900f,
                    )
                )
        )
        content()
    }
}

private enum class WeatherBackdrop(
    @field:DrawableRes val imageRes: Int,
    val imageAlpha: Float,
    val overlayBrush: Brush,
) {
    SUNNY(
        imageRes = R.drawable.bg_weather_sunny,
        imageAlpha = 0.92f,
        overlayBrush = Brush.verticalGradient(
            listOf(
                Color(0x332A4D8F),
                Color(0x22335CA6),
                Color(0x774E79BE),
            )
        ),
    ),
    CLOUDY(
        imageRes = R.drawable.bg_weather_cloudy,
        imageAlpha = 0.88f,
        overlayBrush = Brush.verticalGradient(
            listOf(
                Color(0x55314571),
                Color(0x33476AA8),
                Color(0x885D7FBE),
            )
        ),
    ),
    RAINY(
        imageRes = R.drawable.bg_weather_rainy,
        imageAlpha = 0.86f,
        overlayBrush = Brush.verticalGradient(
            listOf(
                Color(0x88303F63),
                Color(0x5543608C),
                Color(0xAA627AA7),
            )
        ),
    ),
}

private fun backdropForDescription(description: String): WeatherBackdrop {
    return when {
        description.contains("雷") || description.contains("雨") -> WeatherBackdrop.RAINY
        description.contains("云") || description.contains("阴") || description.contains("雾") || description.contains("霾") || description.contains("雪") -> WeatherBackdrop.CLOUDY
        description.contains("晴") -> WeatherBackdrop.SUNNY
        else -> WeatherBackdrop.CLOUDY
    }
}

internal fun weatherCardGradient(description: String): List<Color> {
    return when {
        description.contains("雷") -> listOf(Color(0xFF24314F), Color(0xFF50618F), Color(0xFF8EA2D0))
        description.contains("雪") -> listOf(Color(0xFF4A5870), Color(0xFF91A5C5), Color(0xFFD9E6F7))
        description.contains("雨") -> listOf(Color(0xFF31466E), Color(0xFF6282B4), Color(0xFF9CB7E4))
        description.contains("云") || description.contains("阴") -> listOf(Color(0xFF364A72), Color(0xFF6988BB), Color(0xFFA7BDE2))
        description.contains("雾") || description.contains("霾") -> listOf(Color(0xFF606873), Color(0xFF97A0AE), Color(0xFFD6DDE6))
        description.contains("晴") -> listOf(Color(0xFF4A78D8), Color(0xFF87BBFF), Color(0xFFE3F2FF))
        else -> listOf(Color(0xFF3C5F95), Color(0xFF7F9FCD), Color(0xFFD3DDF3))
    }
}

internal fun parseTemperatureValue(temperature: String): Int? {
    val digits = temperature.filter { it.isDigit() || it == '-' }
    return digits.toIntOrNull()
}


