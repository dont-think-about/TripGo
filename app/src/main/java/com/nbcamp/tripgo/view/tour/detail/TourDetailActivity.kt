package com.nbcamp.tripgo.view.tour.detail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity
import com.nbcamp.tripgo.databinding.ActivityTourDetailBinding
import com.nbcamp.tripgo.databinding.DialogCalendarBinding
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.util.calendar.CantSetDayDecorator
import com.nbcamp.tripgo.util.calendar.OutDateMonthDecorator
import com.nbcamp.tripgo.util.calendar.SaturdayDecorator
import com.nbcamp.tripgo.util.calendar.SundayDecorator
import com.nbcamp.tripgo.util.calendar.TodayDecorator
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.main.MainViewModel
import com.nbcamp.tripgo.view.tour.detail.uistate.DetailCommonUiState
import com.prolificinteractive.materialcalendarview.CalendarDay
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
    private var calendarBinding: DialogCalendarBinding? = null
    private var currentUser: Any? = null

    private val tourDetailViewModel: TourDetailViewModel by viewModels {
        TourDetailViewModelFactory(
            this
        )
    }
    private val sharedViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTourDetailBinding.inflate(layoutInflater)
        loadingDialog = LoadingDialog(this)
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
        moveToCalendar.setOnClickListener {
            tourDetailViewModel.setUserOption()
            if (currentUser != null) {
                tourDetailViewModel.getMySchedules(currentUser!!)
                runCalendarDialog()
            } else {
//                toast(getString(R.string.not_login_so_dont_add_schedule))
                Snackbar.make(
                    binding.root,
                    getString(R.string.not_login_so_dont_add_schedule),
                    5000
                ).setAction("LOGIN") {
                    sharedViewModel.runLoginActivity()
                }.show()
            }
        }

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
            } else {
                loadingDialog.setInvisible()
            }
            state.detailInfo?.let { info ->
                bindingInfo(info)
                tourDetailViewModel.getRouteImage(info)
                detailInfo = info
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
            toast(getString(R.string.cant_select_duplicate_schedule))
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
            "${numSet.second}점".also { binding.evaluation.text = it }
            "${numSet.first}개의 리뷰".also { binding.tourReview.text = it }
        }

        routeImage.observe(this@TourDetailActivity) {
            binding.routeImage.load(it)
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
            .setTitle("일정 추가")
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
}
