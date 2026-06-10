package com.example.weatherforecastapp

import android.app.Application
import com.example.weatherforecastapp.data.AppContainer
import com.example.weatherforecastapp.data.DefaultAppContainer

class MyApplication : Application() {
    /**
     * 全局唯一的依赖注入容器
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
