package com.nbcamp.tripgo.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import coil.load
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.mapper.WeatherType
import com.nbcamp.tripgo.databinding.FragmentHomeBinding
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.home.adapter.FestivalViewPagerAdapter
import com.nbcamp.tripgo.view.home.uistate.HomeFestivalUiState
import com.nbcamp.tripgo.view.home.uistate.HomeWeatherUiState
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViews()
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
        }
        viewPagerCircleIndicator.setViewPager(mainFestivalViewPager)
        // viewpager 데이터 가져오기
        homeViewModel.run {
            fetchViewPagerData()
            autoSlideViewPager()
            getPlaceByTodayWeather()
        }
    }

    private fun initViewModel() = with(homeViewModel) {
        festivalUiState.observe(viewLifecycleOwner) { state ->
            with(binding) {
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
            with(binding) {
                weatherEventProgressBar.isVisible = state.isLoading
                mainWeatherEventImageView.isVisible = state.isLoading.not()
                mainWeatherCelsiusTextView.isVisible = state.isLoading.not()
                onBindWeatherSearch(state)
            }
        }
    }

    private fun onBindWeatherSearch(state: HomeWeatherUiState?) = with(binding) {
        mainWeatherEventImageView.load(state?.data?.imageUrl)
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

            null -> requireActivity().toast("정보를 불러오는데 실패하였습니다.")
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

    override fun onDestroyView() {
        super.onDestroyView()
        homeViewModel.stopSlideViewPager()
    }

    companion object {
        fun newInstance() = HomeFragment()

        const val TAG = "HOME_FRAGMENT"
    }
}
