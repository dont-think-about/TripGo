package com.nbcamp.tripgo.view.home.valuetype

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProvincePlaceEntity(
    val areaCode: String,
    val name: String,
    val tourListCount: Int,
    val imageUrl: String
) : Parcelable
