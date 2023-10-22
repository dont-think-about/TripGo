package com.nbcamp.tripgo.view.reviewwriting

data class ReviewWritingModel(
    val gender: String,
    val generation: String,
    val companion: String,
    val reviewText: String,
    var imageUrl: String = "",
    val rating: Float
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "companion" to companion,
            "gender" to gender,
            "generation" to generation,
            "imageUrl" to imageUrl,
            "rating" to rating,
            "reviewText" to reviewText
        )
    }
}
