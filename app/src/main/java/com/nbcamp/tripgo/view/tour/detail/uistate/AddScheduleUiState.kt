package com.nbcamp.tripgo.view.tour.detail.uistate

data class AddScheduleUiState(
    val message: String,
    val isLoading: Boolean
) {

    companion object {
        fun initialize() = AddScheduleUiState(
            message = "저장 시작",
            isLoading = true
        )

        fun error(message: String) = AddScheduleUiState(
            message = message,
            isLoading = false
        )
    }
}
