package com.nbcamp.tripgo.view.mypage

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.view.App

class MyPageViewModel : ViewModel() {
    private val firestoredb = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val isKakaoUser = App.kakaoUser != null

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _nickname = MutableLiveData<String>()
    val nickname: LiveData<String> get() = _nickname
    init {
        // 초기화 블록 내에서 데이터를 가져올 수 있음
        fetchDataFromFirebase()
    }
    fun fetchDataFromFirebase() {
        // Firestore 데이터를 가져올 대상 이메일 주소 설정
        val targetEmail = if (isKakaoUser) {
            App.kakaoUser?.email
        } else {
            currentUser?.email
        }
        if (targetEmail != null) {
            val dbinpo = firestoredb.collection("users").document(targetEmail)
            dbinpo.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val email = document.data?.get("email") as? String
                        val nickname = document.data?.get("nickname") as? String

                        _nickname.value = nickname!!
                        _email.value = email!!
                    } else {
                        Log.d("Mypagefail", "document를 찾을 수 없습니다.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "실패! $e")
                }
        }
    }
}
