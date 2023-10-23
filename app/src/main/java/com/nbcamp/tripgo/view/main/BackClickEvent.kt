package com.nbcamp.tripgo.view.main

sealed interface BackClickEvent {
    data class OpenDialog(
        val isOpen: Boolean
    ) : BackClickEvent {
        companion object {
            fun initialize() = OpenDialog(
                isOpen = true
            )
        }
    }
}
