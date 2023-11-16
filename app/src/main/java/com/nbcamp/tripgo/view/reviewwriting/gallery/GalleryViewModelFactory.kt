package com.nbcamp.tripgo.view.reviewwriting.gallery

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nbcamp.tripgo.data.repository.GalleryRepositoryImpl

// viewModel에 생성자를 추가 해야할 때 만들어야 하는 viewModelFactory
class GalleryViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            val repository = GalleryRepositoryImpl(context)
            GalleryViewModel(repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}
