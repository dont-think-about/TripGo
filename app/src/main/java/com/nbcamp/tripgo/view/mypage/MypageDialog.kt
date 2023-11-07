package com.nbcamp.tripgo.view.mypage

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.view.App

class MyPageDialog(private val context: Context) {

    private var dialog: AlertDialog? = null

    fun show(fragmentManager: FragmentManager, dialogTag: String) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_mypage, null)
        builder.setView(dialogView)

        dialog = builder.create()
        dialog?.show()

        val dialogname = dialogView.findViewById<TextView>(R.id.mypage_dialog_signin_up_textview)
        val dialogemail =
            dialogView.findViewById<TextView>(R.id.mypage_dialog_sigin_up_inpo_textview)
        val countText = dialogView.findViewById<TextView>(R.id.mypage_dialog_total_count_textview)
        val auth = FirebaseAuth.getInstance()
        val firestoredb = FirebaseFirestore.getInstance()
        val user = auth.currentUser
        val userId = user?.email

        val progressBar = dialogView.findViewById<ProgressBar>(R.id.mypage_dialog_progressbar)
        val mygrade = dialogView.findViewById<TextView>(R.id.mypage_dialog_usergrade_textview)

        imageupdate()


        if (!userId.isNullOrBlank()) {
            getReviewCount(userId) { reviewCount ->
                val (remainCount, grade, maxProgress) = calculateGradeAndProgress(reviewCount)
                countText.text = "$reviewCount 개"
                mygrade.text = "내 등급 ($grade)"
                progressBar.max = maxProgress
                progressBar.progress = reviewCount

                val nextLevelRemainingCount = when (grade) {
                    "브론즈" -> 6 - reviewCount
                    "실버" -> 11 - reviewCount
                    "골드" -> 21 - reviewCount
                    else -> 0 // 다이아몬드는 0
                }

                val nextLevelRemainingCountTextView =
                    dialogView.findViewById<TextView>(R.id.mypage_dialog_next_grage_count_textview)
                nextLevelRemainingCountTextView.text = "$nextLevelRemainingCount 개"
            }
        }

        val mypagedata = getUserDocumentReference(userId, firestoredb)

        mypagedata?.get()
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val nickname = documentSnapshot.getString("nickname")
                    val email = documentSnapshot.getString("email")
                    if (!nickname.isNullOrBlank()) {
                        dialogname.text = "   $nickname 님"
                    }
                    if (!email.isNullOrBlank()) {
                        dialogemail.text = "   $email"
                    }
                }
            }
            ?.addOnFailureListener { e ->
                e.printStackTrace()
            }


        val editbutton = dialogView.findViewById<Button>(R.id.mypage_dialog_edit_userinpo_button)
        editbutton.setOnClickListener { userinfo(fragmentManager)  }
    }

    private fun userinfo(fragmentManager: FragmentManager){

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if( user != null ){
            val transaction = fragmentManager.beginTransaction()
            val newFragment = ProfileModifyFragment()
            transaction.replace(R.id.main_fragment_container, newFragment)
            transaction.addToBackStack(null)
            transaction.commit()
            dismissDialog()
        }
        else{
            Toast.makeText(context, "프로필 데이터가 없습니다.\n프로필을 먼저 설정해주세요.", Toast.LENGTH_SHORT).show()
        }

    }


    private fun calculateGradeAndProgress(reviewCount: Int): Triple<Int, String, Int> {
        return when {
            reviewCount <= 5 -> Triple(5 - reviewCount, "브론즈", 6)
            reviewCount <= 10 -> Triple(10 - reviewCount, "실버", 10)
            reviewCount <= 20 -> Triple(20 - reviewCount, "골드", 20)
            else -> Triple(0, "다이아몬드", 20)
        }
    }

    private fun getUserDocumentReference(
        userId: String?,
        firestoredb: FirebaseFirestore
    ): DocumentReference? {
        val kakaouser = App.kakaoUser?.email
        return userId?.let { firestoredb.collection("users").document(it) }
            ?: kakaouser?.let { firestoredb.collection("users").document(it) }
    }

    fun dismissDialog() {
        dialog?.dismiss()
    }

    private fun getReviewCount(userId: String?, callback: (Int) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = userId?.let { firestore.collection("users").document(it) }
        userRef?.get()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val reviewCount = document.getLong("reviewCount")
                        if (reviewCount != null) {
                            callback(reviewCount.toInt())
                        } else {
                            callback(0)
                        }
                    } else {
                        callback(0)
                    }
                }
            }
    }

    private fun imageupdate() {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val userEmail = user?.email
        val userRef = firestore.collection("users").document(userEmail.toString())


        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    val profileImageUrl = document.getString("profileImageUrl")
                    if (!profileImageUrl.isNullOrEmpty()) {

                        val dialogimageView =
                            dialog?.findViewById<ImageView>(R.id.mypage_dialog_user_imageview)

                        Log.d("MYpageurl", profileImageUrl)

                        dialogimageView?.load(profileImageUrl) {
                            transformations(CircleCropTransformation())
                        }
                    }
                }
            }
        }
    }
}
