package com.nbcamp.tripgo.view.reviewwriting.gallery

import androidx.annotation.IdRes
import com.nbcamp.tripgo.data.repository.model.GalleryPhotoEntity

sealed interface GalleryUiState {

    object Loading : GalleryUiState

    data class Success(
        val photoList: List<GalleryPhotoEntity>,
        @IdRes val toastId: Int? = null
    ) : GalleryUiState

    data class PickPhoto(
        val photo: GalleryPhotoEntity
    ) : GalleryUiState

    data class Error(
        val message: String
    ) : GalleryUiState

}
