package com.nbcamp.tripgo.data.repository.model

import com.nbcamp.tripgo.data.repository.mapper.WeatherType

data class WeatherEntity(
    // 기온
    val temperature: String,
    // 강수 형태
    val weatherType: WeatherType,
    // 강수량
    val precipitationPerHours: String
)
