package com.nbcamp.tripgo.view.review.whole

import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingModel

data class ReviewUiState(
    val allSchedules: List<ReviewWritingModel>?,
    val message: String?,
    val isLoading: Boolean
) {
    companion object {
        fun initialize(message: String?) = ReviewUiState(
            allSchedules = emptyList(),
            message = message,
            isLoading = true
        )

        fun error(message: String?) = ReviewUiState(
            allSchedules = emptyList(),
            message = message,
            isLoading = false
        )
    }
}
