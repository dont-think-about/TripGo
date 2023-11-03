package com.nbcamp.tripgo.view.review.detail

interface ReviewDetailRepository {
    suspend fun getUserStatus(userInfo: String?): UserStatus?
}
