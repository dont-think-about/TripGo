package com.nbcamp.tripgo.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.nbcamp.tripgo.util.SingleLiveEvent
import com.nbcamp.tripgo.util.TourTheme

class MainViewModel : ViewModel() {

    private val _event: SingleLiveEvent<ThemeClickEvent> = SingleLiveEvent()
    val event: LiveData<ThemeClickEvent> get() = _event
    fun runThemeTourActivity(themeId: TourTheme) {
        _event.value = ThemeClickEvent.RunActivity(themeId)
    }
}
