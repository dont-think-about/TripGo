package com.nbcamp.tripgo.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.util.SingleLiveEvent
import com.nbcamp.tripgo.view.calendar.WritingType
import com.nbcamp.tripgo.view.home.valuetype.ProvincePlaceEntity
import com.nbcamp.tripgo.view.home.valuetype.TourTheme
import com.nbcamp.tripgo.view.reviewwriting.CalendarUserModel

class MainViewModel : ViewModel() {

    private val _event: SingleLiveEvent<ThemeClickEvent> = SingleLiveEvent()
    val event: LiveData<ThemeClickEvent> get() = _event

    private val _eventBackClick: SingleLiveEvent<Any?> = SingleLiveEvent()
    val eventBackClick: SingleLiveEvent<Any?> get() = _eventBackClick

    private val _eventPermission: SingleLiveEvent<PermissionEvent> = SingleLiveEvent()
    val eventPermission: SingleLiveEvent<PermissionEvent> get() = _eventPermission

    private val _eventSetLocation: SingleLiveEvent<Any?> = SingleLiveEvent()
    val eventSetLocation: SingleLiveEvent<Any?> get() = _eventSetLocation

    private val _eventRunGallery: SingleLiveEvent<Any?> = SingleLiveEvent()
    val eventRunGallery: SingleLiveEvent<Any?> get() = _eventRunGallery

    // 현재 페이지를 바라볼 livedata
    private val _currentPageType: MutableLiveData<FragmentPageType> =
        MutableLiveData(FragmentPageType.PAGE_HOME)
    val currentPageType: LiveData<FragmentPageType>
        get() = _currentPageType

    // 캘린더에서 리뷰 작성으로 넘어갈 때 보내줄 livedata
    private val _calendarToReviewModel: MutableLiveData<CalendarUserModel> = MutableLiveData()
    val calendarToReviewModel: LiveData<CalendarUserModel>
        get() = _calendarToReviewModel

    fun runThemeTourActivity(themeId: TourTheme) {
        _event.value = ThemeClickEvent.RunTourThemeActivity(themeId)
    }

    fun runTourDetailActivity(contentId: String) {
        _event.value = ThemeClickEvent.RunTourDetailActivity(contentId)
    }

    fun runAttractionActivity(model: ProvincePlaceEntity) {
        _event.value = ThemeClickEvent.RunAttractionActivity(model)
    }

    fun runLoginActivity() {
        _event.value = ThemeClickEvent.RunLogInActivity
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

    fun setBasicReviewModel(
        model: CalendarEntity,
        currentUser: Any?,
        writingType: WritingType
    ) {
        _calendarToReviewModel.value = CalendarUserModel(
            model,
            currentUser,
            writingType
        )
    }

    fun onClickBackButton() {
        _eventBackClick.call()
    }

    fun getGalleryPermissionEvent(permission: String) {
        _eventPermission.value = PermissionEvent.GetGalleryPermission(permission)
    }

    fun getLocationPermissionEvent(permission: String) {
        _eventPermission.value = PermissionEvent.GetLocationPermission(permission)
    }

    fun setLocationEvent() {
        _eventSetLocation.call()
    }

    fun runGalleryEvent() {
        _eventRunGallery.call()
    }
}
