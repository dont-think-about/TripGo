package com.nbcamp.tripgo.view.tour.detail.uistate

import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity

data class DetailCommonUiState(
    val detailInfo: DetailCommonEntity?,
    val message: String,
    val isLoading: Boolean
) {

    companion object {
        fun initialize(message: String) = DetailCommonUiState(
            detailInfo = null,
            message = message,
            isLoading = true
        )

        fun error(message: String) = DetailCommonUiState(
            detailInfo = null,
            message = message,
            isLoading = false
        )
    }
}
