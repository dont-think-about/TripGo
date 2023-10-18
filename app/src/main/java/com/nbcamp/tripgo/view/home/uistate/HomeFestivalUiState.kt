package com.nbcamp.tripgo.view.home.uistate

import com.nbcamp.tripgo.data.repository.model.FestivalEntity

data class HomeFestivalUiState(
    val list: List<FestivalEntity>?,
    val isLoading: Boolean
) {

    companion object {
        fun initialize() = HomeFestivalUiState(
            list = emptyList(),
            isLoading = true
        )
    }
}
