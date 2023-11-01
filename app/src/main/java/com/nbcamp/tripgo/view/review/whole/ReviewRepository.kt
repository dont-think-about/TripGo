package com.nbcamp.tripgo.view.review.whole

import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingModel

interface ReviewRepository {
    suspend fun getAllReviews(): List<ReviewWritingModel>
}
