package com.example.weatherforecastapp.data.remote


import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val BASE_URL ="https://devapi.qweather.com/"
private val json = Json { ignoreUnknownKeys = true }//如果等会儿从服务器送过来的 JSON 盒子里，有在数据类里没见过的属性（Unknown Keys），假装没看见
//初始化Retrofit供api接口用
private val retrofit : Retrofit by lazy{
        Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(BASE_URL)
            .build()
    }


//定义可用api接口
object WeatherApi {
    val retrofitService : WeatherApiService by lazy{
        retrofit.create(WeatherApiService::class.java)
    }

}