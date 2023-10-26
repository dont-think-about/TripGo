package com.nbcamp.tripgo.view.tour

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
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.data.service.RetrofitModule
import com.nbcamp.tripgo.databinding.ActivityTourBinding
import com.nbcamp.tripgo.view.home.valuetype.TourTheme
import com.nbcamp.tripgo.view.tour.adapter.TourAdapter
import com.nbcamp.tripgo.view.tour.adapter.TourSearchAdapter
import com.nbcamp.tripgo.view.tour.detail.TourDetailActivity
import kotlinx.coroutines.launch
import android.Manifest


class TourActivity : AppCompatActivity() {

    private var tourTheme: Int = 0

    private val binding: ActivityTourBinding by lazy {
        ActivityTourBinding.inflate(layoutInflater)
    }

    private val tourSearchAdapter: TourSearchAdapter by lazy {
        TourSearchAdapter { tourItem -> gotoDetailActivity(null, tourItem) }
    }
    //tourSearchAdapter 연결

    private val tourAdapter: TourAdapter by lazy {
        TourAdapter { festivalItem -> gotoDetailActivity(festivalItem, null) }
    }
    //tourAdapter 연결

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    //내 위치

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        binding.distance.setOnClickListener {

            tourSearchAdapter.tourDistance(binding.tourRecyclerview)
            tourAdapter.popularDistance(binding.tourRecyclerview)

        } // 거리순 클릭시

        binding.date.setOnClickListener {
            tourSearchAdapter.tourDate(binding.tourRecyclerview)
            tourAdapter.popularDate(binding.tourRecyclerview)
        } // 날짜순 클릭시

        binding.tourBackButton.setOnClickListener {
            finish()
        } // 뒤로 가기 버튼


        binding.tourRecyclerview.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            // 각 항목마다 줄 그어줌
        }


        tourTheme = intent.getIntExtra("theme", -100)


        initView()

        getMyLocation()

    }

    private fun initView() {

        when (tourTheme) {
            TourTheme.FAMILY.themeId -> {

                retrofitThemeSearch("가족")

            }

            TourTheme.HEALING.themeId -> {

                retrofitThemeSearch("힐링")


            }

            TourTheme.CAMPING.themeId -> {
                retrofitThemeSearch("캠핑")

            }

            TourTheme.TASTY.themeId -> {

                retrofitThemeSearch("맛")


            }

            TourTheme.POPULAR.themeId -> {

                retrofitWork()

            }

            TourTheme.NEARBY.themeId -> Unit
            TourTheme.SEARCH.themeId -> Unit
        }

    } // 해당 뷰 마다 데이터를 보여줌

    private fun retrofitThemeSearch(keyword: String) {
        binding.tourRecyclerview.adapter = tourSearchAdapter
        showProgressBar(true) // API 호출 전에 로딩 활성화
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
                    showError("행사 정보를 가져오는데 실패했습니다.")
                }
            } catch (e: Exception) {
                showError("행사 정보를 가져오는 도중 오류가 발생했습니다: ${e.localizedMessage}")
                println(e.localizedMessage)
            } finally {
                showProgressBar(false) //API 응답후 로딩 비활성화
            }

        }
    }
    // Retrofit 연결 해서 검색 단어 마다 data 를 가져 와주는 함수


    private fun retrofitWork() {
        binding.tourRecyclerview.adapter = tourAdapter
        showProgressBar(true)
        lifecycleScope.launch {
            val service = RetrofitModule.createTourApiService()

            try {
                val response = service.getFestivalInThisMonth(
                    startDate = "20231001",
                    responseCount = 100
                )
                if (response.isSuccessful && response.body() != null) {
                    val festivals = response.body()?.response?.body?.items?.item
                    if (festivals != null) {
                        tourAdapter.submitList(festivals)
                    }
                } else {
                    showError("행사 정보를 가져 오는데 실패 했습니다.")
                }
            } catch (e: Exception) {
                showError("행사 정보를 가져 오는 도중 오류가 발생 했습니다: ${e.localizedMessage}")
                println(e.localizedMessage)
            } finally {
                showProgressBar(false) //API 응답 후 로딩 비활성화
            }

        }
    } // retrofit 연결 해서 인기 여행지 알려 주는 함수

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }  // 에러 토스트 메세지 함수

    private fun gotoDetailActivity(festivalItem: FestivalItem?, keywordItem: KeywordItem?) {
        val myIntent = Intent(this, TourDetailActivity::class.java)
            .apply {
                putExtra("festivalItem", festivalItem)
                putExtra("keywordItem", keywordItem)
            }
        startActivity(myIntent)
    }
    // Detail Activity로 넘어 가는 함수

    private fun showProgressBar(show: Boolean) {
        binding.tourProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.tourRecyclerview.visibility = if (show) View.GONE else View.VISIBLE
    }  // ProgressBar 함수


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
                } else {
                    showError("Cannot fetch location.")
                }
            }
        }
    } //위치


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, now you can get the user's location
                getMyLocation()
            } else {
                showError("Permission denied. Cannot fetch location.")
            }
        }
    }
}




