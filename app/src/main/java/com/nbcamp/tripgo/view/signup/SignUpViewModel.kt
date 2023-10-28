package com.nbcamp.tripgo.view.signup

import android.content.ContentValues
import android.util.Log
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

                        Log.d(ContentValues.TAG, "createUserWithEmail:success")

                    } else {
                        //아이디가 있을 경우에! 로직 추가 ->다음 작업 시 진행
                        Log.w(ContentValues.TAG, "createUserWithEmail:failure", it.exception)
                    }
                }

            }
        }
    }
}