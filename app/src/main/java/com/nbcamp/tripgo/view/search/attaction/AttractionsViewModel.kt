package com.nbcamp.tripgo.view.search.attaction

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.view.search.SearchKeywordUiState
import com.nbcamp.tripgo.view.search.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AttractionsViewModel(
    private val searchRepository: SearchRepository
) : ViewModel() {
    private val _searchUiState: MutableLiveData<SearchKeywordUiState> = MutableLiveData()
    val searchUiState: LiveData<SearchKeywordUiState>
        get() = _searchUiState

    //    private val handler = Handler(Looper.getMainLooper()) {
//        setPage()
//        true
//    }
    private lateinit var thread: Thread
    private val _currentPage: MutableLiveData<Int> = MutableLiveData(0)
    val currentPage: LiveData<Int>
        get() = _currentPage

    fun fetchSearchResult(keyword: String) {
        _searchUiState.value = SearchKeywordUiState.initialize()
        Log.d("키워드1", "값 = $keyword")
        kotlin.runCatching {
            viewModelScope.launch {
                val searchResult = searchRepository.getPlaceBySearch(
                    keyword = keyword,
                    contentTypeId = "14",
                    responseCount = 20
                )
                if (searchResult == null) {
                    _searchUiState.value = (SearchKeywordUiState.error())
                }
                Log.d("키워드2", "값 = $searchResult")
                _searchUiState.value = SearchKeywordUiState(searchResult!!, false)
            }
        }
    }
}