package com.nbcamp.tripgo.view.home.uistate

import android.os.Parcelable
import com.nbcamp.tripgo.data.repository.model.FestivalEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeFestivalUiState(
    val list: List<FestivalEntity>?,
    val isLoading: Boolean
): Parcelable {

    companion object {
        fun initialize() = HomeFestivalUiState(
            list = emptyList(),
            isLoading = true
        )

        fun error() = HomeFestivalUiState(
            list = listOf(
                FestivalEntity(
                    contentId = "error",
                    title = "error",
                    startDate = "error",
                    endDate = "error",
                    imageUrl = "error",
                    address = "error"
                )
            ),
            isLoading = false
        )
    }
}
