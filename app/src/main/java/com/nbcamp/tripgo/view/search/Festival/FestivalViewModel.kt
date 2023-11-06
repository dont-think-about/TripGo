package com.nbcamp.tripgo.view.search.Festival

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.data.repository.mapper.WeatherType
import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity
import com.nbcamp.tripgo.view.search.SearchAdapter
import com.nbcamp.tripgo.view.search.SearchKeywordUiState
import com.nbcamp.tripgo.view.search.SearchRepository
import kotlinx.coroutines.launch
import okhttp3.internal.userAgent

class FestivalViewModel(
    private val searchRepository: SearchRepository
) : ViewModel() {
    lateinit var adapter: SearchAdapter

    private val _searchUiState: MutableLiveData<SearchKeywordUiState> = MutableLiveData()
    val searchUiState: LiveData<SearchKeywordUiState>
        get() = _searchUiState

    // ViewModel의 fetchSearchResult 함수
    fun fetchSearchResult(keyword: String, startDate: String) {
        Log.d("키워드1", "값 = $keyword")

        viewModelScope.launch {
            runCatching {
                val festivalList = arrayListOf<KeywordSearchEntity>()
                val searchResult = searchRepository.getFestivalBySearch(
                    startDate = startDate,
                    responseCount = 100
                )
                    ?.filter { it.addr1.contains(keyword) }
                println(searchResult)
                searchResult?.forEach {
                    val result = KeywordSearchEntity(
                        contentId = it.contentid,
                        title = it.title,
                        address = it.addr1,
                        imageUrl = it.firstimage,
                        latitude = it.mapy,
                        longitude = it.mapx,
                        weatherType = WeatherType.UNDEFINED,
                        temperature = "0",
                    )
                    festivalList.add(result)
                }
                if (searchResult == null) {
                    _searchUiState.postValue(SearchKeywordUiState.error())
                } else {
                    _searchUiState.postValue(SearchKeywordUiState(festivalList))
                }
            }
        }
    }
}
