package com.nbcamp.tripgo.data.model.keywords


import com.google.gson.annotations.SerializedName

data class Items(
    @SerializedName("item")
    val item: List<KeywordItem>
)
