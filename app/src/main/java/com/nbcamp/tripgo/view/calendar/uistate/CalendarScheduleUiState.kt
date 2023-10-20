package com.nbcamp.tripgo.view.calendar.uistate

import com.nbcamp.tripgo.data.repository.model.CalendarEntity

data class CalendarScheduleUiState(
    val data: List<CalendarEntity>?,
    val message: String?,
    val isLoading: Boolean
) {

    companion object {
        fun initialize() = CalendarScheduleUiState(
            data = emptyList(),
            message = null,
            isLoading = true
        )

        fun error(message: String?) = CalendarScheduleUiState(
            data = emptyList(),
            message = message,
            isLoading = false
        )
    }
}
