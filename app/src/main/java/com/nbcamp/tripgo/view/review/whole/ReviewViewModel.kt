package com.nbcamp.tripgo.view.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.view.review.whole.ReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val _reviewUiState: MutableLiveData<ReviewUiState> = MutableLiveData()
    val reviewUiState: LiveData<ReviewUiState>
        get() = _reviewUiState

    fun getAllReviews() {
        _reviewUiState.value = ReviewUiState.initialize("로딩 중..")
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val allReview = reviewRepository.getAllReviews()
                _reviewUiState.postValue(
                    ReviewUiState(
                        allReview,
                        "로딩 성공",
                        false
                    )
                )
            }.onFailure {
                _reviewUiState.postValue(ReviewUiState.error("로딩 실패.."))
            }
        }
    }
}
