package com.example.weatherforecastapp.ui.mapper

import com.example.weatherforecastapp.data.local.WeatherEntity
import com.example.weatherforecastapp.data.remote.model.WeatherDto
import com.example.weatherforecastapp.ui.screen.ForecastItem
import com.example.weatherforecastapp.ui.screen.WeatherUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun WeatherDto.toWeatherUiState(
    cityName: String = "上海",
    sourceLabel: String = "实时数据"
): WeatherUiState.Success {
    val today = dailyList.firstOrNull()
    val highTemperature = formatTemperature(today?.tempMax)
    val lowTemperature = formatTemperature(today?.tempMin)

    return WeatherUiState.Success(
        cityName = cityName,
        currentTemperature = highTemperature,
        weatherDescription = today?.textDay ?: "--",
        forecastItems = dailyList.map { dailyDto ->
            ForecastItem(
                dayOfWeek = formatForecastLabel(dailyDto.fxDate),
                weather = dailyDto.textDay,
                lowTemperature = formatTemperature(dailyDto.tempMin),
                highTemperature = formatTemperature(dailyDto.tempMax),
                temperatureRange = "${formatTemperature(dailyDto.tempMin)} - ${formatTemperature(dailyDto.tempMax)}"
            )
        },
        lastUpdatedText = formatLastUpdated(System.currentTimeMillis()),
        sourceLabel = sourceLabel,
        highTemperature = highTemperature,
        lowTemperature = lowTemperature
    )
}

fun WeatherDto.toEntity(cityName: String): WeatherEntity {
    val today = dailyList.firstOrNull()
    return WeatherEntity(
        cityName = cityName,
        currentTemperature = formatTemperature(today?.tempMax),
        description = today?.textDay ?: "--",
        highTemperature = formatTemperature(today?.tempMax),
        lowTemperature = formatTemperature(today?.tempMin)
    )
}

fun WeatherEntity.toWeatherUiStateFromCache(sourceLabel: String = "离线缓存"): WeatherUiState.Success {
    return WeatherUiState.Success(
        cityName = cityName,
        currentTemperature = currentTemperature,
        weatherDescription = description,
        forecastItems = emptyList(),
        lastUpdatedText = formatLastUpdated(lastUpdated),
        sourceLabel = sourceLabel,
        highTemperature = highTemperature,
        lowTemperature = lowTemperature
    )
}

private fun formatTemperature(value: String?): String {
    return if (value.isNullOrBlank()) {
        "--"
    } else {
        "${value}℃"
    }
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
