package com.nbcamp.tripgo.view.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.kakao.sdk.user.model.Account
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.util.SingleLiveEvent
import com.nbcamp.tripgo.view.calendar.uistate.CalendarLogInUiState
import com.nbcamp.tripgo.view.calendar.uistate.CalendarScheduleUiState
import com.nbcamp.tripgo.view.calendar.uistate.RunDialogUiState
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class CalendarViewModel(
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    private val _loginStatus: MutableLiveData<CalendarLogInUiState> = MutableLiveData()
    val loginStatus: LiveData<CalendarLogInUiState>
        get() = _loginStatus

    private val _myScheduleState: MutableLiveData<CalendarScheduleUiState> = MutableLiveData()
    val myScheduleState: LiveData<CalendarScheduleUiState>
        get() = _myScheduleState

    private val _schedulesDateState: MutableLiveData<List<CalendarDay>> =
        MutableLiveData()
    val schedulesDateState: LiveData<List<CalendarDay>>
        get() = _schedulesDateState

    private val _changedMonthState: MutableLiveData<List<CalendarEntity>?> = MutableLiveData()
    val changedMonthState: LiveData<List<CalendarEntity>?>
        get() = _changedMonthState

    private val _runDialogState: SingleLiveEvent<RunDialogUiState?> = SingleLiveEvent()
    val runDialogState: SingleLiveEvent<RunDialogUiState?>
        get() = _runDialogState

    private val _deleteScheduleUiState: MutableLiveData<CalendarScheduleUiState> = MutableLiveData()
    val deleteScheduleUiState: MutableLiveData<CalendarScheduleUiState>
        get() = _deleteScheduleUiState

    // 일정 수정 시 캘린더 클릭 이벤트를 처리할 라이브 데이터
    private val _calendarClickModifyEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val calendarClickModifyEvent: SingleLiveEvent<Boolean>
        get() = _calendarClickModifyEvent

    // 원본으로 하기 힘든 위치에 추가적인 날짜 필터링을 위해 캐싱 데이터를 생성
    private var cachingSchedule: List<CalendarEntity>? = null
    private val scheduleDates = arrayListOf<CalendarDay>()
    fun getLoginStatus() {
        val currentUser = calendarRepository.getCurrentUser()
        when (currentUser) {
            is FirebaseUser -> {
                println(currentUser.email)
                println(currentUser.isEmailVerified)
                _loginStatus.value = CalendarLogInUiState(currentUser, true)

            }

            is Account -> {
                println(currentUser.email)
                println(currentUser.isEmailVerified)
                _loginStatus.value = CalendarLogInUiState(currentUser, true)
            }

            null -> {
                _loginStatus.value = CalendarLogInUiState(null, false)
            }
        }
    }

    // 파이어스토어로 부터 데이터를 가져오고, 데이터의 상태에 따라 state 분기 처리 - CalendarScheduleUiState
    fun getSchedulesFromFireStoreDatabase() {
        _myScheduleState.value = CalendarScheduleUiState.initialize()
        when (val currentUser = calendarRepository.getCurrentUser()) {
            is FirebaseUser -> {
                if (currentUser.email == null) {
                    _myScheduleState.value = CalendarScheduleUiState.error("로그인이 되어있지 않습니다.")
                    return
                }

                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        // 달력에 보여줄 정보
                        val myAllSchedules = calendarRepository.getMySchedules(currentUser.email!!)
                        // 리사이클러 뷰에 보여줄 정보
                        val monthSchedules = myAllSchedules.filter {
                            it.startDate?.slice(4..5)?.toInt() == Calendar.getInstance()
                                .get(Calendar.MONTH) + 1
                        }.sortedBy { it.startDate?.toInt() }.toMutableList()
                        cachingSchedule = myAllSchedules
                        if (myAllSchedules.isEmpty()) {
                            _myScheduleState.postValue(
                                CalendarScheduleUiState.error(
                                    "일정을 추가하고 관리해보세요!",
                                )
                            )
                            return@launch
                        }
                        // 정상적으로 가져 왔을 때 myScheduleState livedata에 제공
                        _myScheduleState.postValue(
                            CalendarScheduleUiState(
                                myAllSchedules, monthSchedules, "", false
                            )
                        )
                    }.onFailure {
                        _myScheduleState.postValue(CalendarScheduleUiState.error("오류가 발생했습니다."))
                    }
                }
            }

            is Account -> {
                if (currentUser.email == null) {
                    _myScheduleState.value = CalendarScheduleUiState.error("로그인이 되어있지 않습니다.")
                    return
                }

                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        val myAllSchedules = calendarRepository.getMySchedules(currentUser.email!!)
                        val monthSchedules = myAllSchedules.filter {
                            it.startDate?.slice(4..5)?.toInt() == Calendar.getInstance()
                                .get(Calendar.MONTH) + 1
                        }.sortedBy { it.startDate?.toInt() }.toMutableList()
                        cachingSchedule = myAllSchedules
                        if (myAllSchedules.isEmpty()) {
                            _myScheduleState.postValue(
                                CalendarScheduleUiState(
                                    emptyList(), emptyList(), "일정을 추가하고 관리해보세요!", false
                                )
                            )
                            return@launch
                        }
                        _myScheduleState.postValue(
                            CalendarScheduleUiState(
                                myAllSchedules,
                                monthSchedules,
                                "",
                                false
                            )
                        )
                    }.onFailure {
                        _myScheduleState.postValue(CalendarScheduleUiState.error("오류가 발생했습니다."))
                    }
                }
            }
        }
    }

    // startDate ~ endDate 사이의 날짜를 달력을 표시 하기 위해 날짜 데이터를 만드는 함수
    fun setSelectedDate(
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
        val selectedDay = dateList.map { date ->
            CalendarDay.from(
                date.first,
                date.second,
                date.third
            )
        }
        _schedulesDateState.value = selectedDay
    }

    // 날짜 필터링을 통해 현재 달의 일정만 제공
    fun changeScheduleListForThisMonth(
        date: CalendarDay?
    ) {
        val changedMonth = date?.month
        val filteredSchedule = cachingSchedule?.filter {
            it.startDate?.chunked(4)?.last()?.chunked(2)
                ?.first() == changedMonth.toString() || it.endDate?.chunked(4)?.last()?.chunked(2)
                ?.first() == changedMonth.toString()
        }
        _changedMonthState.value = filteredSchedule?.sortedBy { it.startDate?.toInt() }
    }

    // 수정 날짜를 제한하기 위한 함수
    fun selectScheduleRange(dates: List<CalendarDay>, selectedDayList: List<CalendarDay>) {
        if (dates.last().isBefore(CalendarDay.today())) {
            // 선택한 범위가 오늘 보다 전이면, 선택을 못 하도록 막음
            scheduleDates.clear()
            _calendarClickModifyEvent.value = false
            return
        }
        if (dates.intersect(selectedDayList.toSet()).isNotEmpty()) {
            // 겹치는 부분이 있으면 이전 저장 되어 있던 것 제거
            // 제거 안하면 확인 클릭 했을 때 isEmpty 를 통과 하여 이상 현상 발생
            scheduleDates.clear()
            _calendarClickModifyEvent.value = true
            return
        }

        scheduleDates.clear()
        scheduleDates.addAll(dates)
    }

    fun runDialogForReviewWriting(
        clickDate: CalendarDay?,
        selectedDayList: ArrayList<CalendarDay>?
    ) {
        // 우선 오늘 보다는 작아야함
        val today = Calendar.getInstance(Locale.KOREA)
        val year = today.get(Calendar.YEAR)
        val month = today.get(Calendar.MONTH) + 1
        val day = today.get(Calendar.DATE)
        val todayInt =
            "$year${if (month < 10) "0${month}" else "$month"}$day".toInt()
        val clickedDate =
            "${clickDate?.year ?: 100}${clickDate?.month ?: 100}${if ((clickDate?.day ?: 0) < 10) "0${clickDate?.day ?: 100}" else clickDate?.day ?: 100}".toInt()
        val list =
            selectedDayList?.map {
                "${it.year}${it.month}${if (it.day < 10) "0${it.day}" else it.day}".toInt()
            } ?: emptyList()

        // 유효한 범위의 객체를 필터링
        val getDateRangeValidEntity =
            cachingSchedule?.filter {
                it.startDate.toString() <= clickedDate.toString() && clickedDate.toString() <= it.endDate.toString()
            }

        // 유효한 범위라면 리뷰를 작성할 수 있도록 함
        if (getDateRangeValidEntity?.isNotEmpty() == true) {
            _runDialogState.value = RunDialogUiState(
                getDateRangeValidEntity.first(),
                "",
                todayInt >= clickedDate && list.contains(clickedDate)
            )
            return
        }
        // 유효하지 않으면 리뷰를 작성 못하게 함
        _runDialogState.call()
    }


    // documentId 값을 통해 리뷰 삭제
    fun deleteMySchedule(model: CalendarEntity) {
        _deleteScheduleUiState.value = CalendarScheduleUiState.initialize()
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                if (model.id == null) {
                    return@launch
                }
                val myAllSchedules = calendarRepository.deleteSchedule(model.id)
                val monthSchedules = myAllSchedules.filter {
                    it.startDate?.slice(4..5)?.toInt() == Calendar.getInstance()
                        .get(Calendar.MONTH) + 1
                }.sortedBy { it.startDate?.toInt() }.toMutableList()
                cachingSchedule = myAllSchedules
                if (myAllSchedules.isEmpty()) {
                    _deleteScheduleUiState.postValue(
                        CalendarScheduleUiState.error(
                            "일정을 추가하고 관리해보세요!",
                        )
                    )
                    return@launch
                }
                _deleteScheduleUiState.postValue(
                    CalendarScheduleUiState(
                        myAllSchedules,
                        monthSchedules,
                        "",
                        false
                    )
                )

            }.onFailure {
                _deleteScheduleUiState.postValue(CalendarScheduleUiState.error("삭제에 실패하였습니다."))
            }
        }
    }
}
