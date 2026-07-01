package com.example.weatherforecastapp.ui.mapper

import com.example.weatherforecastapp.data.local.WeatherEntity
import com.example.weatherforecastapp.data.remote.model.WeatherDto
import com.example.weatherforecastapp.ui.screen.ForecastItem
import com.example.weatherforecastapp.ui.screen.WeatherUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun WeatherDto.toWeatherUiState(cityName: String = "上海"): WeatherUiState.Success {
    val today = dailyList.firstOrNull()

    return WeatherUiState.Success(
        cityName = cityName,
        currentTemperature = "${today?.tempMax ?: "--"}℃",
        weatherDescription = today?.textDay ?: "--",
        forecastItems = dailyList.map { dailyDto ->
            ForecastItem(
                dayOfWeek = formatForecastLabel(dailyDto.fxDate),
                weather = dailyDto.textDay,
                temperatureRange = "${dailyDto.tempMin}℃ - ${dailyDto.tempMax}℃"
            )
        },
        lastUpdatedText = formatLastUpdated(System.currentTimeMillis()),
        sourceLabel = "实时数据"
    )
}

fun WeatherDto.toEntity(cityName: String): WeatherEntity {
    val today = dailyList.firstOrNull()
    return WeatherEntity(
        cityName = cityName,
        currentTemperature = "${today?.tempMax ?: "--"}℃",
        description = today?.textDay ?: "--"
    )
}

fun WeatherEntity.toWeatherUiStateFromCache(): WeatherUiState.Success {
    return WeatherUiState.Success(
        cityName = cityName,
        currentTemperature = currentTemperature,
        weatherDescription = description,
        forecastItems = emptyList(),
        lastUpdatedText = formatLastUpdated(lastUpdated),
        sourceLabel = "离线缓存"
    )
}

private fun formatLastUpdated(timestamp: Long): String {
    val formatter = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())
    return "更新于 ${formatter.format(Date(timestamp))}"
}

private fun formatForecastLabel(fxDate: String): String {
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = runCatching { parser.parse(fxDate) }.getOrNull() ?: return fxDate
    val calendar = Calendar.getInstance().apply { time = date }
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "周日"
        Calendar.MONDAY -> "周一"
        Calendar.TUESDAY -> "周二"
        Calendar.WEDNESDAY -> "周三"
        Calendar.THURSDAY -> "周四"
        Calendar.FRIDAY -> "周五"
        Calendar.SATURDAY -> "周六"
        else -> fxDate
    }
}
