package com.nbcamp.tripgo.view.calendar.modify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity
import com.nbcamp.tripgo.databinding.ActivityTourDetailBinding
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.util.KaKaoMapFrameLayout
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.main.MainActivity
import com.nbcamp.tripgo.view.tour.detail.TextClickEvent
import com.nbcamp.tripgo.view.tour.detail.TourDetailViewModel
import com.nbcamp.tripgo.view.tour.detail.TourDetailViewModelFactory
import com.nbcamp.tripgo.view.tour.detail.uistate.DetailCommonUiState
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.ArrayList

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
    private lateinit var container: KaKaoMapFrameLayout
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
            startActivity(
                Intent(
                    this@TourModifyDetailActivity,
                    MainActivity::class.java
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            )
        }
        // 공유 버튼
        btnShare.setOnClickListener {
            sharingPlace()
        }
        // 카카오 맵 자차 실행
        runKakaoMapCar.setOnClickListener {
            val url =
                "kakaomap://route?sp=${App.latitude},${App.longitude}&ep=${detailInfo.latitude},${detailInfo.longitude}8&by=CAR"
            runKaKaoMap(url)
        }
        // 카카오 맵 대중교통 실행
        runKakaoMapPublic.setOnClickListener {
            val url =
                "kakaomap://route?sp=${App.latitude},${App.longitude}&ep=${detailInfo.latitude},${detailInfo.longitude}8&by=PUBLICTRANSIT"
            runKaKaoMap(url)
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
                setKaKaoMap(info)
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
        container.setTouchListener(object : KaKaoMapFrameLayout.OnTouchListener {
            override fun onTouch() {
                nestedScrollView.requestDisallowInterceptTouchEvent(true)
            }
        })
    }

    private fun setKaKaoMap(info: DetailCommonEntity) {
        val mapView = MapView(this)
        container.addView(mapView)
        mapView.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() {
                    // 지도 API 가 정상적으로 종료될 때 호출됨
                }

                override fun onMapError(error: Exception) {
                    Log.d("오류", "$error")
                }
            },
            object : KakaoMapReadyCallback() {
                override fun onMapReady(kakaoMap: KakaoMap) {
                    // 인증 후 API가 정상적으로 실행될 때 호출됨
                    val position = LatLng.from(
                        info.latitude.toDouble(),
                        info.longitude.toDouble()
                    )
                    val myPosition = LatLng.from(
                        App.latitude,
                        App.longitude
                    )

                    val cameraUpdate = CameraUpdateFactory.newCenterPosition(position)
                    kakaoMap.moveCamera(cameraUpdate)

                    val endStyles = kakaoMap.labelManager
                        ?.addLabelStyles(
                            LabelStyles.from(
                                LabelStyle.from(R.drawable.icon_end_marker)
                            )
                        )
                    val startStyles = kakaoMap.labelManager
                        ?.addLabelStyles(
                            LabelStyles.from(
                                LabelStyle.from(R.drawable.icon_start_marker)
                            )
                        )
                    val endOptions = LabelOptions.from(position)
                        .setStyles(endStyles)
                    val startOptions = LabelOptions.from(myPosition)
                        .setStyles(startStyles)

                    val layer = kakaoMap.labelManager!!.layer
                    layer!!.run {
                        addLabel(startOptions)
                        addLabel(endOptions)
                    }

                    kakaoMap.setOnMapClickListener { _, _, _, _ ->
                        kakaoMap.moveCamera(
                            cameraUpdate,
                            CameraAnimation.from(
                                500,
                                true,
                                true
                            )
                        )
                        CameraUpdateFactory.zoomTo(10)
                    }
                }
            }
        )
        loadingDialog.setInvisible()
    }

    private fun runKaKaoMap(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        // 카카오맵 어플리케이션이 사용자 핸드폰에 깔려있으면 바로 앱으로 연동
        // 그렇지 않다면 다운로드 페이지로 연결
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=net.daum.android.map")
                )
            )
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
