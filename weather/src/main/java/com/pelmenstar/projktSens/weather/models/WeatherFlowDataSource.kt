package com.pelmenstar.projktSens.weather.models

import kotlinx.coroutines.flow.Flow

interface WeatherFlowDataSource: WeatherDataSource {
    fun weatherFlow(): Flow<WeatherInfo?>
}