package com.example.weatherforecastapp.ui.screen

data class ForecastItem(
    val dayOfWeek: String,
    val weather: String,
    val temperatureRange: String,
)

data class WeatherUiState(
    val cityName: String,
    val currentTemperature: String,
    val weatherDescription: String,
    val forecastItems: List<ForecastItem>,//表示列表里面存的都是 ForecastItem类 的数据
)