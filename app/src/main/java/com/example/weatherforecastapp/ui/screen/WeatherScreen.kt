package com.example.weatherforecastapp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecastapp.R

@Composable
fun WeatherScreen(
    modifier : Modifier = Modifier,
    weatherViewModel : WeatherViewModel = viewModel(factory = WeatherViewModel.Factory)
) {
    val uiState by weatherViewModel.uiState.collectAsState()
    val loadWeather = {
        weatherViewModel.fetchWeather(
            locationId = "101020100",
            apiKey = "f53a4407ba4c4886a8e7cb674f8a8264"
        )
    }
    LaunchedEffect(Unit) {
        loadWeather()
    }
    //添加一个When判定状态
    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is WeatherUiState.Loading -> {
                LoadingScreen()
            }

            is WeatherUiState.Success -> {
                WeatherForecastUI(state = state)
            }

            is WeatherUiState.Error -> {
                ErrorScreen(
                    message = state.message,
                            onRetry = loadWeather
                )
            }
        }
    }
}
//加载中界面
@Composable
fun LoadingScreen(modifier : Modifier = Modifier){
    Box(modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}
//加载成功界面
@Composable
fun WeatherForecastUI(
    state : WeatherUiState.Success,//加载成功界面
    modifier : Modifier = Modifier
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.weather_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(vertical = 100.dp, horizontal = 16.dp)) {
                Text(text = state.cityName, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.currentTemperature,
                    fontSize = 50.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.weatherDescription, color = Color.White)
            }
            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    state.forecastItems.forEach { items ->
                        Row(
                            modifier = Modifier.fillMaxWidth(), // 修复：使用新的 Modifier
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${items.dayOfWeek} ${items.weather}", color = Color.White)
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Text(text = items.temperatureRange, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun ErrorScreen(
    message : String,
    onRetry:()->Unit,
    modifier : Modifier = Modifier

){
    Column(modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "错误提示",
            tint =MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "加载失败",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White
            )
        )
        {
                Text(text = "重新加载")
        }


    }
}





@Preview(showBackground = true)
@Composable
fun WeatherForecastPreview() {
    WeatherScreen()
}
