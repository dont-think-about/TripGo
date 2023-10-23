package com.nbcamp.tripgo.view.reviewwriting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.util.SingleLiveEvent
import com.nbcamp.tripgo.view.calendar.WritingType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReviewWritingViewModel(
    private val reviewWritingRepository: ReviewWritingRepository
) : ViewModel() {

    private val _eventButtonClick: SingleLiveEvent<ReviewWritingEvent> = SingleLiveEvent()
    val eventButtonClick: SingleLiveEvent<ReviewWritingEvent>
        get() = _eventButtonClick

    private val _eventLoadingReview: SingleLiveEvent<ReviewWritingEvent> = SingleLiveEvent()
    val eventLoadingReview: SingleLiveEvent<ReviewWritingEvent>
        get() = _eventLoadingReview


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

            R.id.review_writing_toggle_50s_over_button -> {
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
        calendarUserEntity: CalendarUserModel,
        writingType: WritingType
    ) {
        _eventButtonClick.postValue(ReviewWritingEvent.EventSubmitReview("저장 시작..", true))
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                // 작성, 수정에 따라 분기 처리 - 수정이면 기존 사진 삭제 후 다시 넣기
                val returnImageUrl =
                    reviewWritingRepository.saveToStorage(
                        reviewWritingViewModel,
                        calendarUserEntity,
                        writingType
                    )
                // 2
                updateReviewWritingModel(
                    reviewWritingViewModel,
                    returnImageUrl,
                    calendarUserEntity.model?.id,
                    writingType
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
        documentId: String?,
        writingType: WritingType
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
                reviewWritingRepository.saveReview(
                    reviewedModel,
                    documentId,
                    writingType
                )
                // 4
                reviewWritingRepository.updateReviewStatus(
                    reviewedModel,
                    documentId,
                    writingType
                )
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

    // 리뷰 수정 시 정보 불러 오기
    fun modifyReviewWriting(model: CalendarUserModel) {
        _eventLoadingReview.postValue(
            ReviewWritingEvent.EventLoadingPastReviewDetail(
                "리뷰 로딩 중..",
                null,
                true
            )
        )
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val response = reviewWritingRepository.setPastReviewForModifyReview(model)
                _eventLoadingReview.postValue(
                    ReviewWritingEvent.EventLoadingPastReviewDetail(
                        "수정 할 리뷰 로딩 완료",
                        response,
                        false
                    )
                )
            }.onFailure {
                _eventLoadingReview.postValue(
                    ReviewWritingEvent.EventLoadingPastReviewDetail(
                        "수정 할 리뷰 로딩 실패",
                        null,
                        false
                    )
                )
            }
        }
    }
}
