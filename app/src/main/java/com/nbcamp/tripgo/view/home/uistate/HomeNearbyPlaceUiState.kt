package com.nbcamp.tripgo.view.home.uistate

import com.nbcamp.tripgo.data.repository.model.NearbyPlaceEntity

data class HomeNearbyPlaceUiState(
    val list: List<NearbyPlaceEntity>?,
    val isLoading: Boolean
) {

    companion object {
        fun initialize() = HomeNearbyPlaceUiState(
            list = emptyList(),
            isLoading = true
        )

        fun error() = HomeNearbyPlaceUiState(
            list = listOf(
                NearbyPlaceEntity(
                    contentId = "error",
                    title = "error",
                    longitude = "error",
                    latitude = "error",
                    distance = "error",
                    imageUrl = "error"
                )
            ),
            isLoading = false
        )
    }
}
