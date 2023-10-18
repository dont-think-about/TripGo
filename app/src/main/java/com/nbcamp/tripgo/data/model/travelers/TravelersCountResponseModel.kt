package com.nbcamp.tripgo.data.model.travelers


import com.google.gson.annotations.SerializedName

data class TravelersCountResponseModel(
    @SerializedName("response")
    val response: Response
)
