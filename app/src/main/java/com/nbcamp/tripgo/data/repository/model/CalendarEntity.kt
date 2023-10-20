package com.nbcamp.tripgo.data.repository.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CalendarEntity(
    val contentId: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val title: String? = null,
    val description: String? = null,
    val telPhone: String? = null,
    val address: String? = null,
    val homePage: String? = null
) : Parcelable
