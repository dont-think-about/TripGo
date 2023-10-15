package com.nbcamp.tripgo.view.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nbcamp.tripgo.data.repository.ExamRepositoryImpl
import com.nbcamp.tripgo.data.service.RetrofitModule

// viewModel에 생성자를 추가 해야할 때 만들어야 하는 viewModelFactory
class HomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val repository = ExamRepositoryImpl(RetrofitModule.create())
            return HomeViewModel(repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}
