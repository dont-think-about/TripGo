package com.nbcamp.tripgo.data.model.weathers


import com.google.gson.annotations.SerializedName

data class WeatherResponseModel(
    @SerializedName("response")
    val response: Response
)
