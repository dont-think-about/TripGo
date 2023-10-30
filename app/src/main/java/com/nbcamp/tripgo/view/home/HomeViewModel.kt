package com.nbcamp.tripgo.view.home

import android.location.Location
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
import com.nbcamp.tripgo.view.home.uistate.HomeFestivalUiState
import com.nbcamp.tripgo.view.home.uistate.HomeNearbyPlaceUiState
import com.nbcamp.tripgo.view.home.uistate.HomeProvincePlaceUiState
import com.nbcamp.tripgo.view.home.uistate.HomeWeatherUiState
import com.nbcamp.tripgo.view.home.valuetype.AreaCode
import com.nbcamp.tripgo.view.home.valuetype.ProvincePlaceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
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

    private val _nearbyPlaceUiState: MutableLiveData<HomeNearbyPlaceUiState> = MutableLiveData()
    val nearbyPlaceUiState: LiveData<HomeNearbyPlaceUiState>
        get() = _nearbyPlaceUiState

    private val _provincePlaceUiState: MutableLiveData<HomeProvincePlaceUiState> = MutableLiveData()
    val provincePlaceUiState: LiveData<HomeProvincePlaceUiState>
        get() = _provincePlaceUiState

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
            runCatching {
                val travelers = homeRepository.getCalculationTravelers(
                    responseCount = 1000,
                    startDate = getPastDateString.first,
                    endDate = getPastDateString.second
                )
                if (travelers == null) {
                    _festivalUiState.postValue(HomeFestivalUiState.error())
                }

                // 많이 방문한 3개의 시도를 구함
                val manyTravelersCountList =
                    getHowManyTravelersByPlace(travelers)?.take(3)?.map { it.first }

                val festivals = homeRepository.getFestivalsInThisMonth(
                    responseCount = 1000, startDate = getPastDateString.third
                )

                val filteredFestival = getPopularFestival(
                    festivals, manyTravelersCountList
                )?.let { list ->
                    val randomList = arrayListOf<FestivalEntity>()
                    (0 until 10).forEach { _ ->
                        val randomIndex = Random.nextInt(list.size)
                        randomList.add(list[randomIndex])
                    }
                    randomList
                }
                _festivalUiState.postValue(HomeFestivalUiState(filteredFestival, false))
            }.onFailure {
                _festivalUiState.postValue(HomeFestivalUiState.error())
            }
        }
    }

    fun getPlaceByTodayWeather() {
        _weatherSearchUiState.value = HomeWeatherUiState.initialize()
        viewModelScope.launch(Dispatchers.IO) {
            val today = getTodayInfo()
            runCatching {
                val todayWeather = homeRepository.getTodayWeather(today.first, today.second)
                when (todayWeather?.weatherType) {
                    WeatherType.SUNNY -> {
                        val entity = runSearchByKeyword(
                            keyword = "외",
                            contentTypeId = "15",
                            responseCount = 1000,
                        )?.apply {
                            temperature = todayWeather.temperature
                            weatherType = todayWeather.weatherType
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
                            temperature = todayWeather?.temperature ?: "0"
                            weatherType =
                                todayWeather?.weatherType ?: WeatherType.UNDEFINED
                        }
                        _weatherSearchUiState.postValue(HomeWeatherUiState(entity, false))
                    }
                }
            }.onFailure {
                _weatherSearchUiState.postValue(HomeWeatherUiState.error())
            }
        }
    }

    fun getNearbyPlaceList(
        location: Location?,
        pageNumber: Int
    ) {
        _nearbyPlaceUiState.value = HomeNearbyPlaceUiState.initialize()
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val nearbyPlaces = homeRepository.getNearbyPlaces(
                    latitude = location?.latitude.toString(),
                    longitude = location?.longitude.toString(),
                    radius = "10000", // 10km 이내,
                    pageNumber = pageNumber.toString()
                )
                val calculatedDistanceData = nearbyPlaces?.onEach { nearbyPlaceEntity ->
                    nearbyPlaceEntity.apply {
                        distance = getDistance(
                            location?.latitude ?: Double.NaN,
                            location?.longitude ?: Double.NaN,
                            latitude.toDouble(),
                            longitude.toDouble()
                        ).toString()
                    }
                }
                _nearbyPlaceUiState.postValue(
                    HomeNearbyPlaceUiState(
                        calculatedDistanceData,
                        false
                    )
                )
            }.onFailure {
                _nearbyPlaceUiState.postValue(HomeNearbyPlaceUiState.error())
            }
        }
    }


    fun getProvincePlace() {
        _provincePlaceUiState.value = HomeProvincePlaceUiState.initialize()
        val list = AreaCode.values().toList()
        val provinceInfo = arrayListOf<ProvincePlaceEntity>()
        list.forEach { areaCode ->
            provinceInfo.add(
                ProvincePlaceEntity(
                    areaCode = areaCode.areaCode,
                    name = areaCode.sido,
                    tourListCount = areaCode.tourListCount,
                    imageUrl = areaCode.defaultImageUrl
                )
            )
        }
        _provincePlaceUiState.value = HomeProvincePlaceUiState(provinceInfo, false)
    }

    // 위도 경도 사이 거리 계산 (m)
    private fun getDistance(
        myLatitude: Double,
        myLongitude: Double,
        placeLatitude: Double,
        placeLongitude: Double
    ): Double {
        val distanceLatitude = Math.toRadians(placeLatitude - myLatitude)
        val distanceLongitude = Math.toRadians(placeLongitude - myLongitude)
        val a = sin(distanceLatitude / 2) * sin(distanceLatitude / 2) + cos(
            Math.toRadians(myLatitude)
        ) * cos(Math.toRadians(placeLatitude)) * sin(distanceLongitude / 2) * sin(
            distanceLongitude / 2
        )
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS * c * 1000
    }

    private suspend fun runSearchByKeyword(
        keyword: String,
        contentTypeId: String,
        responseCount: Int
    ): KeywordSearchEntity? = homeRepository.getInformationByKeyword(
        keyword = keyword, contentTypeId = contentTypeId, responseCount = responseCount
    ).let { list ->
        var randomIndex = 0
        list?.let { searchList ->
            randomIndex = searchList.size.let { Random.nextInt(it) }
        }
        list?.get(randomIndex)
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

    private fun getHowManyTravelersByPlace(
        data: List<TravelerEntity>?
    ) = data?.groupBy { it.districtName }?.map { group ->
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
            "${year}${strMonth}01", "${year}${strMonth}30", "${year}${strThisMonth}01"
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
        val strHour = if (hour.toInt() < 10) "0${hour.toInt() - 1}" else "${hour.toInt() - 1}"
        return date to strHour + "00"
    }

    fun autoSlideViewPager() {
        thread = Thread(PagerRunnable())
        thread.start()
    }

    private fun setPage() {
        if (_currentPage.value == 10) _currentPage.value = 0
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

    companion object {
        const val EARTH_RADIUS = 6371
    }
}
