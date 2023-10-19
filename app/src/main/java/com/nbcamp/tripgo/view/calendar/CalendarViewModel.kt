package com.nbcamp.tripgo.view.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.util.APIResponse
import com.nbcamp.tripgo.view.calendar.uistate.CalendarScheduleUiState
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

    fun getLoginStatus() {
        val currentUser = calendarRepository.getCurrentUser()
        println(currentUser?.email)
        _loginStatus.value = currentUser != null
    }

    fun getSchedulesFromFireStoreDatabase() {
        _myScheduleState.value = CalendarScheduleUiState.initialize()
        val currentUser = calendarRepository.getCurrentUser()
        viewModelScope.launch(Dispatchers.IO) {
            val mySchedules = calendarRepository.getMySchedules(currentUser?.email)
            when (mySchedules) {
                is APIResponse.Error -> CalendarScheduleUiState.error(mySchedules.message)

                is APIResponse.Success -> {
                    _myScheduleState.postValue(
                        CalendarScheduleUiState(
                            mySchedules.data,
                            "",
                            false
                        )
                    )
                }
            }
        }
    }
}
