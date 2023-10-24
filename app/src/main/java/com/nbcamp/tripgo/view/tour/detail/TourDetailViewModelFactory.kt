package com.nbcamp.tripgo.view.tour.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nbcamp.tripgo.data.repository.CalendarRepositoryImpl
import com.nbcamp.tripgo.data.repository.TourDetailRepositoryImpl
import com.nbcamp.tripgo.data.service.RetrofitModule

// viewModel에 생성자를 추가 해야할 때 만들어야 하는 viewModelFactory
class TourDetailViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(TourDetailViewModel::class.java)) {
            val tourDetailRepository = TourDetailRepositoryImpl(
                RetrofitModule.createTourApiService()
            )
            val calendarRepository = CalendarRepositoryImpl(
                context
            )
            return TourDetailViewModel(
                tourDetailRepository,
                calendarRepository
            ) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}
