package com.nbcamp.tripgo.view.reviewwriting

sealed interface ReviewWritingEvent {
    data class EventGenderClick(
        val gender: String
    ) : ReviewWritingEvent

    data class EventGenerationClick(
        val generation: String
    ) : ReviewWritingEvent

    data class EventCompanionClick(
        val companion: String
    ) : ReviewWritingEvent

    data class EventReviewWriting(
        val reviewText: String
    ) : ReviewWritingEvent

    data class EventSetRating(
        val rating: Float
    ) : ReviewWritingEvent

    data class EventSubmitReview(
        val message: String,
        val isLoading: Boolean
    ) : ReviewWritingEvent
}
