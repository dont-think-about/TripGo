package com.nbcamp.tripgo.view.reviewwriting

data class ReviewWritingModel(
    val gender: String,
    val generation: String,
    val companion: String,
    val reviewText: String,
    var imageUrl: String = "",
    val rating: Float
)
