package com.nbcamp.tripgo.view.attraction

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
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
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient

class AttractionsActivity : AppCompatActivity() {
    companion object {
        const val MAX_ROWS = 150
        const val REQUEST_CHECK_SETTINGS = 1001
        const val LOCATION_PERMISSION_REQUEST_CODE = 1234
    } // numOfRows

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
                LOCATION_PERMISSION_REQUEST_CODE

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
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val settingsClient: SettingsClient = LocationServices.getSettingsClient(this)

        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnSuccessListener {

            getMyLocation() // 위치 서비스가 활성화 된 경우 위치 정보를 가져옴
        }

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
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
                    retrofitArea()
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
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
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
            REQUEST_CHECK_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    getMyLocation()
                } else {
                    showError(getString(R.string.tour_location_error))
                }
            }
        }
    }
}
