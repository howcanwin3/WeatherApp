package com.example.weatherforecastapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.weatherforecastapp.ui.screen.WeatherRoute
import com.example.weatherforecastapp.ui.theme.WeatherForecastAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherForecastAppTheme(dynamicColor = false) {
                WeatherRoute()
            }
        }
    }
}
