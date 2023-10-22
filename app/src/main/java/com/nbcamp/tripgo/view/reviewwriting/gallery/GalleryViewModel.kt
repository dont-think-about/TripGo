package com.nbcamp.tripgo.view.reviewwriting.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbcamp.tripgo.data.repository.model.GalleryPhotoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val galleryPhotoRepository: GalleryPhotoRepository
) : ViewModel() {

    private val _galleryState: MutableLiveData<GalleryUiState> = MutableLiveData()
    val galleryState: LiveData<GalleryUiState>
        get() = _galleryState

    // 불러온 사진들을 담을 리스트
    private lateinit var photoList: MutableList<GalleryPhotoEntity>

    // 리스트 불러오기
    fun fetchPhotos() {
        _galleryState.value = GalleryUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                photoList = galleryPhotoRepository.getAllPhotos()
                _galleryState.postValue(GalleryUiState.Success(photoList))
            }.onFailure {
                _galleryState.postValue(GalleryUiState.Error("갤러리에서 이미지를 불러오지 못했습니다."))
            }
        }
    }

    // 사진을 선택할 때, 리뷰 작성으로 보내기 위한 메소드
    fun selectPhoto(galleryPhoto: GalleryPhotoEntity) {
        val findGalleryPhoto = photoList.find { it.id == galleryPhoto.id }
        _galleryState.postValue(findGalleryPhoto?.let { GalleryUiState.PickPhoto(it) })
    }
}
