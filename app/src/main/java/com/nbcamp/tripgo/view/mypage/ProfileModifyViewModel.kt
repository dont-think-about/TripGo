package com.nbcamp.tripgo.view.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.user.UserApiClient
import com.nbcamp.tripgo.view.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileModifyViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val firebaseStore = FirebaseFirestore.getInstance()

    private val _deleteStatus: MutableLiveData<UserDeleteStatus> = MutableLiveData()
    val deleteStatus: LiveData<UserDeleteStatus>
        get() = _deleteStatus

    fun withDrawlUser() {
        _deleteStatus.value = UserDeleteStatus.Initialize
        runCatching {
            if (App.kakaoUser == null) {
                // auth 회원 탈퇴
                auth.currentUser?.delete()
                    ?.addOnCompleteListener {
                        println("result: " + it.result)
                        if (it.isSuccessful) {
                            println("success: " + it.result)
                            App.firebaseUser?.email?.let { email ->
                                viewModelScope.launch(Dispatchers.IO) {
                                    deleteUserInfo(email)
                                }
                            }
                        } else {
                            println("fail: "+it.result)
                            // 탈퇴 실패
                            _deleteStatus.postValue(UserDeleteStatus.Error("삭제 실패"))
                        }
                    }
            } else {
                // 카카오 회원 탈퇴
                UserApiClient.instance.unlink { error ->
                    if(error != null) {
                        App.kakaoUser?.email?.let { email ->
                            viewModelScope.launch(Dispatchers.IO) {
                                deleteUserInfo(email)
                            }
                        }
                    } else {
                        // 탈퇴 실패
                        _deleteStatus.postValue(UserDeleteStatus.Error("삭제 실패"))
                    }
                }
            }
        }.onFailure {
            _deleteStatus.postValue(UserDeleteStatus.Error("삭제 실패"))
        }
    }

    private suspend fun deleteUserInfo(email: String) {
        println("delete: $email")
        firebaseStore.runTransaction {
            firebaseStore.collection("users").document(email)
                .delete()
            _deleteStatus.postValue(UserDeleteStatus.Success("유저 정보 삭제 완료"))
            firebaseStore.collection("reviews").document(email)
                .delete()
            _deleteStatus.postValue(UserDeleteStatus.Success("리뷰 삭제 완료"))
            firebaseStore.collection("calendar").document(email)
                .delete()
            _deleteStatus.postValue(UserDeleteStatus.Success("일정 삭제 완료"))
            firebaseStore.collection("liked").document(email)
                .delete()
            _deleteStatus.postValue(UserDeleteStatus.Success("좋아요 정보 삭제 완료"))

            storage.reference.child("reviews").child(email).delete()
            storage.reference.child("users").child(email).delete()
        }.await()

        App.kakaoUser = null
        App.firebaseUser = null

        _deleteStatus.postValue(UserDeleteStatus.Success("모든 정보 삭제 완료"))
    }
}

