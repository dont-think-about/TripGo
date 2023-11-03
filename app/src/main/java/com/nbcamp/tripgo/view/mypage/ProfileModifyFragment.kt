package com.nbcamp.tripgo.view.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.R

class ProfileModifyFragment : Fragment() {
    private lateinit var refreshNickText: AppCompatEditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_modify, container, false)
        refreshNickText = view.findViewById(R.id.profile_edit_username)

        val saveButton = view.findViewById<AppCompatImageView>(R.id.profile_edit_complete_textivew)
        saveButton.setOnClickListener { updateNickname() }

        val backButton = view.findViewById<AppCompatImageView>(R.id.profile_back_imagebutton)
        backButton.setOnClickListener{ navigateToMyPageFragment() }


        return view
    }



    private fun updateNickname() {
        val editNickname = refreshNickText.text.toString()
        val firestore = FirebaseFirestore.getInstance()
        val userId = userEmail()
        val userRef = firestore.collection("users").document(userId)

        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    val currentNickname = document.getString("nickname")

                    checkIfNicknameExists(editNickname) { nicknameExists ->
                        if (!nicknameExists) {
                            val data = hashMapOf("nickname" to editNickname)

                            userRef.update(data as Map<String, Any>).addOnSuccessListener {
                                showToast("닉네임이 업데이트되었습니다.")
                                navigateToMyPageFragment()
                            }.addOnFailureListener {
                                showToast("닉네임 업데이트에 실패했습니다.")
                            }
                        } else {
                            showToast("닉네임이 이미 사용중입니다.")
                        }
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun userEmail(): String {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        return user?.email ?: ""
    }

    private fun checkIfNicknameExists(newNickname: String, callback: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val usersCollection = firestore.collection("users")

        usersCollection
            .whereEqualTo("nickname", newNickname)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    val nicknameExists = !querySnapshot.isEmpty
                    callback(nicknameExists)
                } else {
                    callback(false)
                }
            }
    }

    private fun navigateToMyPageFragment() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.main_fragment_container, MyPageFragment())
            .addToBackStack(null)
            .commit()
    }
}
