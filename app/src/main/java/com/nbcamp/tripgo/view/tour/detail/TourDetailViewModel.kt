package com.nbcamp.tripgo.view.tour.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TourDetailViewModel(
    private val tourDetailRepository: TourDetailRepository
) : ViewModel() {
    private val _detailUiState: MutableLiveData<DetailCommonUiState> = MutableLiveData()
    val detailUiState: LiveData<DetailCommonUiState>
        get() = _detailUiState

    private val _textClickEvent: SingleLiveEvent<TextClickEvent> = SingleLiveEvent()
    val textClickEvent: SingleLiveEvent<TextClickEvent>
        get() = _textClickEvent

    fun runSearchDetailInformation(contentId: String?) {
        _detailUiState.value = DetailCommonUiState.initialize("로딩 중..")
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val response = tourDetailRepository.getDetailInformation(contentId)
                _detailUiState.postValue(DetailCommonUiState(response, "로딩 완료", false))
            }.onFailure {
                println(it.localizedMessage)
                _detailUiState.postValue(DetailCommonUiState.error("정보를 가져 오는데 실패했습니다."))
            }
        }
    }

    fun makeCall() {
        val phoneNumber = detailUiState.value?.detailInfo?.telPhoneNumber
        if (phoneNumber != null)
            _textClickEvent.value = TextClickEvent.PhoneNumberClickEvent(phoneNumber)
    }

    fun moveToHomePage() {
        val homePage = detailUiState.value?.detailInfo?.homePage
        if (homePage != null)
            _textClickEvent.value = TextClickEvent.HomePageClickEvent(homePage)
    }
}
