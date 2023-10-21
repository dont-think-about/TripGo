package com.nbcamp.tripgo.view.calendar.uistate

import com.nbcamp.tripgo.data.repository.model.CalendarEntity

data class RunDialogUiState(
    val data: CalendarEntity?,
    val message: String?,
    val isValidRange: Boolean
) {

    companion object {
        fun error() = RunDialogUiState(
            data = null,
            message = null,
            isValidRange = false
        )

        fun notOpenDialog() = RunDialogUiState(
            data = null,
            message = NOT_OPEN,
            isValidRange = false
        )

        const val NOT_OPEN = "not_open"
    }
}
