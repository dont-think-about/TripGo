package com.nbcamp.tripgo.view.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
        mainBottomNavigation.setOnItemSelectedListener { item ->
            supportFragmentManager.popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            sharedViewModel.setCurrentPage(item.itemId)
            true
        }
        changeFragment(FragmentPageType.PAGE_HOME)
    }

    private fun initViewModels() = with(sharedViewModel) {
        currentPageType.observe(this@MainActivity) { currentPageType ->
            changeFragment(currentPageType)
        }

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

    private fun changeFragment(pageType: FragmentPageType) {
        val transaction = supportFragmentManager.beginTransaction()
        var targetFragment = supportFragmentManager.findFragmentByTag(pageType.tag)

        if (targetFragment == null) {
            targetFragment = getFragment(pageType)
            transaction.add(R.id.main_fragment_container, targetFragment, pageType.tag)
        }

        transaction.show(targetFragment)
        FragmentPageType.values()
            .filterNot { it == pageType }
            .forEach { type ->
                supportFragmentManager.findFragmentByTag(type.tag)?.let {
                    transaction.hide(it)
                }
            }

        transaction.commitAllowingStateLoss()
    }

    private fun getFragment(pageType: FragmentPageType): Fragment = when (pageType) {
        FragmentPageType.PAGE_HOME -> HomeFragment.newInstance()
        FragmentPageType.PAGE_CALENDAR -> CalendarFragment.newInstance()
        FragmentPageType.PAGE_REVIEW -> ReviewFragment.newInstance()
        FragmentPageType.PAGE_MY -> MyPageFragment.newInstance()
    }
}
