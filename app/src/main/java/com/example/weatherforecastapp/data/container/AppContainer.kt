package com.example.weatherforecastapp.data.container

import android.content.Context
import com.example.weatherforecastapp.BuildConfig
import com.example.weatherforecastapp.data.local.WeatherDatabase
import com.example.weatherforecastapp.data.remote.WeatherApiService
import com.example.weatherforecastapp.data.repository.WeatherRepository
import com.example.weatherforecastapp.data.repository.WeatherRepositoryImp
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

interface AppContainer {
    val weatherRepository: WeatherRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val baseUrl = "https://n36yw2tu52.re.qweatherapi.com/"

    private val json = Json { ignoreUnknownKeys = true }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(baseUrl)
            .build()
    }

    private val retrofitService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }

    override val weatherRepository: WeatherRepository by lazy {
        WeatherRepositoryImp(
            apiService = retrofitService,
            weatherDao = WeatherDatabase.getDatabase(context).weatherDao(),
            apiKey = BuildConfig.QWEATHER_API_KEY
        )
    }
}
