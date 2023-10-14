package com.nbcamp.tripgo.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.data.model.ExamResponseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {
    private val _information: MutableLiveData<ExamResponseModel> = MutableLiveData()
    val information: LiveData<ExamResponseModel>
        get() = _information

    fun getInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = homeRepository.getInfo()
            _information.postValue(response)
        }
    }
}
