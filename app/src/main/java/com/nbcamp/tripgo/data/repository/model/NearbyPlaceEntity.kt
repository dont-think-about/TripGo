package com.nbcamp.tripgo.data.repository.model

data class NearbyPlaceEntity(
    val contentId: String,
    val latitude: String,
    val longitude: String,
    val title: String,
    val imageUrl: String,
    var distance: String = ""
)
