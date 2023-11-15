package com.nbcamp.tripgo.data.model.travelerssegmentation

import com.google.gson.annotations.SerializedName

data class Items(
    @SerializedName("item")
    val segmentationItem: List<SegmentationItem>
)