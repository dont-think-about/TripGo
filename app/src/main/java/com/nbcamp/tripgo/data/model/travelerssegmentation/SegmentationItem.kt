package com.nbcamp.tripgo.data.model.travelerssegmentation


import com.google.gson.annotations.SerializedName

data class SegmentationItem(
    @SerializedName("baseYmd")
    val baseYmd: String,
    @SerializedName("daywkDivCd")
    val daywkDivCd: String,
    @SerializedName("daywkDivNm")
    val daywkDivNm: String,
    @SerializedName("signguCode")
    val signguCode: String,
    @SerializedName("signguNm")
    val signguNm: String,
    @SerializedName("touDivCd")
    val touDivCd: String,
    @SerializedName("touDivNm")
    val touDivNm: String,
    @SerializedName("touNum")
    val touNum: String
)