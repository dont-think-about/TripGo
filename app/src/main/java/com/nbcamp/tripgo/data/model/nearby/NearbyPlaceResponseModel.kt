package com.nbcamp.tripgo.data.model.nearby


import com.google.gson.annotations.SerializedName

data class NearbyPlaceResponseModel(
    @SerializedName("response")
    val response: Response
)
