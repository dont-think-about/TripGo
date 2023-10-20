package com.nbcamp.tripgo.view.calendar.uistate

import com.nbcamp.tripgo.data.repository.model.CalendarEntity

data class RunDialogUiState(
    val data: CalendarEntity?,
    val isValidRange: Boolean
) {

    companion object {
        fun error() = RunDialogUiState(
            data = null,
            isValidRange = false
        )
    }
}
