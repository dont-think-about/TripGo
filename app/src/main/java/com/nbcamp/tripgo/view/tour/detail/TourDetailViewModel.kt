package com.nbcamp.tripgo.view.tour.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.kakao.sdk.user.model.Account
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.util.SingleLiveEvent
import com.nbcamp.tripgo.view.calendar.CalendarRepository
import com.nbcamp.tripgo.view.calendar.uistate.CalendarLogInUiState
import com.nbcamp.tripgo.view.tour.detail.uistate.CalendarSetScheduleUiState
import com.nbcamp.tripgo.view.tour.detail.uistate.DetailCommonUiState
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TourDetailViewModel(
    private val tourDetailRepository: TourDetailRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    private val _detailUiState: MutableLiveData<DetailCommonUiState> = MutableLiveData()
    val detailUiState: LiveData<DetailCommonUiState>
        get() = _detailUiState

    private val _textClickEvent: SingleLiveEvent<TextClickEvent> = SingleLiveEvent()
    val textClickEvent: SingleLiveEvent<TextClickEvent>
        get() = _textClickEvent

    private val _calendarClickEvent: SingleLiveEvent<Unit?> = SingleLiveEvent()
    val calendarClickEvent: SingleLiveEvent<Unit?>
        get() = _calendarClickEvent

    private val _calendarSubmitClickEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val calendarSubmitClickEvent: SingleLiveEvent<Boolean>
        get() = _calendarSubmitClickEvent

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

    private val scheduleDates = arrayListOf<CalendarDay>()

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
        // 이전에 지정한 일정 우선 제거
        scheduleDates.clear()
        _myScheduleState.value = CalendarSetScheduleUiState.initialize()
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
                            calendarRepository.getMySchedules(currentUser.email!!)
                        setSelectedDate(myAllSchedules)
                    }.onFailure {
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
                            calendarRepository.getMySchedules(currentUser.email!!)
                        setSelectedDate(myAllSchedules)
                    }.onFailure {
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
        _myScheduleState.postValue(CalendarSetScheduleUiState(emptyList(), null, false))
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
        val currentUser = calendarRepository.getCurrentUser()
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

    fun selectScheduleRange(dates: List<CalendarDay>, selectedDayList: List<CalendarDay>) {
        if (dates.intersect(selectedDayList.toSet()).isNotEmpty()) {
            // 겹치는 부분이 있으면 이전 저장 되어 있던 것 제거
            // 제거 안하면 확인 클릭 했을 때 isEmpty를 통과 하여 이상 현상 발생
            scheduleDates.clear()
            _calendarClickEvent.call()
            return
        }
        scheduleDates.clear()
        scheduleDates.addAll(dates)
    }

    fun saveMySchedule() {
        if (scheduleDates.isEmpty()) {
            _calendarSubmitClickEvent.value = false
            return
        }
        _calendarSubmitClickEvent.value = true
    }

    fun setUserOption() {
        val currentUser = loginStatus.value?.user
        when {
            currentUser == null -> {
                _loginStatus.value = CalendarLogInUiState(null, false)
            }

//            (currentUser as FirebaseUser).isEmailVerified.not() || (currentUser as Account).isEmailVerified!!.not() -> {
//                toast("이메일 인증이 되어 있지 않아 일정을 추가할 수 없습니다.")
            // 테스트 할 떄는 주석 풀고, 이메일 인증 기능이 완성 되면 주석 처리
//                tourDetailViewModel.getMySchedules(currentUser)
//                runCalendarDialog()
//            }

            else -> {
                currentUser.let {
                    _loginStatus.value = CalendarLogInUiState(currentUser, true)
                }
            }
        }
    }
}
