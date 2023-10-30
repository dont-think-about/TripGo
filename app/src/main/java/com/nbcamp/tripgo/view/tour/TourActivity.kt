package com.nbcamp.tripgo.view.tour

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.data.service.RetrofitModule
import com.nbcamp.tripgo.databinding.ActivityTourBinding
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

    private val LOCATION_PERMISSION_REQUEST_CODE = 1234

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
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        tourTheme = intent.getIntExtra("theme", -100)
        initView()
        getMyLocation()
    }

    private fun initView() {

        when (tourTheme) {
            TourTheme.FAMILY.themeId -> {
                binding.tourOfTheMonth.text = "가족 여행"
                retrofitThemeSearch("가족")
            }

            TourTheme.HEALING.themeId -> {
                binding.tourOfTheMonth.text = "힐링"
                retrofitThemeSearch("힐링")
            }

            TourTheme.CAMPING.themeId -> {
                binding.tourOfTheMonth.text = "캠핑"
                retrofitThemeSearch("캠핑")
            }

            TourTheme.TASTY.themeId -> {
                binding.tourOfTheMonth.text = "맛집"
                retrofitThemeSearch("맛")
            }

            TourTheme.POPULAR.themeId -> {
                binding.tourOfTheMonth.text = "이달의 축제"
                retrofitFestival()
            }

            TourTheme.NEARBY.themeId -> Unit
            TourTheme.SEARCH.themeId -> Unit
        }
    }

    private fun retrofitThemeSearch(keyword: String) {
        binding.tourRecyclerview.adapter = tourSearchAdapter
        showProgressBar(true)
        lifecycleScope.launch {
            val service = RetrofitModule.createTourApiService()

            try {
                val response = service.getPlaceBySearch(
                    keyword = keyword,
                    contentTypeId = "",
                    responseCount = 100
                )
                if (response.isSuccessful && response.body() != null) {
                    val festivals = response.body()?.response?.body?.items?.item
                    if (festivals != null) {
                        println(festivals)
                        tourSearchAdapter.submitList(festivals.toMutableList())
                    }
                } else {
                    showError(getString(R.string.tour_error))
                }
            } catch (e: Exception) {
                showError(getString(R.string.tour_exception_error))
            } finally {
                showProgressBar(false)
            }
        }
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

    private fun retrofitFestival() {
        binding.tourRecyclerview.adapter = tourAdapter
        showProgressBar(true)
        lifecycleScope.launch {
            val service = RetrofitModule.createTourApiService()
            val currentDate = Calendar.getInstance()
            val startDate = "${currentDate.get(Calendar.YEAR)}" +
                    "${String.format("%02d", currentDate.get(Calendar.MONTH) + 1)}01"
            try {
                val response = service.getFestivalInThisMonth(
                    startDate = startDate,
                    responseCount = 100
                )
                if (response.isSuccessful && response.body() != null) {
                    val festivals = response.body()?.response?.body?.items?.item
                    if (festivals != null) {
                        tourAdapter.submitList(festivals)
                    }
                } else {
                    showError(getString(R.string.tour_error))
                }
            } catch (e: Exception) {
                showError(getString(R.string.tour_exception_error))
            } finally {
                showProgressBar(false) // API 응답 후 로딩 비활성화
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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
                } else {
                    showError(getString(R.string.tour_location_error))
                }
            }
        }
    } // 사용자 현재 위치를 가져 오는 함수

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMyLocation()
            } else {
                showError(getString(R.string.permission_denide_location))
            }
        }
    }
}
