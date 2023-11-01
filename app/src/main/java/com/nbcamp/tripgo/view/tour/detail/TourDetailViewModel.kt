package com.nbcamp.tripgo.view.tour.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.kakao.sdk.user.model.Account
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity
import com.nbcamp.tripgo.util.SingleLiveEvent
import com.nbcamp.tripgo.view.calendar.CalendarRepository
import com.nbcamp.tripgo.view.calendar.uistate.CalendarLogInUiState
import com.nbcamp.tripgo.view.tour.detail.uistate.AddScheduleUiState
import com.nbcamp.tripgo.view.tour.detail.uistate.CalendarSetScheduleUiState
import com.nbcamp.tripgo.view.tour.detail.uistate.DetailCommonUiState
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.floor

class TourDetailViewModel(
    private val tourDetailRepository: TourDetailRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    // 디테일 페이지의 정보를 가져올 때 사용 되는 라이브 데이터
    private val _detailUiState: MutableLiveData<DetailCommonUiState> = MutableLiveData()
    val detailUiState: LiveData<DetailCommonUiState>
        get() = _detailUiState

    // 홈페이지, 전화번호 등 텍스트 클릭 이벤트를 처리할 라이브 데이터
    private val _textClickEvent: SingleLiveEvent<TextClickEvent> = SingleLiveEvent()
    val textClickEvent: SingleLiveEvent<TextClickEvent>
        get() = _textClickEvent

    // 일정 추가 시 캘린더 클릭 이벤트를 처리할 라이브 데이터
    private val _calendarClickEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val calendarClickEvent: SingleLiveEvent<Boolean>
        get() = _calendarClickEvent

    // 일정 추가에서 저장 버튼 이벤트를 처리할 라이브데이터
    private val _calendarSubmitClickEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val calendarSubmitClickEvent: SingleLiveEvent<Boolean>
        get() = _calendarSubmitClickEvent

    // 로그인 상태를 판단할 라이브 데이터
    private val _loginStatus: MutableLiveData<CalendarLogInUiState> = MutableLiveData()
    val loginStatus: LiveData<CalendarLogInUiState>
        get() = _loginStatus

    // 일정 추가 시 이미 있는 일정을 표시 해주기 위한 라이브 데이터
    private val _schedulesDateState: MutableLiveData<List<CalendarDay>> =
        MutableLiveData()
    val schedulesDateState: LiveData<List<CalendarDay>>
        get() = _schedulesDateState

    private val _myScheduleState: MutableLiveData<CalendarSetScheduleUiState> = MutableLiveData()

    // 사용자의 일정을 표시 해주기 위한 라이브 데이터
    val myScheduleState: LiveData<CalendarSetScheduleUiState>
        get() = _myScheduleState

    // 일정 상태 저장중 UI를 변화를 하기 위한 라이브 데이터
    private val _addScheduleState: MutableLiveData<AddScheduleUiState> = MutableLiveData()
    val addScheduleState: LiveData<AddScheduleUiState>
        get() = _addScheduleState

    // 평점 및 리뷰 개수를 가져오기 위한 라이브 데이터
    private val _countAndRating: MutableLiveData<Pair<Int, Double>> = MutableLiveData()
    val countAndRatting: LiveData<Pair<Int, Double>>
        get() = _countAndRating

    // 좋아요 버튼을 클릭했을 때 이벤트를 처리하기 위한 라이브 데이터
    private val _likeClickEvent: SingleLiveEvent<String> = SingleLiveEvent()
    val likeClickEvent: SingleLiveEvent<String>
        get() = _likeClickEvent

    // 좋아요 상태를 표시하는 라이브 데이터
    private val _likeStatus: MutableLiveData<Boolean> = MutableLiveData()
    val likeStatus: LiveData<Boolean>
        get() = _likeStatus

    private val scheduleDates = arrayListOf<CalendarDay>()

    fun runSearchDetailInformation(contentId: String?) {
        _detailUiState.value = DetailCommonUiState.initialize("로딩 중..")
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                // 상세 정보 가져오기
                val response = tourDetailRepository.getDetailInformation(contentId)
                // 평점 및 리뷰 개수 가져오기
                getAverageRatingThisPlace(contentId)
                // 이 컨텐츠의 현재 사용자 좋아요 상태 가져오기
                getLikedStatusThisContent(contentId)
                _detailUiState.postValue(DetailCommonUiState(response, "정보 로딩 완료\n경로 로딩 시작", false))
            }.onFailure {
                _detailUiState.postValue(DetailCommonUiState.error("정보를 가져 오는데 실패했습니다."))
            }
        }
    }

    fun getMySchedules(currentUser: Any) {
        // 이전에 지정한 일정 우선 제거
        scheduleDates.clear()
        _myScheduleState.value = CalendarSetScheduleUiState.initialize()
        when (currentUser) {
            is FirebaseUser -> {
                if (currentUser.email == null) {
                    _myScheduleState.value = CalendarSetScheduleUiState.error("로그인이 되어있지 않습니다.")
                    return
                }
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        // 달력에 보여줄 정보
                        val myAllSchedules =
                            calendarRepository.getMySchedules(currentUser.email!!)
                        setSelectedDate(myAllSchedules)
                    }.onFailure {
                        _myScheduleState.postValue(CalendarSetScheduleUiState.error("오류가 발생했습니다."))
                    }
                }
            }

            is Account -> {
                if (currentUser.email == null) {
                    _myScheduleState.value = CalendarSetScheduleUiState.error("로그인이 되어있지 않습니다.")
                    return
                }
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        val myAllSchedules =
                            calendarRepository.getMySchedules(currentUser.email!!)
                        setSelectedDate(myAllSchedules)
                    }.onFailure {
                        _myScheduleState.postValue(CalendarSetScheduleUiState.error("오류가 발생했습니다."))
                    }
                }
            }
        }
    }

    // startDate ~ endDate 사이의 날짜를 달력을 표시 하기 위해 날짜 데이터를 만드는 함수
    private fun setSelectedDate(
        data: List<CalendarEntity>?
    ) {
        val dateList = arrayListOf<Triple<Int, Int, Int>>()
        data?.forEach { calendarEntity ->
            for (today in (calendarEntity.startDate?.toInt()?.rangeTo(calendarEntity.endDate?.toInt()!!))!!) {
                val (year, date) = today.toString().chunked(4).map { it }
                val (month, day) = date.chunked(2).map { it.toInt() }
                dateList.add(Triple(year.toInt(), month, day))
            }
        }
        val selectedDateList = dateList.map { date ->
            CalendarDay.from(
                date.first,
                date.second,
                date.third
            )
        }
        _myScheduleState.postValue(CalendarSetScheduleUiState(emptyList(), null, false))
        _schedulesDateState.postValue(selectedDateList)
    }

    fun makeCall() {
        val phoneNumber = detailUiState.value?.detailInfo?.telPhoneNumber
        if (phoneNumber != null)
            _textClickEvent.value = TextClickEvent.PhoneNumberClickEvent(phoneNumber)
    }

    fun moveToHomePage() {
        val homePage = detailUiState.value?.detailInfo?.homePage
        if (homePage != null)
            _textClickEvent.value = TextClickEvent.HomePageClickEvent(homePage)
    }

    fun getLoginStatus() {
        val currentUser = calendarRepository.getCurrentUser()
        when (currentUser) {
            is FirebaseUser -> {
                _loginStatus.value = CalendarLogInUiState(currentUser, true)
            }

            is Account -> {
                _loginStatus.value = CalendarLogInUiState(currentUser, true)
            }

            null -> {
                _loginStatus.value = CalendarLogInUiState(null, false)
            }
        }
    }

    fun selectScheduleRange(dates: List<CalendarDay>, selectedDayList: List<CalendarDay>) {
        if (CalendarDay.today().isAfter(dates.first())) {
            // 선택한 범위가 오늘 보다 전이면, 선택을 못 하도록 막음
            scheduleDates.clear()
            _calendarClickEvent.value = false
            return
        }
        if (dates.intersect(selectedDayList.toSet()).isNotEmpty()) {
            // 겹치는 부분이 있으면 이전 저장 되어 있던 것 제거
            // 제거 안하면 확인 클릭 했을 때 isEmpty 를 통과 하여 이상 현상 발생
            scheduleDates.clear()
            _calendarClickEvent.value = true
            return
        }

        scheduleDates.clear()
        scheduleDates.addAll(dates)
    }

    fun saveMySchedule(
        festivalItem: FestivalItem?,
        keywordItem: KeywordItem?,
        detailInfo: DetailCommonEntity
    ) {
        if (scheduleDates.isEmpty()) {
            _calendarSubmitClickEvent.value = false
            return
        }
        _addScheduleState.value = AddScheduleUiState.initialize()
        // 시작, 끝 날짜 구하기
        val (startDate, endDate) = convertDate(scheduleDates)

        // 사용자 정보 구하기
        val email = if (loginStatus.value?.user is FirebaseUser) {
            (loginStatus.value?.user as FirebaseUser).email
        } else {
            (loginStatus.value?.user as Account).email
        }

        // 저장 시작
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                tourDetailRepository.setMySchedule(
                    festivalItem,
                    keywordItem,
                    detailInfo,
                    startDate,
                    endDate,
                    email
                )
                // 저장 성공 상태로 업데이트
                _addScheduleState.postValue(AddScheduleUiState("저장 성공", false))
                // 캘린더 다이얼로그를 지우기 위한 이벤트
                _calendarSubmitClickEvent.postValue(true)
            }.onFailure {
                _addScheduleState.postValue(AddScheduleUiState.error("저장 중 오류가 발생하였습니다."))
            }
        }
    }

    private fun convertDate(scheduleDates: ArrayList<CalendarDay>): Pair<String, String> {
        val startDate = scheduleDates.first()
        val endDate = scheduleDates.last()
        val (startYear, startMonth, startDay) = listOf(
            startDate.year,
            startDate.month,
            startDate.day
        )
        val (endYear, endMonth, endDay) = listOf(endDate.year, endDate.month, endDate.day)
        val startMonthWithZero = if (startMonth < 10) "0$startMonth" else "$startMonth"
        val endMonthWithZero = if (endMonth < 10) "0$endMonth" else "$endMonth"
        val startDayWithZero = if (startDay < 10) "0$startDay" else "$startDay"
        val endDayWithZero = if (endDay < 10) "0$endDay" else "$endDay"

        return "$startYear$startMonthWithZero$startDayWithZero" to "$endYear$endMonthWithZero$endDayWithZero"
    }

    fun setUserOption() {
        val currentUser = loginStatus.value?.user
        when {
            currentUser == null -> {
                _loginStatus.value = CalendarLogInUiState(null, false)
            }
//            (currentUser as FirebaseUser).isEmailVerified.not() || (currentUser as Account).isEmailVerified!!.not() -> {
// //                toast("이메일 인증이 되어 있지 않아 일정을 추가할 수 없습니다.")
//            // 테스트 할 떄는 주석 풀고, 이메일 인증 기능이 완성 되면 주석 처리
// //                tourDetailViewModel.getMySchedules(currentUser)
// //                runCalendarDialog()
//            }

            else -> {
                currentUser.let {
                    _loginStatus.value = CalendarLogInUiState(currentUser, true)
                }
            }
        }
    }

    private fun getAverageRatingThisPlace(contentId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = tourDetailRepository.getAverageRatingThisPlace(contentId)
            val filteredList = response.filterNot { it == -1f }
            // 적힌 리뷰의 숫자 및 평점
            val reviewCount = filteredList.count()
            val averageRating = filteredList.sum()
            val calculatedRating = if(reviewCount == 1) {
                averageRating.toDouble()
            } else {
                floor(((averageRating / reviewCount) * 10.0) / 10.0)
            }
            _countAndRating.postValue(
                reviewCount to calculatedRating
            )
        }
    }

    private fun getLikedStatusThisContent(contentId: String?) {
        if (contentId == null) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val response = tourDetailRepository.getLikedStatusThisContent(contentId)
            _likeStatus.postValue(response)
        }
    }

    fun saveLikePlace(
        detailInfo: DetailCommonEntity,
        contentId: String?,
        currentUser: Any
    ) {
        if (contentId == null) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                tourDetailRepository.saveLikePlace(
                    detailInfo,
                    contentId,
                    currentUser
                )
                _likeClickEvent.postValue(" 저장 성공")
            }.onFailure {
                _likeClickEvent.postValue(" 저장 실패")
            }
        }
    }

    fun removeLikePlace(contentId: String?, currentUser: Any?) {
        if (contentId == null) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                tourDetailRepository.removeLikePlace(
                    contentId,
                    currentUser
                )
                _likeClickEvent.postValue(" 삭제 성공")
            }.onFailure {
                _likeClickEvent.postValue(" 삭제 실패")
            }
        }
    }
}
