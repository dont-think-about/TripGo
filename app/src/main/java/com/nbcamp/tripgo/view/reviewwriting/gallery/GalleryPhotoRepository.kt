package com.nbcamp.tripgo.view.reviewwriting.gallery

import com.nbcamp.tripgo.data.repository.model.GalleryPhotoEntity

interface GalleryPhotoRepository {
    suspend fun getAllPhotos(): MutableList<GalleryPhotoEntity>
}
