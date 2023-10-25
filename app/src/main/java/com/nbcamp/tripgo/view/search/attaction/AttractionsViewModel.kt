package com.nbcamp.tripgo.view.search.attaction

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity
import com.nbcamp.tripgo.view.search.AttractionAdapter
import com.nbcamp.tripgo.view.search.SearchItemModel
import com.nbcamp.tripgo.view.search.SearchKeywordUiState
import com.nbcamp.tripgo.view.search.SearchRepository
import kotlinx.coroutines.launch

class AttractionsViewModel(
    private val searchRepository: SearchRepository
) : ViewModel() {
    lateinit var adapter: AttractionAdapter  // AttractionAdapter 변수를 ViewModel에 추가

    private val _searchUiState: MutableLiveData<SearchKeywordUiState> = MutableLiveData()
    val searchUiState: LiveData<SearchKeywordUiState>
        get() = _searchUiState

    // ViewModel의 fetchSearchResult 함수
    fun fetchSearchResult(keyword: String) {
        _searchUiState.value = SearchKeywordUiState.initialize()
        Log.d("키워드1", "값 = $keyword")

        viewModelScope.launch {
            runCatching {
                val searchResult = searchRepository.getPlaceBySearch(
                    keyword = keyword,
                    contentTypeId = "12",
                    responseCount = 20
                )
                println(searchResult)
                if (searchResult == null) {
                    _searchUiState.postValue(SearchKeywordUiState.error())
                } else {
                    _searchUiState.postValue(SearchKeywordUiState(searchResult))


                    // Assuming you have an adapter for displaying search results
//                    val searchItemModels = transformSearchResultToSearchItemModels(searchResult)

                    // Update the adapter's data
//                    adapter.items = ArrayList(searchItemModels) // Convert List to ArrayList
                }
            }
        }
    }


    private fun transformSearchResultToSearchItemModels(searchResult: List<KeywordSearchEntity>): List<SearchItemModel> {
        val searchItemModels = mutableListOf<SearchItemModel>()

        for (entity in searchResult) {
            val title = entity.title
            val url = entity.imageUrl
            val dateTime = "" // 빈 문자열 또는 다른 원하는 값을 사용할 수 있습니다
            val id = entity.contentId

            searchItemModels.add(SearchItemModel(title, url, dateTime, id))
        }

        return searchItemModels
    }
}