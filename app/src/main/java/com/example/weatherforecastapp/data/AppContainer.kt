package com.example.weatherforecastapp.data

import com.example.weatherforecastapp.data.remote.WeatherApi
import com.example.weatherforecastapp.data.repository.WeatherRepository
import com.example.weatherforecastapp.data.repository.WeatherRepositoryImp

/**
 * 依赖注入容器，用于管理全局唯一的单例对象
 */
interface AppContainer {
    val weatherRepository: WeatherRepository
}

class DefaultAppContainer : AppContainer {
    // 使用之前定义的 WeatherApi.retrofitService
    override val weatherRepository: WeatherRepository by lazy {
        WeatherRepositoryImp(WeatherApi.retrofitService)
    }
}
