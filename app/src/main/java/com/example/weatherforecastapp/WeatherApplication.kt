package com.example.weatherforecastapp

import android.app.Application
import com.example.weatherforecastapp.data.container.AppContainer
import com.example.weatherforecastapp.data.container.DefaultAppContainer

class WeatherApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(context = this)
    }
}
