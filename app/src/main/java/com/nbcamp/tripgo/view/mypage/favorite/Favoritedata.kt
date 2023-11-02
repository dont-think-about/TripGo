package com.nbcamp.tripgo.view.mypage.favorite

data class FavoriteItem(
    val title: String,
    val imageUrl: String,
    val shortDescription: String,
    val address: String,
    var isSelected: Boolean = false,
    val collectionPath: String
)
