package com.example.weatherforecastapp.ui.mapper
import com.example.weatherforecastapp.data.remote.model.WeatherForecastDto
import com.example.weatherforecastapp.ui.screen.ForecastItem
import com.example.weatherforecastapp.ui.screen.WeatherUiState


//1.给WeatherForecastDto添加一个扩展方法
fun WeatherForecastDto.toWeatherUiState(cityName : String = "上海"):WeatherUiState{

    val today = this.dailyList.firstOrNull()//拿到List里的第一个数据

    return WeatherUiState(
        cityName = cityName,

        currentTemperature = today?.tempMax.orEmpty(),

        weatherDescription = today?.description.orEmpty(),

        forecastItems = this.dailyList.map {//map函数遍历dailyList
            dailyForecastDto -> ForecastItem(
                dayOfWeek = dailyForecastDto.fxDate,
                weather = dailyForecastDto.description,
                temperatureRange = "${dailyForecastDto.tempMin}-----${dailyForecastDto.tempMax}"
            )
        }
    )
}