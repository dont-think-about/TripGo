package com.nbcamp.tripgo.view.review.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReviewDetailViewModel(
    private val reviewDetailRepository: ReviewDetailRepository
): ViewModel() {
    private val _userStatus: MutableLiveData<UserStatus?> = MutableLiveData()
    val userStatus: LiveData<UserStatus?>
        get() = _userStatus

    fun getUserStatus(userNickName: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val userStatus = reviewDetailRepository.getUserStatus(userNickName)
                _userStatus.postValue(userStatus)
            }.onFailure {
                _userStatus.postValue(null)
            }
        }
    }

}
