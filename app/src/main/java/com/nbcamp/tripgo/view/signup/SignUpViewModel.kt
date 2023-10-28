package com.nbcamp.tripgo.view.signup

import android.content.ContentValues
import android.util.Log
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
    var nickname: MutableLiveData<String> = MutableLiveData("")
    var signUpButton: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _isEmailRegistered = MutableLiveData<Boolean>()
    val isEmailRegistered: LiveData<Boolean> get() = _isEmailRegistered

    private val _isNickNameRegistered = MutableLiveData<Boolean>()
    val isNickNameRegistered: LiveData<Boolean> get() = _isNickNameRegistered

    fun signUpComplete() {
        val email = email.value.toString()
        val password = password.value.toString()
        val nickname = nickname.value.toString()

        if (email.trim().isNotEmpty() && password.trim().isNotEmpty() && nickname.trim()
                .isNotEmpty()
        ) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {

                    signUpButton.value = true

                    // 자체 로그인 firestore 저장부분  users -> email -> uid,nickname,image
                    val firebaseUID = FirebaseAuth.getInstance().currentUser?.uid

                    if (firebaseUID != null) {

                        val user = hashMapOf(
                            "email" to email,
                            "nickname" to nickname,
                            "profileImage" to null,
                        )

                    // 자체 로그인 firestore 저장부분  users -> email -> email,nickname,image
                        fireStore.collection("users").document(email).set(user)
                        firebaseAuth.currentUser?.sendEmailVerification()

                        Log.d(ContentValues.TAG, "createUserWithEmail:success")

                    } else {
                        //아이디가 있을 경우에! 로직 추가 ->다음 작업 시 진행
                        Log.w(ContentValues.TAG, "createUserWithEmail:failure", it.exception)
                    }
                }

            }
        }
    }

    fun checkEmailDuplication(email: String) {

        fireStore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.id == email) {
                        Log.d("ContentValues123123123", "아이디가 중복이다아")
                        _isEmailRegistered.value = false
                    } else {
                        _isEmailRegistered.value = true
                    }
                    Log.d(ContentValues.TAG, "${document.id}=${email}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }

    }

    fun checkNickNameDuplication(nickname: String) {

        fireStore.collection("users").get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                val fieldValue = document.getString("nickname")

                if (fieldValue != null) {
                    if (fieldValue.toString() == nickname) {
                        _isNickNameRegistered.value = false
                        Log.d("Firestore", "값이 이따")
                    } else {
                        _isNickNameRegistered.value = true
                        Log.d("Firestore", "값이 다르따")
                    }
                }
            }
        }.addOnFailureListener { exception ->
            // 에러 처리
            Log.d(ContentValues.TAG, "Error getting documents: ", exception)
        }


    }


}