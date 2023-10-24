package com.nbcamp.tripgo.view.tour.detail.uistate

import com.nbcamp.tripgo.data.repository.model.CalendarEntity

data class CalendarSetScheduleUiState(
    val allSchedules: List<CalendarEntity>?,
    val message: String?,
    val isLoading: Boolean
) {

    companion object {
        fun initialize() = CalendarSetScheduleUiState(
            allSchedules = emptyList(),
            message = null,
            isLoading = true
        )

        fun error(message: String?) = CalendarSetScheduleUiState(
            allSchedules = emptyList(),
            message = message,
            isLoading = false
        )
    }
}
