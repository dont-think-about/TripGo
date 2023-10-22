package com.nbcamp.tripgo.view.reviewwriting

interface ReviewWritingRepository {
    suspend fun saveToStorage(
        reviewWritingModel: ReviewWritingModel,
        calendarUserModel: CalendarUserModel
    ): String

    suspend fun saveReview(
        reviewedModel: ReviewWritingModel
    )

    suspend fun updateReviewStatus(
        reviewedModel: ReviewWritingModel,
        documentId: String
    )

}
