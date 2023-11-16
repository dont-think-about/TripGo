package com.nbcamp.tripgo.view.search.tour

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nbcamp.tripgo.data.repository.SearchRepositoryImpl
import com.nbcamp.tripgo.data.service.RetrofitModule

// viewModel에 생성자를 추가 해야할 때 만들어야 하는 viewModelFactory
class TourViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(TourViewModel::class.java)) {
            val repository = SearchRepositoryImpl(
                RetrofitModule.createTourApiService(),
            )
            TourViewModel(repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}
