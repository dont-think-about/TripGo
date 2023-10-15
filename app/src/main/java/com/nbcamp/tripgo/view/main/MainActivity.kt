package com.nbcamp.tripgo.view.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivityMainBinding
import com.nbcamp.tripgo.view.home.HomeFragment
import com.nbcamp.tripgo.view.mypage.MyPageFragment
import com.nbcamp.tripgo.view.review.ReviewFragment
import com.nbcamp.tripgo.view.weather.WeatherFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() = with(binding) {
        mainBottomNavigation.itemIconTintList = null
        mainBottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    showFragment(HomeFragment.newInstance(), HomeFragment.TAG)
                    true
                }

                R.id.weather -> {
                    showFragment(WeatherFragment.newInstance(), WeatherFragment.TAG)
                    true
                }

                R.id.review -> {
                    showFragment(ReviewFragment.newInstance(), ReviewFragment.TAG)
                    true
                }

                R.id.my_page -> {
                    showFragment(MyPageFragment.newInstance(), MyPageFragment.TAG)
                    true
                }

                else -> false
            }
        }
        mainBottomNavigation.selectedItemId = R.id.home
    }

    private fun showFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment, tag)
            .commit()
    }
}
