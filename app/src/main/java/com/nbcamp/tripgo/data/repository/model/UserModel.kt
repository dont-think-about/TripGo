package com.nbcamp.tripgo.data.repository.model

data class UserModel(
    val email: String? = "",
    val nickname: String? = "",
    val profileImage: String? = "",
    var reviewCount: Int = 0,
)
