package com.example.weatherforecastapp.ui.mapper

import com.example.weatherforecastapp.data.local.WeatherEntity
import com.example.weatherforecastapp.data.remote.model.WeatherDto
import com.example.weatherforecastapp.ui.screen.ForecastItem
import com.example.weatherforecastapp.ui.screen.WeatherUiState

fun WeatherDto.toWeatherUiState(cityName : String = "上海") : WeatherUiState {

    val today = this.dailyList.firstOrNull()//由于Dto拿到的是一个List->拿到列表中第一个元素
    //返回一个WeatherUiState对象
    return WeatherUiState.Success(
        cityName = cityName,
        currentTemperature = "${today?.tempMax ?: "0"}",
        weatherDescription = "${today?.textDay?:"--"}",
        forecastItems = this.dailyList.map{         //list的扩展函数map 遍历列表将箭头左边的类映射到右边
            dailyDto ->ForecastItem(
            dayOfWeek = dailyDto.fxDate,
            weather = dailyDto.textDay,
            temperatureRange = "${dailyDto.tempMin}-----${dailyDto.tempMax}"
            )
        }
    )
}

// 扩展函数 toEntity ----> 为了将 Dto 对象映射成 Entity 对象
fun WeatherDto.toEntity(cityName: String): WeatherEntity {
    // 拿到 dailyList 里第一天的数据（代表当前或今日天气）
    val today = this.dailyList.firstOrNull()
    return WeatherEntity(
        cityName = cityName,
        currentTemperature = "${today?.tempMax ?: "0"}℃", // 建议带上单位，保持一致
        description = today?.textDay ?: "--"
    )
}

//这里还需要再将Entity 对象映射成 WeatherUiState 对象
fun WeatherEntity.toWeatherUiStateFromCache(): WeatherUiState.Success{
    return WeatherUiState.Success(
        cityName = this.cityName,
        currentTemperature = this.currentTemperature,
        weatherDescription = this.description,
        forecastItems = emptyList()
    )
}