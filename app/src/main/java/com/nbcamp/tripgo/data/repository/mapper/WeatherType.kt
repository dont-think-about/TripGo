package com.nbcamp.tripgo.data.repository.mapper

enum class WeatherType(type: String) {
    SUNNY("0"),
    RAIN("1"),
    RAIN_OR_SNOW("2"),
    SNOW("3"),
    RAIN_DROP("5"),
    RAIN_SNOW_DROP("6"),
    SNOW_FLYING("7"),
    UNDEFINED("-1")
}
