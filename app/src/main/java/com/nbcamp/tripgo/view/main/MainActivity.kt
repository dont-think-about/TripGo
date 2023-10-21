package com.nbcamp.tripgo.view.main

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivityMainBinding
import com.nbcamp.tripgo.util.checkPermission
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.util.setFancyDialog
import com.nbcamp.tripgo.view.attraction.AttractionsActivity
import com.nbcamp.tripgo.view.calendar.CalendarFragment
import com.nbcamp.tripgo.view.calendar.CalendarViewModel
import com.nbcamp.tripgo.view.calendar.CalendarViewModelFactory
import com.nbcamp.tripgo.view.home.HomeFragment
import com.nbcamp.tripgo.view.home.valuetype.TourTheme
import com.nbcamp.tripgo.view.login.LogInActivity
import com.nbcamp.tripgo.view.mypage.MyPageFragment
import com.nbcamp.tripgo.view.review.ReviewFragment
import com.nbcamp.tripgo.view.search.SearchActivity
import com.nbcamp.tripgo.view.tour.TourActivity
import com.nbcamp.tripgo.view.tour.detail.TourDetailActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val sharedViewModel: MainViewModel by viewModels()
    private val calendarViewModel: CalendarViewModel by viewModels {
        CalendarViewModelFactory(
            this
        )
    }

    private val permissionGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        permission
                    ),
                    100
                )
            }
        }

    private val permissionLocationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    100
                )
            }
        }

    private val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

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
            sharedViewModel.onClickBackButton()
            true
        }
        changeFragment(FragmentPageType.PAGE_HOME)
    }

    private fun initViewModels() = with(sharedViewModel) {
        currentPageType.observe(this@MainActivity) { currentPageType ->
            changeFragment(currentPageType)
        }

        eventBackClick.observe(this@MainActivity) { backClicked ->
            when (backClicked) {
                is BackClickEvent.OpenDialog -> {
                    calendarViewModel.runDialogForReviewWriting(null, null)
                }
            }
        }

        eventPermission.observe(this@MainActivity) { permissionState ->
            when (permissionState) {
                is PermissionEvent.GetGalleryPermission -> {
                    checkGalleryPermissions(permissionState.permission)
                }

                is PermissionEvent.GetLocationPermission -> {
                    checkLocationPermission(permissionState.permission)
                }
            }
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

                is ThemeClickEvent.RunLogInActivity -> {
                    startActivity(Intent(this@MainActivity, LogInActivity::class.java))
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

    private fun checkGalleryPermissions(permission: String) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            checkPermission(
                context = this,
                permission = permission,
                permissionLauncher = permissionGalleryLauncher,
                showPermissionContextPopUp = {
                    showGalleryPermissionPopUp()
                },
                runTaskAfterPermissionGranted = {
                    toast("갤러리 권한 요청 완료")
                })
            return
        }
        checkPermission(
            context = this,
            permission = permission,
            permissionLauncher = permissionGalleryLauncher,
            showPermissionContextPopUp = {
                showGalleryPermissionPopUp()
            },
            runTaskAfterPermissionGranted = {
                toast("갤러리 권한 요청 완료")
            })
    }


    private fun checkLocationPermission(permission: String) {
        checkPermission(
            context = this,
            permission = permission,
            permissionLauncher = permissionLocationLauncher,
            showPermissionContextPopUp = {
                showLocationPermissionPopUp()
            },
            runTaskAfterPermissionGranted = {
                sharedViewModel.setLocationEvent()
            }
        )
    }

    private fun showGalleryPermissionPopUp() {
        setFancyDialog(
            context = this,
            title = getString(R.string.permission_for_gallery),
            message = getString(R.string.need_permission_into_gallery),
            positiveText = getString(R.string.yes),
            negativeText = getString(R.string.no),
            icon = R.drawable.icon_gallery,
            onPositiveClicked = {
                permissionGalleryLauncher.launch(permission)
            }
        ).show()
    }

    private fun showLocationPermissionPopUp() {
        setFancyDialog(
            context = this,
            title = getString(R.string.need_permission),
            message = getString(R.string.for_load_nearby_place),
            positiveText = getString(R.string.agree_permission),
            negativeText = getString(R.string.disagree_permission),
            icon = R.drawable.icon_map,
            onPositiveClicked = {
                permissionLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        ).show()
    }
}
