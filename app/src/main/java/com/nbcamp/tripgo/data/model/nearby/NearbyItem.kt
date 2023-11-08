package com.nbcamp.tripgo.data.model.nearby

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NearbyItem(
    @SerializedName("addr1")
    val addr1: String,
    @SerializedName("addr2")
    val addr2: String,
    @SerializedName("areacode")
    val areacode: String,
    @SerializedName("booktour")
    val booktour: String,
    @SerializedName("cat1")
    val cat1: String,
    @SerializedName("cat2")
    val cat2: String,
    @SerializedName("cat3")
    val cat3: String,
    @SerializedName("contentid")
    val contentid: String,
    @SerializedName("contenttypeid")
    val contenttypeid: String,
    @SerializedName("cpyrhtDivCd")
    val cpyrhtDivCd: String,
    @SerializedName("createdtime")
    val createdtime: String,
    @SerializedName("dist")
    val dist: String,
    @SerializedName("firstimage")
    val firstimage: String,
    @SerializedName("firstimage2")
    val firstimage2: String,
    @SerializedName("mapx")
    val mapx: String,
    @SerializedName("mapy")
    val mapy: String,
    @SerializedName("mlevel")
    val mlevel: String,
    @SerializedName("modifiedtime")
    val modifiedtime: String,
    @SerializedName("sigungucode")
    val sigungucode: String,
    @SerializedName("tel")
    val tel: String,
    @SerializedName("title")
    val title: String
): Parcelable
