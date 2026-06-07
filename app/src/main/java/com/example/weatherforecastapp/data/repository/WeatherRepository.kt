import com.example.weatherforecastapp.data.remote.WeatherApiService
import com.example.weatherforecastapp.data.remote.model.WeatherDto
import com.example.weatherforecastapp.ui.mapper.toWeatherUiState
import com.example.weatherforecastapp.ui.screen.WeatherUiState


//仓库--->拿去给Container用
interface WeatherRepository{
    //写一个模板方法-->获取天气 -->return获取的数据
    suspend fun getWeather(locationId : String, apiKey : String) : WeatherDto
}

//继承仓库接口
class WeatherRepositoryImp(
    private val apiService : WeatherApiService//继承WeatherRepository接口,传入apiService成员变量
) : WeatherRepository{
    //重写函数体
    override suspend fun getWeather(locationId : String, apiKey : String): WeatherDto {
        return apiService.get3dWeather(locationId, apiKey)
    }
}


