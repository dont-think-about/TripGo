package com.nbcamp.tripgo.view.tour.detail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.kakao.sdk.user.model.Account
import com.nbcamp.tripgo.BuildConfig
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity
import com.nbcamp.tripgo.databinding.ActivityTourDetailBinding
import com.nbcamp.tripgo.databinding.DialogCalendarBinding
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.util.TMapFrameLayout
import com.nbcamp.tripgo.util.calendar.CantSetDayDecorator
import com.nbcamp.tripgo.util.calendar.OutDateMonthDecorator
import com.nbcamp.tripgo.util.calendar.SaturdayDecorator
import com.nbcamp.tripgo.util.calendar.SundayDecorator
import com.nbcamp.tripgo.util.calendar.TodayDecorator
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.login.LogInActivity
import com.nbcamp.tripgo.view.main.MainActivity
import com.nbcamp.tripgo.view.tour.detail.uistate.DetailCommonUiState
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapMarkerItem
import java.util.Calendar

class TourDetailActivity : AppCompatActivity() {
    private var festivalItem: FestivalItem? = null
    private var keywordItem: KeywordItem? = null
    private var nearbyContentId: String? = null
    private lateinit var binding: ActivityTourDetailBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var selectedDayList: List<CalendarDay>
    private lateinit var dialog: AlertDialog
    private lateinit var detailInfo: DetailCommonEntity
    private lateinit var container: TMapFrameLayout
    private lateinit var tMapView: TMapView
    private var calendarBinding: DialogCalendarBinding? = null
    private var currentUser: Any? = null
    private var isEmailVerified = false


    private val tourDetailViewModel: TourDetailViewModel by viewModels {
        TourDetailViewModelFactory(
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTourDetailBinding.inflate(layoutInflater)
        loadingDialog = LoadingDialog(this)
        container = binding.routeMap
        setContentView(binding.root)

        initVariables()
        initViews()
        initViewModel()
    }

    private fun initVariables() {
        loadingDialog = LoadingDialog(this)
        festivalItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("festivalItem", FestivalItem::class.java)
        } else {
            intent?.getParcelableExtra("festivalItem")
        }

        keywordItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("keywordItem", KeywordItem::class.java)
        } else {
            intent?.getParcelableExtra("keywordItem")
        }
        nearbyContentId = intent?.getStringExtra("contentId")

    }

    private fun initViews() = with(binding) {
        val contentId = if (festivalItem?.contentid != null) {
            festivalItem?.contentid
        } else if (keywordItem?.contentid != null) {
            keywordItem?.contentid
        } else {
            nearbyContentId
        }
        btnBack.setOnClickListener {
            finish()
        }
        phoneNumber.setOnClickListener {
            tourDetailViewModel.makeCall()
        }
        moveToHomepage.setOnClickListener {
            tourDetailViewModel.moveToHomePage()
        }
        btnHeart.setOnClickListener {
            // 좋아요 저장 및 삭제
            if (currentUser != null) {
                // 좋아요 버튼 상태 조작
                it.isSelected = it.isSelected.not()
                if (it.isSelected) {
                    tourDetailViewModel.saveLikePlace(detailInfo, contentId, currentUser!!)
                    return@setOnClickListener
                }
                tourDetailViewModel.removeLikePlace(contentId, currentUser)
            } else {
                toast(getString(R.string.not_login_then_not_submit_like))
            }

        }
        // 일정 추가 캘린더 다이얼로그 실행
        moveToCalendar.setOnClickListener {
            tourDetailViewModel.setUserOption()
            if (currentUser != null && isEmailVerified) {
                // 영상 용 조건문
//            if (currentUser != null) {
                tourDetailViewModel.getMySchedules(currentUser!!)
                runCalendarDialog()
            } else if (isEmailVerified.not()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.not_email_verified_then_cant_add_schedule),
                    2000
                ).show()
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.not_login_so_dont_add_schedule),
                    5000
                ).setAction(getString(R.string.login_en)) {
                    startActivity(Intent(this@TourDetailActivity, LogInActivity::class.java))
                }.show()
            }
        }
        // 상단 홈 버튼 클릭 시 메인으로 이동
        btnHome.setOnClickListener {
            startActivity(Intent(this@TourDetailActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }
        // 공유 버튼
        btnShare.setOnClickListener {
            sharingPlace()
        }

        // 지도 위에서 스크롤 뷰의 이벤트를 막기 위한 함수
        doNotFrameScroll()
        // 상세 정보 가져오기
        runSearchDetailInformation(contentId)
    }

    private fun runSearchDetailInformation(contentId: String?) {
        // 관광지 상세 정보 및 리뷰에서 평점 가져오기
        tourDetailViewModel.runSearchDetailInformation(contentId)
    }

    private fun initViewModel() = with(tourDetailViewModel) {
        getLoginStatus()
        detailUiState.observe(this@TourDetailActivity) { state ->
            loadingDialog.setText(state.message)
            if (state == DetailCommonUiState.error(state.message)) {
                loadingDialog.setInvisible()
                toast(state.message)
                finish()
                return@observe
            }
            if (state.isLoading) {
                loadingDialog.setVisible()
            }
            state.detailInfo?.let { info ->
                bindingInfo(info)
                detailInfo = info
                setTMap(info)
            }
        }

        textClickEvent.observe(this@TourDetailActivity) { event ->
            when (event) {
                is TextClickEvent.HomePageClickEvent -> {
                    if (event.homePage == getString(R.string.no_detail_info)) {
                        return@observe
                    }
                    val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                        putExtra("query", event.homePage)
                    }
                    startActivity(intent)
                }

                is TextClickEvent.PhoneNumberClickEvent -> {
                    if (event.phoneNumber == getString(R.string.no_detail_info)) {
                        return@observe
                    }
                    makePhoneCall(event.phoneNumber)
                }
            }
        }

        loginStatus.observe(this@TourDetailActivity) { state ->
            // 유저 정보 확인
            currentUser = state.user
            // 이메일 인증 확인
            isEmailVerified = if (state.user is FirebaseUser) {
                state.user.isEmailVerified
            } else {
                (state.user as Account).isEmailVerified ?: false
            }
        }

        schedulesDateState.observe(this@TourDetailActivity) { dateList ->
            selectedDayList = dateList
            calendarBinding?.addScheduleCalendarView?.addDecorator(
                CantSetDayDecorator(this@TourDetailActivity, dateList)
            )
        }

        myScheduleState.observe(this@TourDetailActivity) { state ->
            state.message?.let { toast(it) }
            calendarBinding?.calendarProgressBar?.isVisible = state.isLoading
        }

        calendarClickEvent.observe(this@TourDetailActivity) {
            when (it) {
                true -> toast(getString(R.string.cant_select_duplicate_schedule))
                false -> toast(getString(R.string.not_register_schedule_before_today))
            }
            calendarBinding?.addScheduleCalendarView?.clearSelection()
        }

        calendarSubmitClickEvent.observe(this@TourDetailActivity) {
            if (!it) {
                toast(getString(R.string.please_select_schedule))
                return@observe
            }
            dialog.dismiss()
            calendarBinding = null
        }

        // 일정 저장 중 상태에 따른 Ui 업데이트
        addScheduleState.observe(this@TourDetailActivity) { state ->
            if (state.isLoading) {
                loadingDialog.run {
                    setVisible()
                    setText(state.message)
                }
            } else {
                toast(getString(R.string.done_save_schedule))
                loadingDialog.setInvisible()
            }
        }

        countAndRatting.observe(this@TourDetailActivity) { numSet ->
            "${if (numSet.second.isNaN()) 0.0 else numSet.second}점".also {
                binding.evaluation.text = it
            }
            "${numSet.first}개의 리뷰".also { binding.tourReview.text = it }
        }

        routeMap.observe(this@TourDetailActivity) { polyLine ->
            with(binding) {
                when (polyLine) {
                    null -> {
                        "${getString(R.string.find_road)} ${getString(R.string.now_loading)}".also {
                            noticeRoute.text = it
                        }
                    }

                    else -> {
                        tMapView.run {
                            addTMapPolyLine(polyLine)
                            val info = tMapView.getDisplayTMapInfo(polyLine.linePointList)
                            zoomLevel = info.zoom
                            setCenterPoint(info.point.latitude, info.point.longitude, true)
                        }
                        noticeRoute.text = getString(R.string.find_road)
                        // 지도까지 로딩 다 되면 다이얼로그 끄기
                        loadingDialog.setInvisible()
                    }
                }
            }
        }

        likeClickEvent.observe(this@TourDetailActivity) {
            Snackbar.make(binding.root, getString(R.string.like_place) + it, 2000).show()
        }

        likeStatus.observe(this@TourDetailActivity) {
            with(binding) {
                btnHeart.run {
                    isEnabled = false
                    isSelected = it
                    isEnabled = true
                }
            }
        }
    }

    private fun doNotFrameScroll() = with(binding) {
        container.setTouchListener(object : TMapFrameLayout.OnTouchListener {
            override fun onTouch() {
                nestedScrollView.requestDisallowInterceptTouchEvent(true)
            }
        })
    }

    private fun setTMap(info: DetailCommonEntity) {
        tMapView = TMapView(this)
        container.addView(tMapView)
        tMapView.setSKTMapApiKey(BuildConfig.SK_OPEN_API_KEY)
        setRouteOnTMap(info)
    }

    private fun setRouteOnTMap(detailInfo: DetailCommonEntity) = with(tMapView) {
        setOnMapReadyListener {
            setUserScrollZoomEnable(true)
            val startMarker = TMapMarkerItem().apply {
                id = "startMarker"
                visible = true
                icon = BitmapFactory.decodeResource(resources, R.drawable.icon_start_marker)
                setTMapPoint(App.latitude, App.longitude)
            }
            val endMarker = TMapMarkerItem().apply {
                id = "endMarker"
                visible = true
                icon = BitmapFactory.decodeResource(resources, R.drawable.icon_end_marker)
                setTMapPoint(detailInfo.latitude.toDouble(), detailInfo.longitude.toDouble())
            }
            addTMapMarkerItem(startMarker)
            addTMapMarkerItem(endMarker)
            val startPoint = TMapPoint(App.latitude, App.longitude)
            val endPoint =
                TMapPoint(detailInfo.latitude.toDouble(), detailInfo.longitude.toDouble())
            loadingDialog.setText(getString(R.string.loading_route))
            // 경로 찾기 시작
            tourDetailViewModel.getRouteMap(startPoint, endPoint)
        }
    }

    private fun bindingInfo(info: DetailCommonEntity) = with(binding) {
        imageViewMainPhoto.load(info.imageUrl)
        tourMainTitle.text = info.title.ifEmpty { getString(R.string.no_detail_info) }
        "${info.mainAddress}\n${info.subAddress}".also {
            addressTour.text = it.ifEmpty { getString(R.string.no_detail_info) }
        }
        addressTourDetail.text = info.description.ifEmpty { getString(R.string.no_detail_info) }
        phoneNumber.text = info.telPhoneNumber.ifEmpty { getString(R.string.no_detail_info) }
        moveToHomepage.text = info.homePage.ifEmpty { getString(R.string.no_detail_info) }
        addressTourDetail.post {
            with(addressTourDetail) {
                if (addressTourDetail.lineCount > 5) {
                    maxLines = 5
                    ellipsize = TextUtils.TruncateAt.END
                } else {
                    showMore.isVisible = false
                }
            }
        }
        showMore.setOnClickListener {
            controlFoldTextView()
        }
    }

    private fun controlFoldTextView() = with(binding) {
        when (showMore.text) {
            getString(R.string.more_description) -> {
                addressTourDetail.maxLines = Integer.MAX_VALUE
                addressTourDetail.ellipsize = null
                showMore.text = getString(R.string.fold_description)
            }

            else -> {
                addressTourDetail.maxLines = 5
                addressTourDetail.ellipsize = TextUtils.TruncateAt.END
                showMore.text = getString(R.string.more_description)
            }
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                1
            )
        }
    }

    private fun runCalendarDialog() {
        calendarBinding = DialogCalendarBinding.inflate(layoutInflater)
        setCalendarOption()
        dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_schedule))
            .setView(calendarBinding?.root)
            .setPositiveButton(getString(R.string.save)) { _, _ -> }
            .setNegativeButton(getString(R.string.disagree_permission)) { _, _ ->
                calendarBinding = null
            }
            .create()
        dialog.run {
            show()
            getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                tourDetailViewModel.saveMySchedule(
                    festivalItem,
                    keywordItem,
                    detailInfo
                )
            }
        }
    }

    private fun setCalendarOption() = with(calendarBinding!!) {
        addScheduleCalendarView.run {
            val month = Calendar.getInstance().get(Calendar.MONTH)
            removeDecorators()
            invalidateDecorators()
            addDecorators(
                SaturdayDecorator(month, 1),
                SundayDecorator(month, 1),
                OutDateMonthDecorator(this@TourDetailActivity, month + 1),
                TodayDecorator(this@TourDetailActivity)
            )
            setOnMonthChangedListener { _, date ->
                removeDecorators()
                invalidateDecorators()
                addDecorators(
                    SaturdayDecorator(date.month, 0),
                    SundayDecorator(date.month, 0),
                    TodayDecorator(this@TourDetailActivity),
                    OutDateMonthDecorator(this@TourDetailActivity, date.month),
                    CantSetDayDecorator(this@TourDetailActivity, selectedDayList)
                )
            }
            setOnRangeSelectedListener { _, dates ->
                tourDetailViewModel.selectScheduleRange(dates, selectedDayList)
            }
            setOnDateChangedListener { _, date, _ ->
                // 사용자는 하루만 선택을 할 수도 있으므로 단일 처리도 해야함
                val dates = listOf(date, date)
                tourDetailViewModel.selectScheduleRange(dates, selectedDayList)
            }
        }
    }

    private fun sharingPlace() {
        val placeInfo = "${detailInfo.title}\n${detailInfo.telPhoneNumber}\n${detailInfo.homePage}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, placeInfo)
            type = "text/plain"
        }
        val sharingIntent = Intent.createChooser(intent, "공유하기")
        startActivity(sharingIntent)
    }
}
