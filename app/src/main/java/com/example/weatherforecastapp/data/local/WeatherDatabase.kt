package com.example.weatherforecastapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WeatherEntity::class], version = 1, exportSchema = false)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var Instance: WeatherDatabase? = null

        // 提供一个简单的获取数据库实例的方法
        fun getDatabase(context: Context): WeatherDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, WeatherDatabase::class.java, "weather_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
