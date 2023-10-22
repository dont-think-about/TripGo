package com.nbcamp.tripgo.view.reviewwriting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReviewWritingViewModel(
    private val reviewWritingRepository: ReviewWritingRepository
) : ViewModel() {

    private val _eventButtonClick: SingleLiveEvent<ReviewWritingEvent> = SingleLiveEvent()
    val eventButtonClick: SingleLiveEvent<ReviewWritingEvent>
        get() = _eventButtonClick


    fun onClickGenderGroupEvent(checkedId: Int) {
        when (checkedId) {
            R.id.review_writing_toggle_man_button -> {
                _eventButtonClick.value = ReviewWritingEvent.EventGenderClick("남자")
            }

            R.id.review_writing_toggle_woman_button -> {
                _eventButtonClick.value = ReviewWritingEvent.EventGenderClick("여자")
            }
        }
    }

    fun onClickAgeGroupEvent(checkedId: Int) {
        when (checkedId) {
            R.id.review_writing_toggle_10s_button -> {
                _eventButtonClick.value = ReviewWritingEvent.EventGenerationClick("10대")
            }

            R.id.review_writing_toggle_20s_button -> {
                _eventButtonClick.value = ReviewWritingEvent.EventGenerationClick("20대")
            }

            R.id.review_writing_toggle_30s_button -> {
                _eventButtonClick.value = ReviewWritingEvent.EventGenerationClick("30대")
            }

            R.id.review_writing_toggle_40s_button -> {
                _eventButtonClick.value = ReviewWritingEvent.EventGenerationClick("40대")
            }

            R.id.review_writing_toggle_50s_button -> {
                _eventButtonClick.value = ReviewWritingEvent.EventGenerationClick("50대 이상")
            }
        }
    }

    fun onClickCompanionGroupEvent(checkedId: Int) {
        when (checkedId) {
            R.id.review_writing_toggle_family_button -> {
                _eventButtonClick.value = ReviewWritingEvent.EventCompanionClick("가족")
            }

            R.id.review_writing_toggle_bf_gf_button -> {
                _eventButtonClick.value = ReviewWritingEvent.EventCompanionClick("연인")
            }

            R.id.review_writing_toggle_friends_button -> {
                _eventButtonClick.value = ReviewWritingEvent.EventCompanionClick("친구")
            }

            R.id.review_writing_toggle_solo_button -> {
                _eventButtonClick.value = ReviewWritingEvent.EventCompanionClick("혼자")
            }

            R.id.review_writing_toggle_pet_button -> {
                _eventButtonClick.value = ReviewWritingEvent.EventCompanionClick("반려동물")
            }
        }
    }

    fun editReviewWriting(text: CharSequence?) {
        _eventButtonClick.value = ReviewWritingEvent.EventReviewWriting(text.toString())
    }

    fun setRatingReview(rating: Float) {
        _eventButtonClick.value = ReviewWritingEvent.EventSetRating(rating)
    }

    /**
     * 1. 이미지를 파이어스토어에 저장한다.
     * 2. 저장을 성공하면 ReviewWritingViewModel의 imageUrl 값을 업데이트한다.
     * 3. 업데이트 성공하면 firestore에 ReviewWritingModel을 저장한다.(업데이트)
     * 4. 저장을 성공하면 CalendarUserModel의 isReviewed 값을 업데이트 한다.
     */
    fun saveReview(
        reviewWritingViewModel: ReviewWritingModel,
        calendarUserEntity: CalendarUserModel
    ) {
        _eventButtonClick.postValue(ReviewWritingEvent.EventSubmitReview("저장 시작..", true))
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                // 1
                val returnImageUrl =
                    reviewWritingRepository.saveToStorage(
                        reviewWritingViewModel,
                        calendarUserEntity
                    )
                // 2
                updateReviewWritingModel(
                    reviewWritingViewModel,
                    returnImageUrl,
                    calendarUserEntity.model?.id
                )
            }.onFailure {
                _eventButtonClick.postValue(
                    ReviewWritingEvent.EventSubmitReview(
                        "리뷰 저장에 실패했습니다.",
                        false
                    )
                )
            }
        }
    }

    private fun updateReviewWritingModel(
        reviewWritingViewModel: ReviewWritingModel,
        returnImageUrl: String,
        documentId: String?
    ) {
        _eventButtonClick.postValue(ReviewWritingEvent.EventSubmitReview("저장 중..", true))
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                if (documentId == null) {
                    return@launch
                }
                // 2
                val reviewedModel = reviewWritingViewModel.copy(
                    imageUrl = returnImageUrl
                )
                // 3
                reviewWritingRepository.saveReview(reviewedModel)
                // 4
                reviewWritingRepository.updateReviewStatus(reviewedModel, documentId)
                _eventButtonClick.postValue(
                    ReviewWritingEvent.EventSubmitReview(
                        "리뷰 저장이 완료 되었습니다.",
                        false
                    )
                )
            }.onFailure {
                _eventButtonClick.postValue(
                    ReviewWritingEvent.EventSubmitReview(
                        "리뷰 저장에 실패했습니다.",
                        false
                    )
                )
            }
        }
    }
}
