package com.nbcamp.tripgo.view.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.nbcamp.tripgo.BuildConfig
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.mapper.WeatherType
import com.nbcamp.tripgo.databinding.FragmentHomeBinding
import com.nbcamp.tripgo.util.FestivalTransformer
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.home.adapter.FestivalViewPagerAdapter
import com.nbcamp.tripgo.view.home.adapter.NearbyPlaceAdapter
import com.nbcamp.tripgo.view.home.adapter.ProvincePlaceListAdapter
import com.nbcamp.tripgo.view.home.uistate.HomeFestivalUiState
import com.nbcamp.tripgo.view.home.uistate.HomeNearbyPlaceUiState
import com.nbcamp.tripgo.view.home.uistate.HomeWeatherUiState
import com.nbcamp.tripgo.view.home.valuetype.ProvincePlaceEntity
import com.nbcamp.tripgo.view.home.valuetype.TourTheme
import com.nbcamp.tripgo.view.main.MainViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels { HomeViewModelFactory() }
    private val sharedViewModel: MainViewModel by activityViewModels()

    private val festivalViewPagerAdapter by lazy {
        FestivalViewPagerAdapter()
    }

    // 위치 정보 획득에 관한 변수 모음
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null

    private val nearbyPlaceAdapter by lazy {
        NearbyPlaceAdapter(requireActivity()) { contentId ->
            runTourDetailActivity(contentId)
        }
    }
    private val provincePlaceListAdapter by lazy {
        ProvincePlaceListAdapter(requireActivity()) { model ->
            runAttractionActivity(model)
        }
    }

    // gps가 켜져있는 지 확인
    private var isGpsOn = false

    // 오른쪽 끝일 때, 다음 페이지를 불러올 OnScrollListener
    private val endScrollListener by lazy {
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                    !binding.mainNearbyTourRecyclerView.canScrollHorizontally(1)
                ) {
                    homeViewModel.getNearbyPlaceList(locationForScrollListener, ++nearbyPageNumber)
                }
            }
        }
    }
    private var nearbyPageNumber = 1
    private var locationForScrollListener: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkNewVersion()
        checkGPSStatus()
        initVariables()
        initViewModel()
        initViews()
    }

    private fun checkNewVersion() {
        val appUpdateManager = AppUpdateManagerFactory.create(requireActivity())
        homeViewModel.checkNewVersion(appUpdateManager)
    }

    private fun checkGPSStatus() {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // gps가 켜져 있다면 on 상태로 바뀜
        isGpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun initVariables() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        cancellationTokenSource = CancellationTokenSource()
//        checkLocationPermissions()
    }

    private fun initViews() = with(binding) {
        mainSearchView.setOnClickListener {
            runThemeTourActivity(TourTheme.SEARCH)
        }
        mainFamilyCardView.setOnClickListener {
            runThemeTourActivity(TourTheme.FAMILY)
        }
        mainCampingCardView.setOnClickListener {
            runThemeTourActivity(TourTheme.CAMPING)
        }
        mainHealingCardView.setOnClickListener {
            runThemeTourActivity(TourTheme.HEALING)
        }
        mainTastyCardView.setOnClickListener {
            runThemeTourActivity(TourTheme.TASTY)
        }
        mainMorePopularFestivalTextView.setOnClickListener {
            runThemeTourActivity(TourTheme.POPULAR)
        }
        mainMoreNearbyTourListTextView.setOnClickListener {
            runThemeTourActivity(TourTheme.NEARBY)
        }
        mainFestivalViewPager.run {
            adapter = festivalViewPagerAdapter
            setPageTransformer(FestivalTransformer())
        }
        viewPagerCircleIndicator.setViewPager(mainFestivalViewPager)
        mainNearbyTourRecyclerView.run {
            adapter = nearbyPlaceAdapter
            addOnScrollListener(endScrollListener)
        }
        mainAllTourListRecyclerView.run {
            adapter = provincePlaceListAdapter
        }
    }

    @SuppressLint("MissingPermission")
    private fun initViewModel() = with(homeViewModel) {
        // 버전 확인하고 다이얼로그 띄우기
        versionCode.observe(viewLifecycleOwner) { versionCode ->
            // 같으면 아무동작안하고 리턴
            if (versionCode == BuildConfig.VERSION_CODE) {
                return@observe
            }
            runUpdateDialog()
        }

        // viewpager 데이터 가져오기
        homeViewModel.run {
            fetchViewPagerData()
            autoSlideViewPager()
            getProvincePlace()
        }

        festivalUiState.observe(viewLifecycleOwner) { state ->
            with(binding) {
                if (state == HomeFestivalUiState.error()) {
                    requireActivity().toast(getString(R.string.load_failed_data))
                    return@observe
                }
                festivalProgressBar.isVisible = state.isLoading
                mainFestivalViewPager.isVisible = state.isLoading.not()
                viewPagerCircleIndicator.createIndicators(state.list?.size ?: 0, 0)
                onBindFestival(state)
            }
        }
        currentPage.observe(viewLifecycleOwner) { currentPage ->
            binding.mainFestivalViewPager.setCurrentItem(currentPage, true)
        }
        weatherSearchUiState.observe(viewLifecycleOwner) { state ->
            if (state == HomeWeatherUiState.error()) {
                requireActivity().toast(getString(R.string.load_failed_data))
                return@observe
            }
            with(binding) {
                weatherEventProgressBar.isVisible = state.isLoading
                mainWeatherEventImageView.isVisible = state.isLoading.not()
                mainWeatherCelsiusTextView.isVisible = state.isLoading.not()
                onBindWeatherSearch(state)
            }
        }
        nearbyPlaceUiState.observe(viewLifecycleOwner) { state ->
            if (state == HomeNearbyPlaceUiState.error()) {
                requireActivity().toast(getString(R.string.load_failed_data))
                return@observe
            }
            binding.nearbyProgressBar.isVisible = state.isLoading
            nearbyPlaceAdapter.setList(state.list)
        }
        provincePlaceUiState.observe(viewLifecycleOwner) { state ->
            binding.allTourProgressBar.isVisible = state.isLoading
            provincePlaceListAdapter.submitList(state.list)
        }

        sharedViewModel.eventSetLocation.observe(viewLifecycleOwner) {
            if (isGpsOn.not()) {
                // 권한 체크 했는데 gps가 꺼져 있다면 gps 설정 화면으로
                requireActivity().toast(getString(R.string.on_gps_for_location_permission))
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                return@observe
            }
            fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource!!.token
            ).addOnSuccessListener { location ->
                locationForScrollListener = location
                App.latitude = location.latitude
                App.longitude = location.longitude
                homeViewModel.run {
                    getNearbyPlaceList(location, nearbyPageNumber)
                    getPlaceByTodayWeather(location)
                }
            }
        }
    }

    private fun onBindWeatherSearch(state: HomeWeatherUiState?) = with(binding) {
        mainWeatherEventImageView.load(state?.data?.imageUrl, App.imageLoader) {
            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
        }
        (state?.data?.temperature + getString(R.string.temperature_sign)).also {
            mainWeatherCelsiusTextView.text = it
        }
        mainWeatherEventTitleTextView.text = state?.data?.title
        mainWeatherEventDateTextView.text = state?.data?.address
        when (state?.data?.weatherType) {
            WeatherType.SUNNY -> {
                mainWeatherTextView.text = getString(R.string.good_weather)
                mainWeatherIcon.load(R.drawable.icon_sun)
            }

            null -> requireActivity().toast(getString(R.string.load_failed_data))
            WeatherType.RAIN -> {
                mainWeatherTextView.text = getString(R.string.bad_weather)
                mainWeatherIcon.load(R.drawable.icon_rain)
            }

            WeatherType.RAIN_OR_SNOW -> {
                mainWeatherTextView.text = getString(R.string.bad_weather)
                mainWeatherIcon.load(R.drawable.icon_rain)
            }

            WeatherType.SNOW -> {
                mainWeatherTextView.text = getString(R.string.bad_weather)
                mainWeatherIcon.load(R.drawable.icon_snow)
            }

            WeatherType.RAIN_DROP -> {
                mainWeatherTextView.text = getString(R.string.bad_weather)
                mainWeatherIcon.load(R.drawable.icon_cloudy)
            }

            WeatherType.RAIN_SNOW_DROP -> {
                mainWeatherTextView.text = getString(R.string.bad_weather)
                mainWeatherIcon.load(R.drawable.icon_cloudy)
            }

            WeatherType.SNOW_FLYING -> {
                mainWeatherTextView.text = getString(R.string.bad_weather)
                mainWeatherIcon.load(R.drawable.icon_snow)
            }

            WeatherType.UNDEFINED -> Unit
        }
    }

    private fun onBindFestival(state: HomeFestivalUiState?) = with(binding) {
        festivalViewPagerAdapter.submitList(state?.list)
    }

    private fun runThemeTourActivity(themeId: TourTheme) {
        sharedViewModel.runThemeTourActivity(themeId)
    }

    private fun runTourDetailActivity(contentId: String) {
        sharedViewModel.runTourDetailActivity(contentId)
    }

    private fun runAttractionActivity(model: ProvincePlaceEntity) {
        sharedViewModel.runAttractionActivity(model)
    }

    private fun checkLocationPermissions() {
        sharedViewModel.getLocationPermissionEvent(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun runUpdateDialog() {
        AlertDialog.Builder(requireActivity())
            .setTitle(getString(R.string.new_update))
            .setMessage(getString(R.string.new_update_available))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.playstore_url))
                    )
                )
            }.setNegativeButton(getString(R.string.no)) { _, _ -> }
            .create()
            .show()
    }


    override fun onResume() {
        super.onResume()
        checkGPSStatus()
        checkLocationPermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeViewModel.stopSlideViewPager()
        _binding = null
    }

    companion object {
        fun newInstance() = HomeFragment()

        const val TAG = "HOME_FRAGMENT"
    }
}
