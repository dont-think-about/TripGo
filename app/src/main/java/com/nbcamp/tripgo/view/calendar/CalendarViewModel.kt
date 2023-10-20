package com.nbcamp.tripgo.view.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.view.calendar.uistate.CalendarScheduleUiState
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CalendarViewModel(
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    private val _loginStatus: MutableLiveData<Boolean> = MutableLiveData(false)
    val loginStatus: LiveData<Boolean>
        get() = _loginStatus

    private val _myScheduleState: MutableLiveData<CalendarScheduleUiState> = MutableLiveData()
    val myScheduleState: LiveData<CalendarScheduleUiState>
        get() = _myScheduleState

    private val _schedulesDateState: MutableLiveData<List<Triple<Int, Int, Int>>> =
        MutableLiveData()
    val schedulesDateState: LiveData<List<Triple<Int, Int, Int>>>
        get() = _schedulesDateState

    private val _changedMonthState: MutableLiveData<List<CalendarEntity>?> = MutableLiveData()
    val changedMonthState: LiveData<List<CalendarEntity>?>
        get() = _changedMonthState

    private var cachingSchedule: List<CalendarEntity>? = null

    fun getLoginStatus() {
        val currentUser = calendarRepository.getCurrentUser()
        println(currentUser?.email)
        println(currentUser?.isEmailVerified)
        _loginStatus.value = currentUser != null
    }

    fun getSchedulesFromFireStoreDatabase() {
        _myScheduleState.value = CalendarScheduleUiState.initialize()
        val currentUser = calendarRepository.getCurrentUser()
        if (currentUser?.email == null) {
            _myScheduleState.value = CalendarScheduleUiState.error("로그인이 되어있지 않습니다.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val mySchedules = calendarRepository.getMySchedules(currentUser.email!!)
                cachingSchedule = mySchedules
                if (mySchedules.isEmpty()) {
                    _myScheduleState.postValue(CalendarScheduleUiState.error("일정을 추가하고 관리해보세요!"))
                }
                _myScheduleState.postValue(
                    CalendarScheduleUiState(
                        mySchedules,
                        "",
                        false
                    )
                )
            }.onFailure {
                _myScheduleState.postValue(CalendarScheduleUiState.error("오류가 발생했습니다."))
            }
        }
    }

    fun setSelectedDate(data: List<CalendarEntity>?) {
        val dateList = arrayListOf<Triple<Int, Int, Int>>()
        data?.forEach { calendarEntity ->
            for (today in (calendarEntity.startDate?.toInt()
                ?.rangeTo(calendarEntity.endDate?.toInt()!!))!!) {
                val (year, date) = today.toString().chunked(4).map { it }
                val (month, day) = date.chunked(2).map { it.toInt() }
                dateList.add(Triple(year.toInt(), month, day))
            }
        }
        _schedulesDateState.value = dateList
    }

    fun changeScheduleListForThisMonth(date: CalendarDay?) {
        val changedMonth = date?.month
        val filteredSchedule = cachingSchedule?.filter {
            it.startDate?.chunked(4)?.last()?.chunked(2)?.first() == changedMonth.toString() ||
                    it.endDate?.chunked(4)?.last()?.chunked(2)?.first() == changedMonth.toString()
        }
        _changedMonthState.value = filteredSchedule
    }
}
