package com.nbcamp.tripgo.view.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nbcamp.tripgo.data.repository.CalendarRepositoryImpl

// viewModel에 생성자를 추가 해야할 때 만들어야 하는 viewModelFactory
class CalendarViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            val repository = CalendarRepositoryImpl(context)
            CalendarViewModel(repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}
