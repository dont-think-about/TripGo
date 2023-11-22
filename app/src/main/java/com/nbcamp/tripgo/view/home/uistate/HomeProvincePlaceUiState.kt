package com.nbcamp.tripgo.view.home.uistate

import android.os.Parcelable
import com.nbcamp.tripgo.view.home.valuetype.ProvincePlaceEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeProvincePlaceUiState(
    val list: List<ProvincePlaceEntity>?,
    val isLoading: Boolean
): Parcelable {

    companion object {
        fun initialize() = HomeProvincePlaceUiState(
            list = emptyList(),
            isLoading = true
        )
    }
}
