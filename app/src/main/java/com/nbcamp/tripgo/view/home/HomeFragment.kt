package com.nbcamp.tripgo.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.nbcamp.tripgo.databinding.FragmentHomeBinding
import com.nbcamp.tripgo.util.TourTheme
import com.nbcamp.tripgo.view.home.adapter.FestivalViewPagerAdapter
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()
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

    }

    private fun initViewModel() = with(homeViewModel) {
        // viewpager 데이터 가져오기
        fetchViewPagerData()
        autoSlideViewPager()
        festivalUiState.observe(viewLifecycleOwner) { state ->
            with(binding) {
                festivalProgressBar.isVisible = state.isLoading
                mainFestivalViewPager.isVisible = state.isLoading.not()
                viewPagerCircleIndicator.createIndicators(state.list?.size ?: 0, 0)
                onBind(state)
            }
        }
        currentPage.observe(viewLifecycleOwner) { currentPage ->
            binding.mainFestivalViewPager.setCurrentItem(currentPage, true)
        }
    }

    private fun onBind(state: HomeFestivalUiState?) = with(binding) {
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
