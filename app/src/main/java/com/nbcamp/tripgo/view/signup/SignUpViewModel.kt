package com.nbcamp.tripgo.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class SignUpViewModel : ViewModel() {
    var firebaseAuth: FirebaseAuth = Firebase.auth
    var fireStore: FirebaseFirestore = Firebase.firestore

    var email: MutableLiveData<String> = MutableLiveData("")
    var password: MutableLiveData<String> = MutableLiveData("")
    var passwordCorrect: MutableLiveData<String> = MutableLiveData("")
    var nickname: MutableLiveData<String> = MutableLiveData("")
    var signUpButton: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _isEmailRegistered = MutableLiveData<Boolean>()
    val isEmailRegistered: LiveData<Boolean> get() = _isEmailRegistered

    private val _isNickNameRegistered = MutableLiveData<Boolean>()
    val isNickNameRegistered: LiveData<Boolean> get() = _isNickNameRegistered

    fun signUpComplete() {
        val email = email.value.toString()
        val password = password.value.toString()
        val passwordCorrect = passwordCorrect.value.toString()
        val nickname = nickname.value.toString()

        if (email.trim().isNotEmpty() && password.trim().isNotEmpty() &&
            nickname.trim().isNotEmpty() && passwordCorrect.trim().isNotEmpty() && password == passwordCorrect
        ) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    signUpButton.value = true

                    val firebaseUID = FirebaseAuth.getInstance().currentUser?.uid
                    if (firebaseUID != null) {
                        val user = hashMapOf(
                            "email" to email,
                            "nickname" to nickname,
                            "profileImage" to null,
                            "reviewCount" to 0
                        )
                        // 자체 로그인 firestore 저장부분  users -> email -> email,nickname,image
                        fireStore.collection("users").document(email).set(user)
                        firebaseAuth.currentUser?.sendEmailVerification()
                    } else {
                        // 아이디가 있을 경우에! 로직 추가 ->다음 작업 시 진행
                    }
                }
            }
        }
    }

    fun checkEmailDuplication(email: String) {
        fireStore.collection("users").whereEqualTo("email", email)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result

                    if (result != null && !result.isEmpty) {
                        _isEmailRegistered.value = false
                    } else {
                        _isEmailRegistered.value = true
                    }
                }
            }
            .addOnFailureListener { exception ->

            }
    }

    fun checkNickNameDuplication(nickname: String) {
        fireStore.collection("users").whereEqualTo("nickname", nickname).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result

                if (result != null && !result.isEmpty) {
                    _isNickNameRegistered.value = false
                } else {
                    _isNickNameRegistered.value = true
                }
            }
        }.addOnFailureListener { exception ->
            // 에러 처리
        }
    }
}
