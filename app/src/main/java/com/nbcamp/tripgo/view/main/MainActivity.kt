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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.kakao.sdk.user.model.Account
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivityMainBinding
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.util.checkPermission
import com.nbcamp.tripgo.util.setFancyDialog
import com.nbcamp.tripgo.view.App
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
    private lateinit var loadingDialog: LoadingDialog

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
        loadingDialog = LoadingDialog(this)

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
//            sharedViewModel.onClickBackButton()
            true
        }
        setUserState()
        changeFragment(FragmentPageType.PAGE_HOME)
    }

    private fun setUserState() {
        sharedViewModel.setUserState()
    }

    private fun initViewModels() = with(sharedViewModel) {
        currentPageType.observe(this@MainActivity) { currentPageType ->
            changeFragment(currentPageType)
        }

        eventBackClick.observe(this@MainActivity) {
            calendarViewModel.runDialogForReviewWriting(null, null)
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

        eventSetUser.observe(this@MainActivity) { setUserEvent ->
            when (setUserEvent) {
                is SetUserEvent.Loading -> {
                    loadingDialog.run {
                        setText(setUserEvent.message)
                        setVisible()
                    }
                }

                is SetUserEvent.Error -> {
                    loadingDialog.run {
                        setText(setUserEvent.message)
                        setInvisible()
                    }
                    Snackbar.make(binding.root, "회원 정보 확인이 실패했습니다\n 재로그인 해주세요", 5000)
                        .setAction("LOGIN") {
                            sharedViewModel.runLoginActivity()
                        }.show()
                }

                is SetUserEvent.Success -> {
                    when (setUserEvent.currentUser) {
                        is FirebaseUser -> App.firebaseUser = setUserEvent.currentUser
                        is Account -> App.kaKaoUser = setUserEvent.currentUser
                    }
                    loadingDialog.run {
                        setText(setUserEvent.message)
                        setInvisible()
                    }
                    println("firebaseUser:" + App.firebaseUser)
                    println("kakaoUser: " + App.kaKaoUser)
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

        // 데이터 정합성을 위해 캘린더에 들어 갈 때는 데이터를 실시간 업데이트 - 중요
        if (targetFragment is CalendarFragment) {
            transaction.replace(
                R.id.main_fragment_container,
                CalendarFragment.newInstance(),
                pageType.tag
            )
        } else {
            transaction.show(targetFragment)
            FragmentPageType.values()
                .filterNot { it == pageType }
                .forEach { type ->
                    supportFragmentManager.findFragmentByTag(type.tag)?.let {
                        transaction.hide(it)
                    }
                }
        }

        transaction.commit()
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
                    sharedViewModel.runGalleryEvent()
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
                sharedViewModel.runGalleryEvent()
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
