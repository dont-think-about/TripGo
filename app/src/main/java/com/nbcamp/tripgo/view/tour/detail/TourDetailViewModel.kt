package com.nbcamp.tripgo.view.tour.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.kakao.sdk.user.model.Account
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.util.SingleLiveEvent
import com.nbcamp.tripgo.view.calendar.uistate.CalendarLogInUiState
import com.nbcamp.tripgo.view.tour.detail.uistate.CalendarSetScheduleUiState
import com.nbcamp.tripgo.view.tour.detail.uistate.DetailCommonUiState
import com.prolificinteractive.materialcalendarview.CalendarDay
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

    private val _loginStatus: MutableLiveData<CalendarLogInUiState> = MutableLiveData()
    val loginStatus: LiveData<CalendarLogInUiState>
        get() = _loginStatus

    private val _schedulesDateState: MutableLiveData<List<CalendarDay>> =
        MutableLiveData()
    val schedulesDateState: LiveData<List<CalendarDay>>
        get() = _schedulesDateState

    private val _myScheduleState: MutableLiveData<CalendarSetScheduleUiState> = MutableLiveData()
    val myScheduleState: LiveData<CalendarSetScheduleUiState>
        get() = _myScheduleState

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


    fun getMySchedules(currentUser: Any) {
        when (currentUser) {
            is FirebaseUser -> {
                if (currentUser.email == null) {
                    _myScheduleState.value = CalendarSetScheduleUiState.error("로그인이 되어있지 않습니다.")
                    return
                }
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        // 달력에 보여줄 정보
                        val myAllSchedules =
                            tourDetailRepository.getMySchedules(currentUser.email!!)
                        setSelectedDate(myAllSchedules)
                    }.onFailure {
                        println(it.localizedMessage)
                        _myScheduleState.postValue(CalendarSetScheduleUiState.error("오류가 발생했습니다."))
                    }
                }
            }

            is Account -> {
                if (currentUser.email == null) {
                    _myScheduleState.value = CalendarSetScheduleUiState.error("로그인이 되어있지 않습니다.")
                    return
                }
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        val myAllSchedules =
                            tourDetailRepository.getMySchedules(currentUser.email!!)
                        setSelectedDate(myAllSchedules)
                    }.onFailure {
                        println(it.localizedMessage)
                        _myScheduleState.postValue(CalendarSetScheduleUiState.error("오류가 발생했습니다."))
                    }
                }
            }
        }
    }

    // startDate ~ endDate 사이의 날짜를 달력을 표시 하기 위해 날짜 데이터를 만드는 함수
    private fun setSelectedDate(
        data: List<CalendarEntity>?
    ) {
        val dateList = arrayListOf<Triple<Int, Int, Int>>()
        data?.forEach { calendarEntity ->
            for (today in (calendarEntity.startDate?.toInt()
                ?.rangeTo(calendarEntity.endDate?.toInt()!!))!!) {
                val (year, date) = today.toString().chunked(4).map { it }
                val (month, day) = date.chunked(2).map { it.toInt() }
                dateList.add(Triple(year.toInt(), month, day))
            }
        }
        val selectedDateList = dateList.map { date ->
            CalendarDay.from(
                date.first,
                date.second,
                date.third
            )
        }
        _schedulesDateState.postValue(selectedDateList)
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

    fun getLoginStatus() {
        val currentUser = tourDetailRepository.getCurrentUser()
        when (currentUser) {
            is FirebaseUser -> {
                _loginStatus.value = CalendarLogInUiState(currentUser, true)
            }

            is Account -> {
                _loginStatus.value = CalendarLogInUiState(currentUser, true)
            }

            null -> {
                _loginStatus.value = CalendarLogInUiState(null, false)
            }
        }
    }
}
