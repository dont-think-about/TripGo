package com.nbcamp.tripgo.view.main

import com.nbcamp.tripgo.view.home.valuetype.ProvincePlaceEntity
import com.nbcamp.tripgo.view.home.valuetype.TourTheme

sealed interface ThemeClickEvent {

    data class RunTourThemeActivity(
        val theme: TourTheme
    ) : ThemeClickEvent

    data class RunTourDetailActivity(
        val contentId: String
    ) : ThemeClickEvent

    data class RunAttractionActivity(
        val model: ProvincePlaceEntity
    ) : ThemeClickEvent

    object RunLogInActivity : ThemeClickEvent
}
