package com.example.weatherforecastapp.data.container

import WeatherRepository
import WeatherRepositoryImp
import com.example.weatherforecastapp.data.remote.WeatherApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

interface AppContainer {
    val weatherRepository: WeatherRepository//AppContainer的属性
}

class DefaultAppContainer : AppContainer {
    private val baseUrl = "https://devapi.qweather.com/"
    //手动造一个新json用于忽略不认识的字段
    private val json = Json { ignoreUnknownKeys = true }
    //初始化Retrofit
    private val retrofit : Retrofit by lazy{
        Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(baseUrl)
            .build()
    }
    //定义了一个私有的天气网络请求服务，让 Retrofit 帮你create一个对象。
    private val retrofitService : WeatherApiService by lazy{
        retrofit.create(WeatherApiService::class.java)
    }

    //实现数据仓库接口-->往数据仓库接口里传入retrofitService对象
    override val weatherRepository : WeatherRepository by lazy{
        WeatherRepositoryImp(retrofitService)
    }
}