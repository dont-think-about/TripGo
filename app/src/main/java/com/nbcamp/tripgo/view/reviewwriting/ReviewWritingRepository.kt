package com.nbcamp.tripgo.view.reviewwriting

import com.nbcamp.tripgo.view.calendar.WritingType

interface ReviewWritingRepository {
    suspend fun saveToStorage(
        reviewWritingModel: ReviewWritingModel,
        calendarUserModel: CalendarUserModel,
        writingType: WritingType
    ): String

    suspend fun saveReview(
        reviewedModel: ReviewWritingModel,
        documentId: String,
        writingType: WritingType
    )

    suspend fun updateReviewStatus(
        reviewedModel: ReviewWritingModel,
        documentId: String,
        writingType: WritingType
    )

    suspend fun setPastReviewForModifyReview(
        model: CalendarUserModel
    ): ReviewWritingModel?
}
