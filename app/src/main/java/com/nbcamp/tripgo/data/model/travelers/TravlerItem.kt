package com.nbcamp.tripgo.data.model.travelers

import com.google.gson.annotations.SerializedName

data class TravlerItem(
    @SerializedName("areaCode")
    val areaCode: String,
    @SerializedName("areaNm")
    val areaNm: String,
    @SerializedName("baseYmd")
    val baseYmd: String,
    @SerializedName("daywkDivCd")
    val daywkDivCd: String,
    @SerializedName("daywkDivNm")
    val daywkDivNm: String,
    @SerializedName("touDivCd")
    val touDivCd: String,
    @SerializedName("touDivNm")
    val touDivNm: String,
    @SerializedName("touNum")
    val touNum: String
)
