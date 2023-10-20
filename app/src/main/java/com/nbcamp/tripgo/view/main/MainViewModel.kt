package com.nbcamp.tripgo.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.util.SingleLiveEvent
import com.nbcamp.tripgo.view.home.valuetype.ProvincePlaceEntity
import com.nbcamp.tripgo.view.home.valuetype.TourTheme

class MainViewModel : ViewModel() {

    private val _event: SingleLiveEvent<ThemeClickEvent> = SingleLiveEvent()
    val event: LiveData<ThemeClickEvent> get() = _event

    // 현재 페이지를 바라볼 livedata
    private val _currentPageType: MutableLiveData<FragmentPageType> =
        MutableLiveData(FragmentPageType.PAGE_HOME)
    val currentPageType: LiveData<FragmentPageType>
        get() = _currentPageType

    fun runThemeTourActivity(themeId: TourTheme) {
        _event.value = ThemeClickEvent.RunTourThemeActivity(themeId)
    }

    fun runTourDetailActivity(contentId: String) {
        _event.value = ThemeClickEvent.RunTourDetailActivity(contentId)
    }

    fun runAttractionActivity(model: ProvincePlaceEntity) {
        _event.value = ThemeClickEvent.RunAttractionActivity(model)
    }

    fun setCurrentPage(menuItemId: Int): Boolean {
        val pageType = getPageType(menuItemId)
        changeCurrentPage(pageType)
        return true
    }

    private fun getPageType(menuItemId: Int): FragmentPageType {
        return when (menuItemId) {
            R.id.home -> FragmentPageType.PAGE_HOME
            R.id.calendar -> FragmentPageType.PAGE_CALENDAR
            R.id.review -> FragmentPageType.PAGE_REVIEW
            R.id.my_page -> FragmentPageType.PAGE_MY
            else -> throw IllegalArgumentException("invalid page type")
        }
    }

    private fun changeCurrentPage(pageType: FragmentPageType) {
        if (currentPageType.value == pageType)
            return
        _currentPageType.value = pageType
    }
}
