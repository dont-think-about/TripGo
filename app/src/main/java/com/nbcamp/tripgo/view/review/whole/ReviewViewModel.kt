package com.nbcamp.tripgo.view.review.whole

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // 다중 필터링을 하는 메소드
    fun setFilteredReview(category: MutableMap<String, String>) {
        if (category.isEmpty()) {
            _filteredList.value = cachingReviews
            return
        }
        val map = category.filter { it.value.isNotEmpty() }.toSortedMap()
        var filteredReviews = cachingReviews
        map.forEach { (key, value) ->
            when (key) {
                "지역" -> filteredReviews = filteredReviews.filter {
                    it.address
                        .split(" ").first().split("")
                        .containsAll(value.split(""))
                }

                "성별" -> filteredReviews = filteredReviews.filter { it.gender == value }
                "나이" -> filteredReviews = filteredReviews.filter { it.generation == value }
                "동반인" -> filteredReviews = filteredReviews.filter { it.companion == value }
            }
        }

        // 라이브데이터로 제공
        _filteredList.value = filteredReviews
    }
}
