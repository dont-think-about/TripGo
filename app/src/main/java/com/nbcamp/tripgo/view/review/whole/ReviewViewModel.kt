package com.nbcamp.tripgo.view.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.view.review.whole.ReviewRepository
import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val _reviewUiState: MutableLiveData<ReviewUiState> = MutableLiveData()
    val reviewUiState: LiveData<ReviewUiState>
        get() = _reviewUiState

    private val _filteredList: MutableLiveData<List<ReviewWritingModel>> = MutableLiveData()
    val filteredList: LiveData<List<ReviewWritingModel>>
        get() = _filteredList

    private var cachingReviews = listOf<ReviewWritingModel>()

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
                cachingReviews = allReview
            }.onFailure {
                _reviewUiState.postValue(ReviewUiState.error("로딩 실패.."))
            }
        }
    }

    fun setFilteredReview(category: String, text: CharSequence) {
        // 필터링 해서 - 빈 값일 때는 원상 복구
        val filteredReviews = when (text) {
            "" -> cachingReviews
            "성별" -> cachingReviews.filter { it.gender == category }
            "나이" -> cachingReviews.filter { it.generation == category }
            "동반인" -> cachingReviews.filter { it.companion == category }
            else -> {
                cachingReviews.filter {
                    it.address
                        .split(" ").first().split("")
                        .containsAll(category.split(""))
                }
            }
        }
        // 라이브데이터로 제공
        _filteredList.value = filteredReviews
    }
}
