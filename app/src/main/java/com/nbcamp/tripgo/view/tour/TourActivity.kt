package com.nbcamp.tripgo.view.tour

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.data.service.RetrofitModule
import com.nbcamp.tripgo.databinding.ActivityTourBinding
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.attraction.AttractionsActivity
import com.nbcamp.tripgo.view.home.valuetype.TourTheme
import com.nbcamp.tripgo.view.tour.adapter.TourAdapter
import com.nbcamp.tripgo.view.tour.adapter.TourSearchAdapter
import com.nbcamp.tripgo.view.tour.detail.TourDetailActivity
import kotlinx.coroutines.launch
import java.util.Calendar

class TourActivity : AppCompatActivity() {

    private var tourTheme: Int = 0

    private val binding: ActivityTourBinding by lazy {
        ActivityTourBinding.inflate(layoutInflater)
    }

    private val tourSearchAdapter: TourSearchAdapter by lazy {
        TourSearchAdapter { tourItem -> gotoDetailActivity(null, tourItem) }
    }

    private val tourAdapter: TourAdapter by lazy {
        TourAdapter { festivalItem -> gotoDetailActivity(festivalItem, null) }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentPage = 1

    private var theme = ""

    private var isLastPage = false

    private val scrollListener by lazy {
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && !binding.tourRecyclerview.canScrollVertically(1)
                ) {
                    if (isLastPage) {
                        toast("마지막 페이지 입니다.")
                        return
                    }
                    when (tourTheme) {
                        TourTheme.POPULAR.themeId -> getFestivalSearch(++currentPage)
                        else -> getThemeSearch(theme, ++currentPage)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.distance.setOnClickListener {
            tourSearchAdapter.tourDistance(binding.tourRecyclerview)
            tourAdapter.popularDistance(binding.tourRecyclerview)
            updateButtonColors(isDistanceSelected = true)
        }

        binding.date.setOnClickListener {
            tourSearchAdapter.tourDate(binding.tourRecyclerview)
            tourAdapter.popularDate(binding.tourRecyclerview)
            updateButtonColors(isDistanceSelected = false)
        }

        binding.tourBackButton.setOnClickListener {
            finish()
        }

        binding.tourRecyclerview.apply {

            addOnScrollListener(scrollListener)

            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        tourTheme = intent.getIntExtra("theme", -100)

        // 위치 권한 체크 후 처리
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 승인 되지 않았을 경우, 권한 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                AttractionsActivity.LOCATION_PERMISSION_REQUEST_CODE
            )
            Toast.makeText(
                this,
                getString(R.string.request_location_permission),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // 이미 권한이 승인된 경우, 위치 정보를 가져 옵니다.
            getMyLocation()
            Toast.makeText(this, getString(R.string.load_location_information), Toast.LENGTH_SHORT)
                .show()
        }
        checkLocationSettingAndEnable()
    }

    private fun checkLocationSettingAndEnable() {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }  // 위치 요청 설정

        val builder = LocationSettingsRequest.Builder()  // 위치 설정 요청 빌더 생성
            .addLocationRequest(locationRequest)

        val settingsClient: SettingsClient =
            LocationServices.getSettingsClient(this)  // 위치 설정 client 생성

        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build()) // 위치 설정 체크

        locationSettingsResponseTask.addOnSuccessListener {
            getMyLocation() // 위치 서비스가 활성화 된 경우 위치 정보를 가져옴
        }

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        this,
                        AttractionsActivity.REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Toast.makeText(
                        this,
                        getString(R.string.cannot_change_location_settings),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } // 위치 설정에 문제가 있을 경우 처리
    }

    private fun initView() {

        when (tourTheme) {
            TourTheme.FAMILY.themeId -> {
                binding.tourOfTheMonth.text = "가족 여행"
                theme = "가족"
                retrofitThemeSearch(theme)
            }

            TourTheme.HEALING.themeId -> {
                binding.tourOfTheMonth.text = "힐링"
                theme = "힐링"
                retrofitThemeSearch(theme)
            }

            TourTheme.CAMPING.themeId -> {
                binding.tourOfTheMonth.text = "캠핑"
                theme = "캠핑"
                retrofitThemeSearch(theme)
            }

            TourTheme.TASTY.themeId -> {
                binding.tourOfTheMonth.text = "맛집"
                theme = "맛"
                retrofitThemeSearch(theme)
            }

            TourTheme.POPULAR.themeId -> {
                binding.tourOfTheMonth.text = "이달의 축제"
                retrofitFestival()
            }

            TourTheme.NEARBY.themeId -> {
                binding.tourOfTheMonth.text = "주변에 있는 관광지"
            }

            TourTheme.SEARCH.themeId -> Unit
        }
    }

    private fun getThemeSearch(keyword: String, currentPage: Int) {
        if (currentPage == 1) {
            showProgressBar(true)
        } else {
            showSmallProgressBar(true)
        }
        lifecycleScope.launch {
            val service = RetrofitModule.createTourApiService()

            try {
                val response = service.getPlaceSearchByPage(
                    keyword = keyword,
                    contentTypeId = "",
                    responseCount = 20,
                    currentPage = currentPage
                )
                if (response.isSuccessful && response.body() != null) {
                    val festivals = response.body()?.response?.body?.items?.item
                    if (festivals != null) {
                        if (festivals.size < 20) {
                            isLastPage = true
                        }
                        tourSearchAdapter.submitList(festivals.toMutableList())
                    }
                } else {
                    showError(getString(R.string.tour_error))
                }
            } catch (e: Exception) {
                showError(getString(R.string.tour_exception_error))
            } finally {
                if (currentPage == 1) {
                    showProgressBar(false)
                } else {
                    showSmallProgressBar(false)
                }
            }
        }
    }

    private fun retrofitThemeSearch(keyword: String) {
        binding.tourRecyclerview.adapter = tourSearchAdapter
        getThemeSearch(keyword, 1)
    }

    private fun getFestivalSearch(currentPage: Int) {
        if (currentPage == 1) {
            showProgressBar(true)
        } else {
            showSmallProgressBar(true)
        }
        lifecycleScope.launch {
            val service = RetrofitModule.createTourApiService()
            val currentDate = Calendar.getInstance()
            val startDate = "${currentDate.get(Calendar.YEAR)}" +
                    "${String.format("%02d", currentDate.get(Calendar.MONTH) + 1)}01"
            try {
                val response = service.getFestivalInThisMonthByPage(
                    startDate = startDate,
                    responseCount = 20,
                    currentPage = currentPage
                )
                if (response.isSuccessful && response.body() != null) {
                    val festivals = response.body()?.response?.body?.items?.item
                    if (festivals != null) {
                        if (festivals.size < 20) {
                            isLastPage = true
                        }
                        tourAdapter.submitList(festivals)
                    }
                } else {
                    showError(getString(R.string.tour_error))
                }
            } catch (e: Exception) {
                showError(getString(R.string.tour_exception_error))
            } finally {
                if (currentPage == 1) {
                    showProgressBar(false)
                } else {
                    showSmallProgressBar(false)
                }
            }
        }
    }

    private fun retrofitFestival() {
        binding.tourRecyclerview.adapter = tourAdapter
        showProgressBar(true)
        getFestivalSearch(1)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun updateButtonColors(isDistanceSelected: Boolean) {
        if (isDistanceSelected) {
            binding.distance.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.date.setTextColor(ContextCompat.getColor(this, R.color.gray))
        } else {
            binding.distance.setTextColor(ContextCompat.getColor(this, R.color.gray))
            binding.date.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    private fun gotoDetailActivity(festivalItem: FestivalItem?, keywordItem: KeywordItem?) {
        val myIntent = Intent(this, TourDetailActivity::class.java)
            .apply {
                putExtra("festivalItem", festivalItem)
                putExtra("keywordItem", keywordItem)
            }
        startActivity(myIntent)
    } // Detail Activity로 넘어 가는 함수

    private fun showProgressBar(show: Boolean) {
        binding.tourProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.tourRecyclerview.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showSmallProgressBar(show: Boolean) {
        binding.smallTourProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLat = location.latitude
                    val userLon = location.longitude
                    tourSearchAdapter.setUserLocation(userLat, userLon)
                    tourAdapter.setUserLocation(userLat, userLon)
                    initView()
                } else {
                    showError(getString(R.string.tour_location_error))
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AttractionsActivity.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocationSettingAndEnable()
                } else {
                    showError(getString(R.string.permission_denide_location))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AttractionsActivity.REQUEST_CHECK_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    getMyLocation()
                } else {
                    showError(getString(R.string.tour_location_error))
                }
            }
        }
    }
}
