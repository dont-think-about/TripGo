package com.nbcamp.tripgo.data.model.travelers

import com.google.gson.annotations.SerializedName

data class Items(
    @SerializedName("item")
    val item: List<TravlerItem>
)
