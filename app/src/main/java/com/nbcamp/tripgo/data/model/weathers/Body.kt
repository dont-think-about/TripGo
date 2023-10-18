package com.nbcamp.tripgo.data.model.weathers


import com.google.gson.annotations.SerializedName

data class Body(
    @SerializedName("dataType")
    val dataType: String,
    @SerializedName("items")
    val items: Items,
    @SerializedName("numOfRows")
    val numOfRows: Int,
    @SerializedName("pageNo")
    val pageNo: Int,
    @SerializedName("totalCount")
    val totalCount: Int
)
