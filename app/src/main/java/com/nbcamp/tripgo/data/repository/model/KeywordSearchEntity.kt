package com.nbcamp.tripgo.data.repository.model

import com.nbcamp.tripgo.data.repository.mapper.WeatherType

data class KeywordSearchEntity(
    val contentId: String,
    val title: String,
    val address: String,
    val imageUrl: String,
    val latitude: String,
    val longitude: String,
    var weatherType: WeatherType,
    var temperature: String
)
