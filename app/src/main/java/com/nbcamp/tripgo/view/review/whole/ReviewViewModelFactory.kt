package com.nbcamp.tripgo.view.review.whole

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nbcamp.tripgo.data.repository.ReviewRepositoryImpl
import com.nbcamp.tripgo.view.review.detail.ReviewDetailViewModel

// viewModel에 생성자를 추가 해야할 때 만들어야 하는 viewModelFactory
class ReviewViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            val reviewRepository = ReviewRepositoryImpl()

            ReviewViewModel(reviewRepository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}
