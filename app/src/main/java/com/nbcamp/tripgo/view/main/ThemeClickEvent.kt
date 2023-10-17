package com.nbcamp.tripgo.view.main

import com.nbcamp.tripgo.util.TourTheme

sealed interface ThemeClickEvent {

    data class RunActivity(
        val theme: TourTheme
    ) : ThemeClickEvent
}
