package com.nbcamp.tripgo.view.reviewwriting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nbcamp.tripgo.data.repository.ReviewWritingRepositoryImpl

// viewModel에 생성자를 추가 해야할 때 만들어야 하는 viewModelFactory
class ReviewWritingViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ReviewWritingViewModel::class.java)) {
            val repository = ReviewWritingRepositoryImpl()
            return ReviewWritingViewModel(repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}
