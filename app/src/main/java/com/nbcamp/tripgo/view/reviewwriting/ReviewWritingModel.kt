package com.nbcamp.tripgo.view.reviewwriting

data class ReviewWritingModel(
    val contentId: String,
    val gender: String,
    val generation: String,
    val companion: String,
    val reviewText: String,
    var reviewImageUrl: String = "",
    val rating: Float,
    val userNickName: String,
    val tourTitle: String,
    val address: String,
    val userImageUrl: String,
    val schedule: String,
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "contentId" to contentId,
            "companion" to companion,
            "gender" to gender,
            "generation" to generation,
            "reviewImageUrl" to reviewImageUrl,
            "rating" to rating,
            "reviewText" to reviewText
        )
    }
}
