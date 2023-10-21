package com.nbcamp.tripgo.view.reviewwriting

import androidx.lifecycle.ViewModel
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.util.SingleLiveEvent

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

}
