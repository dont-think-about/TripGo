package com.nbcamp.tripgo.view.review.detail

data class UserStatus(
    val email: String? = "",
    val nickname: String? = "",
    val profileImage: String? = "",
    var reviewCount: Int = 0,
)
