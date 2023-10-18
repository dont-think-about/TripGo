package com.nbcamp.tripgo.view.home.uistate

import com.nbcamp.tripgo.data.repository.mapper.WeatherType
import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity

data class HomeWeatherUiState(
    val data: KeywordSearchEntity?,
    val isLoading: Boolean
) {

    companion object {
        fun initialize() = HomeWeatherUiState(
            data = KeywordSearchEntity(
                contentId = "",
                title = "",
                address = "",
                imageUrl = "",
                latitude = "",
                longitude = "",
                weatherType = WeatherType.UNDEFINED,
                temperature = ""
            ),
            isLoading = true
        )

        fun error() = HomeWeatherUiState(
            data = KeywordSearchEntity(
                contentId = "error",
                title = "error",
                address = "error",
                imageUrl = "error",
                latitude = "error",
                longitude = "error",
                weatherType = WeatherType.UNDEFINED,
                temperature = "error"
            ),
            isLoading = false
        )
    }
}
