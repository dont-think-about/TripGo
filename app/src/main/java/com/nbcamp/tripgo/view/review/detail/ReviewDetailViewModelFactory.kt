package com.nbcamp.tripgo.view.review.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nbcamp.tripgo.data.repository.ReviewDetailRepositoryImpl

class ReviewDetailViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ReviewDetailViewModel::class.java)) {
            val reviewDetailRepository = ReviewDetailRepositoryImpl()

            ReviewDetailViewModel(reviewDetailRepository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}
