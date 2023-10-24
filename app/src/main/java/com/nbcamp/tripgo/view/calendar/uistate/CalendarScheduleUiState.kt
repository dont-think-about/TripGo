package com.nbcamp.tripgo.view.calendar.uistate

import com.nbcamp.tripgo.data.repository.model.CalendarEntity

data class CalendarScheduleUiState(
    val allSchedules: List<CalendarEntity>?,
    val monthSchedules: List<CalendarEntity>?,
    val message: String?,
    val isLoading: Boolean
) {

    companion object {
        fun initialize() = CalendarScheduleUiState(
            allSchedules = emptyList(),
            monthSchedules = emptyList(),
            message = null,
            isLoading = true
        )

        fun error(message: String?) = CalendarScheduleUiState(
            allSchedules = emptyList(),
            monthSchedules = emptyList(),
            message = message,
            isLoading = false
        )
    }
}
