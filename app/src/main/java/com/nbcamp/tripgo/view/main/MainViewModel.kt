package com.nbcamp.tripgo.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.user.UserApiClient
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.data.repository.model.FestivalEntity
import com.nbcamp.tripgo.util.SingleLiveEvent
import com.nbcamp.tripgo.view.calendar.WritingType
import com.nbcamp.tripgo.view.calendar.uistate.CalendarScheduleUiState
import com.nbcamp.tripgo.view.home.valuetype.ProvincePlaceEntity
import com.nbcamp.tripgo.view.home.valuetype.TourTheme
import com.nbcamp.tripgo.view.reviewwriting.CalendarUserModel
import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _event: SingleLiveEvent<ThemeClickEvent> = SingleLiveEvent()
    val event: LiveData<ThemeClickEvent> get() = _event

    private val _eventBackClick: SingleLiveEvent<Unit?> = SingleLiveEvent()
    val eventBackClick: SingleLiveEvent<Unit?> get() = _eventBackClick

    private val _eventPermission: SingleLiveEvent<PermissionEvent> = SingleLiveEvent()
    val eventPermission: SingleLiveEvent<PermissionEvent> get() = _eventPermission

    private val _eventSetLocation: SingleLiveEvent<Unit?> = SingleLiveEvent()
    val eventSetLocation: SingleLiveEvent<Unit?> get() = _eventSetLocation

    private val _eventRunGallery: SingleLiveEvent<Unit?> = SingleLiveEvent()
    val eventRunGallery: SingleLiveEvent<Unit?> get() = _eventRunGallery

    private val _eventSetUser: MutableLiveData<SetUserEvent> = MutableLiveData()
    val eventSetUser: LiveData<SetUserEvent>
        get() = _eventSetUser

    // 현재 페이지를 바라볼 livedata
    private val _currentPageType: MutableLiveData<FragmentPageType> =
        MutableLiveData(FragmentPageType.PAGE_HOME)
    val currentPageType: LiveData<FragmentPageType>
        get() = _currentPageType

    // 캘린더에서 리뷰 작성으로 넘어갈 때 보내줄 livedata
    private val _calendarToReviewModel: MutableLiveData<CalendarUserModel> = MutableLiveData()
    val calendarToReviewModel: LiveData<CalendarUserModel>
        get() = _calendarToReviewModel

    // 일정 수정 시 수정 이벤트를 처리할 라이브 데이터
    private val _buttonClickModifyState: MutableLiveData<CalendarScheduleUiState> =
        MutableLiveData()
    val buttonClickModifyState: MutableLiveData<CalendarScheduleUiState>
        get() = _buttonClickModifyState

    // 리뷰 목록 아이템에서 리뷰 상세 페이지로 넘겨줄 라이브 데이터
    private val _reviewDetailModel: MutableLiveData<ReviewWritingModel> = MutableLiveData()
    val reviewDetailModel: LiveData<ReviewWritingModel>
        get() = _reviewDetailModel

    // 메인 화면 로딩 시간을 줄여주기 위해 스플래시에서 받아온 데이터
    private val _dataFromSplash: MutableLiveData<Pair<List<FestivalEntity>, List<ProvincePlaceEntity>>> = MutableLiveData()
    val dataFromSplash: MutableLiveData<Pair<List<FestivalEntity>, List<ProvincePlaceEntity>>>
        get() = _dataFromSplash

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

    fun setUserState() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = firebaseAuth.currentUser
        _eventSetUser.value = SetUserEvent.Loading("회원 정보 로딩 증..")
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                UserApiClient.instance.me { user, _ ->
                    user?.let {
                        _eventSetUser.postValue(
                            SetUserEvent.Success(
                                user.kakaoAccount,
                                "회원 정보 로딩 완료"
                            )
                        )
                    }
                }

                _eventSetUser.postValue(
                    SetUserEvent.Success(
                        firebaseUser,
                        "회원 정보 로딩 완료"
                    )
                )
            }.onFailure {
                _eventSetUser.postValue(SetUserEvent.Error("회원 정보 로딩 실패.."))
            }
        }
    }

    fun updateModifiedCalendarUi(state: CalendarScheduleUiState) {
        _buttonClickModifyState.value = state
    }

    fun setReviewDetailModel(model: ReviewWritingModel) {
        _reviewDetailModel.value = model
    }

    fun sendSplashData(
        festivalList: List<FestivalEntity>?,
        provinceList: List<ProvincePlaceEntity>?
    ) {
        if (festivalList == null || provinceList == null) {
            return
        }
        _dataFromSplash.value = festivalList to provinceList
    }
}
