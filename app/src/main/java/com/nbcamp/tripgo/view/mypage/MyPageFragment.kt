package com.nbcamp.tripgo.view.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.login.LogInActivity
import com.nbcamp.tripgo.view.mypage.favorite.FavoriteFragment
import com.nbcamp.tripgo.view.review.mypage.ReviewFragment

class MyPageFragment : Fragment() {

    private var dbinpo: DocumentReference? = null

    private val dialogTag = "MyDialog"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_page, container, false)


        val reviewLayout = view.findViewById<LinearLayout>(R.id.review_layout)
        val zzimLayout = view.findViewById<LinearLayout>(R.id.mypage_zzim_layout)
        val logoutButton = view.findViewById<Button>(R.id.mypage_logout_button)
        val userLayout = view.findViewById<LinearLayout>(R.id.mypage_userlayout)


        reviewLayout.setOnClickListener { navigateToFragment(ReviewFragment()) }
        zzimLayout.setOnClickListener { navigateToFragment(FavoriteFragment()) }
        logoutButton.setOnClickListener { logout() }
        userLayout.setOnClickListener { showUserDialog() }

        // Firebase 정보 가져오고 UI 업데이트
        fetchFirebaseDataAndUIUpdate(view)

        return view
    }

    private fun fetchFirebaseDataAndUIUpdate(view: View) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val userEmail = user?.email
        val firestoredb = FirebaseFirestore.getInstance()

        val kakaouser = App.kakaoUser?.email

        val emailText = view.findViewById<TextView>(R.id.mypage_signin_up_inpo)
        val nicknameText = view.findViewById<TextView>(R.id.mypage_signin_up_text)

        dbinpo = when{
            kakaouser == null -> firestoredb.collection("users")?.document(userEmail.toString())
            kakaouser != null -> firestoredb.collection("users")?.document(kakaouser.toString())
        else ->{
            return
            }
        }


        dbinpo?.get()
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userdata = documentSnapshot.data
                    val email = userdata?.get("email") as? String
                    val nickname = userdata?.get("nickname") as? String

                    nicknameText.text = nickname?.let { "   $it 님" } ?: ""
                    emailText.text = email?.let { "   $it" } ?: ""
                } else {
                    Log.d(TAG, "document를 찾을 수 없습니다.")
                }
            }
            ?.addOnFailureListener { e ->
                Log.d(TAG, "실 패 ! $e")
            }
    }

    private fun navigateToFragment(fragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun logout() {
        App.firebaseUser = null
        App.kakaoUser = null
        val intent = Intent(context, LogInActivity::class.java)
        startActivity(intent)
    }

    private fun showUserDialog() {
        val myPageDialog = MyPageDialog(requireContext())
        myPageDialog.show(parentFragmentManager, dialogTag)
    }

    companion object {
        const val TAG = "MY_PAGE_FRAGMENT"

        fun newInstance() = MyPageFragment()
    }
}
