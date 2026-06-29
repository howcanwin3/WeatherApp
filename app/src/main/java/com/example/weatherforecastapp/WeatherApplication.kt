package com.example.weatherforecastapp

import android.app.Application
import com.example.weatherforecastapp.data.container.AppContainer
import com.example.weatherforecastapp.data.container.DefaultAppContainer
//Application:全局唯一单例 --->把container写在里面 ---> Application不会受MainActivity销毁创建的影响
class WeatherApplication : Application(){
    lateinit var container : AppContainer

    override fun onCreate(){
        super.onCreate()
        container = DefaultAppContainer(context = this)
    }
}