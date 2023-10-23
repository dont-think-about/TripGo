package com.nbcamp.tripgo.view.tour.detail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import coil.load
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity
import com.nbcamp.tripgo.databinding.ActivityTourDetailBinding
import com.nbcamp.tripgo.databinding.DialogCalendarBinding
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.util.calendar.OutDateMonthDecorator
import com.nbcamp.tripgo.util.calendar.SaturdayDecorator
import com.nbcamp.tripgo.util.calendar.SundayDecorator
import com.nbcamp.tripgo.util.calendar.TodayDecorator
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import java.util.Calendar

class TourDetailActivity : AppCompatActivity() {
    private var festivalItem: FestivalItem? = null
    private var keywordItem: KeywordItem? = null
    private var nearbyContentId: String? = null
    private lateinit var binding: ActivityTourDetailBinding
    private lateinit var loadingDialog: LoadingDialog
    private var calendarBinding: DialogCalendarBinding? = null

    private val tourDetailViewModel: TourDetailViewModel by viewModels { TourDetailViewModelFactory() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTourDetailBinding.inflate(layoutInflater)
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
            runCalendarDialog()
        }

        runSearchDetailInformation(contentId)

    }

    private fun runSearchDetailInformation(contentId: String?) {
        tourDetailViewModel.runSearchDetailInformation(contentId)
    }

    private fun initViewModel() = with(tourDetailViewModel) {
        detailUiState.observe(this@TourDetailActivity) { state ->
            with(binding) {
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
                }
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
        // TODO 평점 및 길찾기
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
        AlertDialog.Builder(this)
            .setTitle("일정 추가")
            .setView(calendarBinding?.root)
            .setPositiveButton("저장") { _, _ ->
                calendarBinding = null
            }.setNegativeButton("취소") { _, _ ->
                calendarBinding = null
            }
            .create()
            .show()
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
//                    SelectedDayDecorator(selectedDayList),
                    OutDateMonthDecorator(this@TourDetailActivity, date.month)
                )
            }
        }
    }

}
