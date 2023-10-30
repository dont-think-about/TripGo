package com.nbcamp.tripgo.view.search

import com.nbcamp.tripgo.data.repository.mapper.WeatherType
import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity

data class SearchKeywordUiState(
    val list: List<KeywordSearchEntity> = emptyList(),
    val isLoading: Boolean = true
) {
    companion object {
        fun initialize() = SearchKeywordUiState()

        fun error() = SearchKeywordUiState(
            list = listOf(
                KeywordSearchEntity(
                    contentId = "error",
                    title = "error",
                    address = "error",
                    imageUrl = "error",
                    latitude = "error",
                    longitude = "error",
                    weatherType = WeatherType.UNDEFINED, // 여기서 적절한 열거형 멤버 사용
                    temperature = "error"
                )
            ),
            isLoading = false
        )
    }
}
