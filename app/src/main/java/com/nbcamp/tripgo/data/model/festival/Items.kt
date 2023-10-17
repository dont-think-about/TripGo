package com.nbcamp.tripgo.data.model.festival


import com.google.gson.annotations.SerializedName

data class Items(
    @SerializedName("item")
    val item: List<FestivalItem>
)
