package com.nbcamp.tripgo.view.tour.detail

sealed interface TextClickEvent {
    data class PhoneNumberClickEvent(
        val phoneNumber: String
    ) : TextClickEvent

    data class HomePageClickEvent(
        val homePage: String
    ) : TextClickEvent
}
