package com.nbcamp.tripgo.view.home

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.data.repository.mapper.WeatherType
import com.nbcamp.tripgo.data.repository.model.FestivalEntity
import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity
import com.nbcamp.tripgo.data.repository.model.TravelerEntity
import com.nbcamp.tripgo.util.APIResponse
import com.nbcamp.tripgo.view.home.uistate.HomeFestivalUiState
import com.nbcamp.tripgo.view.home.uistate.HomeWeatherUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class HomeViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {
    private val _festivalUiState: MutableLiveData<HomeFestivalUiState> = MutableLiveData()
    val festivalUiState: LiveData<HomeFestivalUiState>
        get() = _festivalUiState

    private val _weatherSearchUiState: MutableLiveData<HomeWeatherUiState> = MutableLiveData()
    val weatherSearchUiState: LiveData<HomeWeatherUiState>
        get() = _weatherSearchUiState


    private val handler = Handler(Looper.getMainLooper()) {
        setPage()
        true
    }

    private lateinit var thread: Thread
    private val _currentPage: MutableLiveData<Int> = MutableLiveData(0)
    val currentPage: LiveData<Int>
        get() = _currentPage

    fun fetchViewPagerData() {
        val getPastDateString = getPastDateString()
        _festivalUiState.value = HomeFestivalUiState.initialize()
        viewModelScope.launch(Dispatchers.IO) {
            val travelers = homeRepository.getCalculationTravelers(
                responseCount = 1000,
                startDate = getPastDateString.first,
                endDate = getPastDateString.second
            )
            when (travelers) {
                is APIResponse.Error -> {
                    _festivalUiState.postValue(HomeFestivalUiState.error())
                }

                is APIResponse.Success -> {
                    // 많이 방문한 3개의 시도를 구함
                    val manyTravelersCountList = getHowManyTravelersByPlace(travelers.data)
                        ?.take(3)?.map { it.first }

                    val festivals = homeRepository.getFestivalsInThisMonth(
                        responseCount = 1000,
                        startDate = getPastDateString.third
                    )

                    val filteredFestival = getPopularFestival(
                        festivals.data,
                        manyTravelersCountList
                    )?.let { list ->
                        val randomList = arrayListOf<FestivalEntity>()
                        (0 until 10).forEach { _ ->
                            val randomIndex = Random.nextInt(list.size)
                            randomList.add(list[randomIndex])
                        }
                        randomList
                    }

                    _festivalUiState.postValue(HomeFestivalUiState(filteredFestival, false))
                }
            }
        }
    }

    fun getPlaceByTodayWeather() {
        _weatherSearchUiState.value = HomeWeatherUiState.initialize()
        viewModelScope.launch(Dispatchers.IO) {
            val today = getTodayInfo()
            val todayWeather =
                homeRepository.getTodayWeather(today.first, today.second)
            when (todayWeather) {
                is APIResponse.Error -> {
                    _weatherSearchUiState.postValue(HomeWeatherUiState.error())
                }

                is APIResponse.Success -> {
                    when (todayWeather.data?.weatherType) {
                        WeatherType.SUNNY -> {
                            val entity = runSearchByKeyword(
                                keyword = "외",
                                contentTypeId = "15",
                                responseCount = 1000,
                            )?.apply {
                                temperature = todayWeather.data.temperature
                                weatherType = todayWeather.data.weatherType
                            }

                            _weatherSearchUiState.postValue(HomeWeatherUiState(entity, false))
                        }

                        WeatherType.UNDEFINED -> Unit
                        else -> {
                            val entity = runSearchByKeyword(
                                keyword = "관",
                                contentTypeId = "14",
                                responseCount = 1000,
                            )?.apply {
                                temperature = todayWeather.data?.temperature ?: "0"
                                weatherType =
                                    todayWeather.data?.weatherType ?: WeatherType.UNDEFINED
                            }
                            _weatherSearchUiState.postValue(HomeWeatherUiState(entity, false))
                        }
                    }
                }
            }
        }
    }


    fun getNearbyPlaceList() {
        viewModelScope.launch(Dispatchers.IO) {
//            val nearbyPlaces = homeRepository.getNearbyPlaces()
        }
    }

    private suspend fun runSearchByKeyword(
        keyword: String,
        contentTypeId: String,
        responseCount: Int
    ): KeywordSearchEntity? = homeRepository.getInformationByKeyword(
        keyword = keyword,
        contentTypeId = contentTypeId,
        responseCount = responseCount
    ).let { list ->
        var randomIndex = 0
        list.data?.let { searchList ->
            randomIndex = searchList.size.let { Random.nextInt(it) }
        }
        list.data?.get(randomIndex)
    }


    private fun getPopularFestival(
        data: List<FestivalEntity>?,
        manyTravelersCountList: List<String>?
    ) = data?.filter {
        it.address.contains(
            """${manyTravelersCountList?.get(0)}|${manyTravelersCountList?.get(1)}|${
                manyTravelersCountList?.get(2)
            }""".toRegex()
        )
    }

    private fun getHowManyTravelersByPlace(data: List<TravelerEntity>?) =
        data?.groupBy { it.districtName }?.map { group ->
            group.key to group.value.sumOf { it.travelCount }
        }?.sortedByDescending { it.second }

    private fun getPastDateString(): Triple<String, String, String> {
        val calendar = Calendar.getInstance(Locale.KOREAN)
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)
        val thisMonth = month + 1
        // month는 0~11 범위로 동작
        if (month == 0) {
            // 0이면 현재 1월이므로 month++를 해서 이전 달인 12월로 맞추고 year 는 -1
            month = 12
            year--
        }
        val strMonth = if (month < 10) "0$month" else month.toString()
        val strThisMonth = if (thisMonth < 10) "0$thisMonth" else thisMonth.toString()

        return Triple(
            "${year}${strMonth}01",
            "${year}${strMonth}30",
            "${year}${strThisMonth}01"
        )
    }

    private fun getTodayInfo(): Pair<String, String> {
        val sdf = SimpleDateFormat("yyyyMMdd HH", Locale.KOREAN)
        val day = Date(System.currentTimeMillis())
        var (date, hour) = sdf.format(day).split(" ")
        if (hour == "00") {
            hour = "24"
            date = (date.toInt() - 1).toString()
        }
        return date to "${hour.toInt() - 1}" + "00"
    }

    fun autoSlideViewPager() {
        thread = Thread(PagerRunnable())
        thread.start()
    }

    private fun setPage() {
        if (_currentPage.value == 10)
            _currentPage.value = 0
        _currentPage.value = _currentPage.value?.plus(1)
    }

    fun stopSlideViewPager() {
        thread.interrupt()
    }

    inner class PagerRunnable : Runnable {
        override fun run() {
            while (true) {
                try {
                    Thread.sleep(5000)
                    handler.sendEmptyMessage(0)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
