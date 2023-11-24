package com.nbcamp.tripgo.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieDrawable
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivitySplashBinding
import com.nbcamp.tripgo.view.home.HomeViewModel
import com.nbcamp.tripgo.view.home.HomeViewModelFactory
import com.nbcamp.tripgo.view.home.uistate.HomeFestivalUiState
import com.nbcamp.tripgo.view.home.uistate.HomeProvincePlaceUiState
import com.nbcamp.tripgo.view.main.MainActivity
import com.nbcamp.tripgo.view.main.MainActivity.Companion.FESTIVAL_EXTRA_KEY
import com.nbcamp.tripgo.view.main.MainActivity.Companion.PROVINCE_EXTRA_KEY

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    private val homeViewModel: HomeViewModel by viewModels { HomeViewModelFactory() }
    private lateinit var homeFestivalUiState: HomeFestivalUiState
    private lateinit var homeProvinceUiState: HomeProvincePlaceUiState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val animation = binding.splashLottie
        animation.run {
            setAnimation("lottie_5.json")
            speed = 4f
            playAnimation()
            repeatCount = LottieDrawable.INFINITE
        }

        initViewModel()
        fetchHomeData()
    }

    private fun fetchHomeData() = with(homeViewModel) {
        fetchViewPagerData()
        getProvincePlace()
    }

    private fun initViewModel() = with(homeViewModel) {
        festivalUiState.observe(this@SplashActivity) {
            if (it.list.isNullOrEmpty().not())
                homeFestivalUiState = it
            runHomeFragment()
        }
        provincePlaceUiState.observe(this@SplashActivity) {
            if (it.list.isNullOrEmpty().not())
                homeProvinceUiState = it
            runHomeFragment()
        }
    }

    private fun runHomeFragment() {
        if (::homeFestivalUiState.isInitialized && ::homeProvinceUiState.isInitialized) {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(FESTIVAL_EXTRA_KEY, homeFestivalUiState)
                putExtra(PROVINCE_EXTRA_KEY, homeProvinceUiState)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up_enter, R.anim.slide_down_exit)
        }
    }
}
