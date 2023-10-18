package com.nbcamp.tripgo.data.model.weathers


import com.google.gson.annotations.SerializedName

data class Items(
    @SerializedName("Weatheritem")
    val weatherItem: List<WeatherItem>
)
