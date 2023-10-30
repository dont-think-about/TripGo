package com.nbcamp.tripgo.data.model.nearby

import com.google.gson.annotations.SerializedName

data class Items(
    @SerializedName("item")
    val item: List<NearbyItem>
)
