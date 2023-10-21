package com.nbcamp.tripgo.view.calendar.uistate

data class CalendarLogInUiState(
    val user: Any?,
    val isLoggedIn: Boolean
) {

    companion object {
        fun initialize() = CalendarLogInUiState(
            user = null,
            isLoggedIn = false
        )
    }
}
