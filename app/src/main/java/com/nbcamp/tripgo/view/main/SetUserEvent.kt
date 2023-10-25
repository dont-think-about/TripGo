package com.nbcamp.tripgo.view.main

sealed interface SetUserEvent {
    data class Loading(
        val message: String
    ) : SetUserEvent

    data class Success(
        val currentUser: Any?,
        val message: String
    ) : SetUserEvent

    data class Error(
        val message: String
    ) : SetUserEvent
}

