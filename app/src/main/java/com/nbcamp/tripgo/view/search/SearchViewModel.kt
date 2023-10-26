package com.nbcamp.tripgo.view.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity

class SearchViewModel() : ViewModel() {
    private val _pullData: MutableLiveData<List<KeywordSearchEntity>> = MutableLiveData()
    val pullData: LiveData<List<KeywordSearchEntity>>
        get() = _pullData

    fun sendSearchData(list: List<KeywordSearchEntity>) {
        _pullData.value = list
    }
}