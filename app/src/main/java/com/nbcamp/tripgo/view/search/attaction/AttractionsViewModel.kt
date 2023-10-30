package com.nbcamp.tripgo.view.search.attaction

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.view.search.SearchAdapter
import com.nbcamp.tripgo.view.search.SearchKeywordUiState
import com.nbcamp.tripgo.view.search.SearchRepository
import kotlinx.coroutines.launch

class AttractionsViewModel(
    private val searchRepository: SearchRepository
) : ViewModel() {
    lateinit var adapter: SearchAdapter

    private val _searchUiState: MutableLiveData<SearchKeywordUiState> = MutableLiveData()
    val searchUiState: LiveData<SearchKeywordUiState>
        get() = _searchUiState

    // ViewModel의 fetchSearchResult 함수
    fun fetchSearchResult(keyword: String) {
        Log.d("키워드1", "값 = $keyword")

        viewModelScope.launch {
            runCatching {
                val searchResult = searchRepository.getPlaceBySearch(
                    keyword = keyword,
                    contentTypeId = "14",
                    responseCount = 20
                )
                println(searchResult)
                if (searchResult == null) {
                    _searchUiState.postValue(SearchKeywordUiState.error())
                } else {
                    _searchUiState.postValue(SearchKeywordUiState(searchResult))
                }
            }
        }
    }
}