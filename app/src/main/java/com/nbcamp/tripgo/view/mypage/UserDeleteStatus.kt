package com.nbcamp.tripgo.view.mypage

sealed interface UserDeleteStatus{
    object Initialize: UserDeleteStatus

    data class Success(
        val message: String
    ): UserDeleteStatus

    data class Error(
        val message: String
    ): UserDeleteStatus
}
