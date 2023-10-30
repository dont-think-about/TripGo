package com.nbcamp.tripgo.view.calendar.modify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.nbcamp.tripgo.BuildConfig
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity
import com.nbcamp.tripgo.databinding.ActivityTourDetailBinding
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.util.TMapFrameLayout
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.main.MainActivity
import com.nbcamp.tripgo.view.tour.detail.TextClickEvent
import com.nbcamp.tripgo.view.tour.detail.TourDetailViewModel
import com.nbcamp.tripgo.view.tour.detail.TourDetailViewModelFactory
import com.nbcamp.tripgo.view.tour.detail.uistate.DetailCommonUiState
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapMarkerItem

class TourModifyDetailActivity : AppCompatActivity() {
    private var startDate: CalendarDay? = null
    private var endDate: CalendarDay? = null
    private var contentId: String? = null
    private lateinit var binding: ActivityTourDetailBinding
    private lateinit var loadingDialog: LoadingDialog
    private var entity: CalendarEntity? = null
    private var selectedDayList: List<CalendarDay>? = null
    private var forModifySchedule: ArrayList<CalendarEntity>? = null
    private lateinit var detailInfo: DetailCommonEntity
    private lateinit var container: TMapFrameLayout
    private lateinit var tMapView: TMapView
    private var currentUser: Any? = null

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
        startDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("startDate", CalendarDay::class.java)
        } else {
            intent?.getParcelableExtra("startDate")
        }

        endDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("endDate", CalendarDay::class.java)
        } else {
            intent?.getParcelableExtra("endDate")
        }
        contentId = intent?.getStringExtra("contentId")
        entity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("model", CalendarEntity::class.java)
        } else {
            intent?.getParcelableExtra("model")
        }
        selectedDayList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableArrayListExtra("selectedDayList", CalendarDay::class.java)
        } else {
            intent?.getParcelableArrayListExtra("selectedDayList")
        }
        forModifySchedule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableArrayListExtra("forModifySchedule", CalendarEntity::class.java)
        } else {
            intent?.getParcelableArrayListExtra("forModifySchedule")
        }
    }

    private fun initViews() = with(binding) {
        val contentId = contentId
        moveToCalendar.run {
            text = "일정 수정하기"
            setTextColor(ResourcesCompat.getColor(resources, R.color.black, theme))
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
            val bundle = Bundle().apply {
                putParcelable("model", entity)
                putParcelableArrayList(
                    "selectedDayList",
                    selectedDayList?.let { ArrayList(it) }
                )
                putParcelableArrayList(
                    "forModifySchedule",
                    forModifySchedule?.let { ArrayList(it) }
                )
            }
            ScheduleModifyFragment.newInstance().apply {
                arguments = bundle
            }.show(supportFragmentManager, ScheduleModifyFragment.TAG)
        }

        // 상단 홈 버튼 클릭 시 메인으로 이동
        btnHome.setOnClickListener {
            startActivity(Intent(
                this@TourModifyDetailActivity,
                MainActivity::class.java
            ).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }
        // 공유 버튼
        btnShare.setOnClickListener {
            sharingPlace()
        }
        when {
            CalendarDay.today().isInRange(startDate, endDate) -> {
                moveToCalendar.run {
                    text = getString(R.string.now_doing_schedule_cant_modify)
                    setTextColor(resources.getColor(R.color.black, theme))
                    isEnabled = false
                }
            }

            endDate?.isBefore(CalendarDay.today()) == true -> {
                moveToCalendar.run {
                    text = getString(R.string.past_schedule_not_modify)
                    setTextColor(resources.getColor(R.color.black, theme))
                    isEnabled = false
                }
            }
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
        detailUiState.observe(this@TourModifyDetailActivity) { state ->
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

        textClickEvent.observe(this@TourModifyDetailActivity) { event ->
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

        loginStatus.observe(this@TourModifyDetailActivity) { state ->
            // 유저 정보 확인
            currentUser = state.user
        }

        countAndRatting.observe(this@TourModifyDetailActivity) { numSet ->
            "${if (numSet.second.isNaN()) 0.0 else numSet.second}점".also {
                binding.evaluation.text = it
            }
            "${numSet.first}개의 리뷰".also { binding.tourReview.text = it }
        }

        routeMap.observe(this@TourModifyDetailActivity) { polyLine ->
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

        likeClickEvent.observe(this@TourModifyDetailActivity) {
            Snackbar.make(binding.root, getString(R.string.like_place) + it, 2000).show()
        }

        likeStatus.observe(this@TourModifyDetailActivity) {
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

    private fun sharingPlace() {
        val placeInfo = "${detailInfo.title}\n${detailInfo.telPhoneNumber}\n${detailInfo.homePage}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, placeInfo)
            type = "text/plain"
        }
        val sharingIntent = Intent.createChooser(intent, getString(R.string.sharing_place))
        startActivity(sharingIntent)
    }
}
