package com.nbcamp.tripgo.view.home.uistate

import com.nbcamp.tripgo.view.home.valuetype.ProvincePlaceEntity

data class HomeProvincePlaceUiState(
    val list: List<ProvincePlaceEntity>?,
    val isLoading: Boolean
) {

    companion object {
        fun initialize() = HomeProvincePlaceUiState(
            list = emptyList(),
            isLoading = true
        )
    }
}
