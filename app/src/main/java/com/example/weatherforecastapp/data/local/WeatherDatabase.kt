package com.example.weatherforecastapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
//@Database注释需要多个参数
@Database(entities = [WeatherEntity::class], version = 1, exportSchema = false)
//去继承RoomDatabase类，以便Room编译器生成代码
abstract class  WeatherDatabase : RoomDatabase() {
    //定义抽象方法：返回Dao接口的实现
    abstract fun weatherDao(): WeatherDao
}