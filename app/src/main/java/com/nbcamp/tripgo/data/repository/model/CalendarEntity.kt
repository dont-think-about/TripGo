package com.nbcamp.tripgo.data.repository.model

import com.google.firebase.firestore.DocumentId

data class CalendarEntity(
    @DocumentId
    val id: String? = null,
    val contentId: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val title: String? = null,
    val description: String? = null,
    val telPhone: String? = null,
    val address: String? = null,
    val homePage: String? = null,
    @field:JvmField
    val isReviewed: Boolean? = null
)
