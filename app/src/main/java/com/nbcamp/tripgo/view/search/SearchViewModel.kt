package com.nbcamp.tripgo.view.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.data.model.travelerssegmentation.SegmentationItem
import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class SearchViewModel(
    private val searchRepository: SearchRepository
) : ViewModel() {
    private lateinit var adapter: SearchAdapter
    private val _pullData: MutableLiveData<List<KeywordSearchEntity>> = MutableLiveData()
    val pullData: LiveData<List<KeywordSearchEntity>>
        get() = _pullData
    var manyTravelersCountList: List<String>? = null
    private val _rankList: MutableLiveData<List<String>> = MutableLiveData()
    val rankList: LiveData<List<String>>
        get() = _rankList

    fun initAdapter(adapter: SearchAdapter) {
        this.adapter = adapter
    }

    fun sendSearchData(list: List<KeywordSearchEntity>) {
        _pullData.value = list
    }

    fun fetchViewPagerData() {
        val getPastDateString = getPastDateString()
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val travelersSegmentation = searchRepository.getCalculationTravelers(
                    responseCount = 500,
                    startDate = getPastDateString.first,
                    endDate = getPastDateString.second
                )
                if (travelersSegmentation == null) {
                }

                val manyTravelersCountList =
                    getHowManyTravelersByPlace(travelersSegmentation)?.map { it.first }
                Log.d("데이터1", "$manyTravelersCountList")

                manyTravelersCountList?.let {
                    _rankList.postValue(it)
                }
                // manyTravelersCountList에 데이터가 저장되어 있을 것입니다.
            }.onFailure {
                Log.d("데이터12", it.localizedMessage.toString())
                // 오류 처리
            }
        }
    }

    private fun getHowManyTravelersByPlace(data: List<SegmentationItem>?) =
        data?.filter { it.touDivNm == "외지인(b)" }?.groupBy { it.signguNm }?.map { group ->
            val placeName = group.key
            val travelersCount = group.value.sumOf { it.touNum.toDouble().toInt() }

            // 이미 필터링된 글자를 한 번 더 필터링
            val filteredPlaceName = placeName
                .replace("특별자치시", "")// "특별자치시" 제거
            val formattedPlaceName =
                if (filteredPlaceName.endsWith("시") || filteredPlaceName.endsWith("군")) {
                    filteredPlaceName.substring(0, filteredPlaceName.length - 1)
                } else {
                    filteredPlaceName
                }

            formattedPlaceName to travelersCount
        }?.sortedByDescending { it.second }?.take(10)

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
}
