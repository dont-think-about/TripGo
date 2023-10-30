package com.nbcamp.tripgo.view.attraction

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import com.nbcamp.tripgo.data.service.RetrofitModule
import com.nbcamp.tripgo.databinding.ActivityAttractionsBinding
import com.nbcamp.tripgo.view.home.valuetype.ProvincePlaceEntity
import kotlinx.coroutines.launch

class AttractionsActivity : AppCompatActivity() {
    companion object {
        const val MAX_ROWS = 150
    } // numOfRows

    private val LOCATION_PERMISSION_REQUEST_CODE = 1234

    private val binding: ActivityAttractionsBinding by lazy {
        ActivityAttractionsBinding.inflate(layoutInflater)
    }

    private val attractionsAdapter: AttractionsAdapter by lazy {
        AttractionsAdapter {}
    }

    private lateinit var provinceItem: ProvincePlaceEntity

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this) // 사용자 현재 위치를 가져 오는 객체

        provinceItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("provinceModel", ProvincePlaceEntity::class.java)!!
        } else {
            intent?.getParcelableExtra("provinceModel")!!
        } // 지역 정보 가져 오기

        val provinceName = provinceItem.name
        binding.attractionOfTheMonth.text =
            getString(R.string.tourist_attractions, provinceName) // 지역 이름 표시

        binding.attractionBackButton.setOnClickListener {
            finish()
        }

        binding.attractionRecyclerview.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = attractionsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        } // 아이템 선 표시

        binding.distance.setOnClickListener {
            attractionsAdapter.attractionDistance(binding.attractionRecyclerview)
            updateButtonColors(isDistanceSelected = true)
        }

        binding.date.setOnClickListener {
            attractionsAdapter.attractionDate(binding.attractionRecyclerview)
            updateButtonColors(isDistanceSelected = false)
        }

        retrofitArea()
        getMyLocation()
    }

    private fun retrofitArea() {
        binding.attractionRecyclerview.adapter = attractionsAdapter
        showProgressBar(true)
        lifecycleScope.launch {
            val service = RetrofitModule.createAreaApiService()

            try {
                val response = provinceItem.areaCode.let {
                    service.getAreaInformation(

                        areaCode = it,
                        numOfRows = MAX_ROWS

                    )
                }
                if (response.isSuccessful && response.body() != null) {
                    val area = response.body()?.response?.body?.items?.item
                    if (area != null) {
                        attractionsAdapter.submitList(area)
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
    } // 관광지 정보 가져 오기

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showProgressBar(show: Boolean) {
        binding.attractionProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.attractionRecyclerview.visibility = if (show) View.GONE else View.VISIBLE
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
                    attractionsAdapter.setUserLocation(userLat, userLon)
                } else {
                    showError(getString(R.string.tour_location_error))
                }
            }
        }
    } // 사용자 현재 위치를 가져 오는 함수

    private fun updateButtonColors(isDistanceSelected: Boolean) {
        if (isDistanceSelected) {
            binding.distance.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.date.setTextColor(ContextCompat.getColor(this, R.color.gray))
        } else {
            binding.distance.setTextColor(ContextCompat.getColor(this, R.color.gray))
            binding.date.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

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
