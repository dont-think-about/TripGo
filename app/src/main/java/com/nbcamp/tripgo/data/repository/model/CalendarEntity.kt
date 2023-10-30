package com.nbcamp.tripgo.data.repository.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class CalendarEntity(
    @DocumentId
    val id: String? = null,
    val contentId: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    val title: String? = null,
    val description: String? = null,
    val telPhone: String? = null,
    val address: String? = null,
    val homePage: String? = null,
    @field:JvmField
    val isReviewed: Boolean? = null
) : Parcelable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "address" to address,
            "contentId" to contentId,
            "description" to description,
            "endDate" to endDate,
            "homePage" to homePage,
            "isReviewed" to isReviewed,
            "startDate" to startDate,
            "telPhone" to telPhone,
            "title" to title
        )
    }
}
