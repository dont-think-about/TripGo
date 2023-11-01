package com.nbcamp.tripgo.view.review.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nbcamp.tripgo.data.repository.ReviewDetailRepositoryImpl

class ReviewDetailViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ReviewDetailViewModel::class.java)) {
            val reviewDetailRepository = ReviewDetailRepositoryImpl()

            return ReviewDetailViewModel(reviewDetailRepository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}
