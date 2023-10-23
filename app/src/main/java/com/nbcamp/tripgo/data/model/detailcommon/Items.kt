package com.nbcamp.tripgo.data.model.detailcommon


import com.google.gson.annotations.SerializedName

data class Items(
    @SerializedName("item")
    val item: List<DetailCommonItem>
)
