package com.nbcamp.tripgo.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.nbcamp.tripgo.util.SingleLiveEvent
import com.nbcamp.tripgo.view.home.valuetype.TourTheme

class MainViewModel : ViewModel() {

    private val _event: SingleLiveEvent<ThemeClickEvent> = SingleLiveEvent()
    val event: LiveData<ThemeClickEvent> get() = _event
    fun runThemeTourActivity(themeId: TourTheme) {
        _event.value = ThemeClickEvent.RunTourThemeActivity(themeId)
    }

    fun runTourDetailActivity(contentId: String) {
        _event.value = ThemeClickEvent.RunTourDetailActivity(contentId)
    }
}
