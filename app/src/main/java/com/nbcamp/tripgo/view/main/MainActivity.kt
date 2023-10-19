package com.nbcamp.tripgo.view.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivityMainBinding
import com.nbcamp.tripgo.view.attraction.AttractionsActivity
import com.nbcamp.tripgo.view.calendar.CalendarFragment
import com.nbcamp.tripgo.view.home.HomeFragment
import com.nbcamp.tripgo.view.home.valuetype.TourTheme
import com.nbcamp.tripgo.view.mypage.MyPageFragment
import com.nbcamp.tripgo.view.review.ReviewFragment
import com.nbcamp.tripgo.view.search.SearchActivity
import com.nbcamp.tripgo.view.tour.TourActivity
import com.nbcamp.tripgo.view.tour.detail.TourDetailActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val sharedViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initViewModels()
    }

    private fun initViews() = with(binding) {
        mainBottomNavigation.itemIconTintList = null
        mainBottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    showFragment(HomeFragment.newInstance(), HomeFragment.TAG)
                    true
                }

                R.id.calendar -> {
                    showFragment(CalendarFragment.newInstance(), CalendarFragment.TAG)
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

    private fun initViewModels() = with(sharedViewModel) {
        event.observe(this@MainActivity) { themeClickEvent ->
            when (themeClickEvent) {
                is ThemeClickEvent.RunTourThemeActivity -> {
                    if (themeClickEvent.theme == TourTheme.SEARCH) {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                SearchActivity::class.java
                            )
                        )
                        return@observe
                    }
                    startActivity(
                        Intent(
                            this@MainActivity,
                            TourActivity::class.java
                        ).apply {
                            putExtra("theme", themeClickEvent.theme)
                        }
                    )
                }

                is ThemeClickEvent.RunTourDetailActivity -> {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            TourDetailActivity::class.java
                        ).apply {
                            putExtra("contentId", themeClickEvent.contentId)
                        }
                    )
                }

                is ThemeClickEvent.RunAttractionActivity -> {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            AttractionsActivity::class.java
                        ).apply {
                            putExtra("provinceModel", themeClickEvent.model)
                        }
                    )
                }
            }
        }
    }

    private fun showFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment, tag)
            .commit()
    }
}
