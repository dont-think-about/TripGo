package com.nbcamp.tripgo.view.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.login.LogInActivity
import com.nbcamp.tripgo.view.mypage.favorite.FavoriteFragment
import com.nbcamp.tripgo.view.mypage.favorite.MypageAppInpo
import com.nbcamp.tripgo.view.review.mypage.ReviewFragment
import org.w3c.dom.Text

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
        val openSourceLicenseTextView = view.findViewById<TextView>(R.id.mypage_opensource_textview)
        val appInpo = view.findViewById<TextView>(R.id.mypage_appinpo_textview)


        reviewLayout.setOnClickListener { navigateToFragment(ReviewFragment()) }
        zzimLayout.setOnClickListener { navigateToFragment(FavoriteFragment()) }
        logoutButton.setOnClickListener { logout() }
        userLayout.setOnClickListener { showUserDialog() }
        openSourceLicenseTextView.setOnClickListener { runOpenSourceDialog() }
        appInpo.setOnClickListener {
            val appinfodialog = MypageAppInpo()
            appinfodialog.show(parentFragmentManager, "app_info_dialog")
        }


        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firebase 정보 가져오고 UI 업데이트
        fetchFirebaseDataAndUIUpdate(view)

        imageupdate()

        changetextbutton()
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

                        val imageView = view?.findViewById<ImageView>(R.id.mypage_usericon)

                        Log.d("MYpageurl", profileImageUrl)

                        imageView?.load(profileImageUrl){
                            transformations(CircleCropTransformation())
                        }
                    }
                }
            }
        }
    }


    private fun fetchFirebaseDataAndUIUpdate(view: View) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val userEmail = user?.email
        val firestoredb = FirebaseFirestore.getInstance()

        Log.d("mypageemail" ,userEmail.toString())

        val kakaouser = App.kakaoUser?.email

        val emailText = view.findViewById<TextView>(R.id.mypage_signin_up_inpo)
        val nicknameText = view.findViewById<TextView>(R.id.mypage_signin_up_text)

        dbinpo = when {
            kakaouser == null -> firestoredb.collection("users").document(userEmail.toString())
            kakaouser != null -> firestoredb.collection("users").document(kakaouser.toString())
            else -> {
                return
            }
        }

        dbinpo?.get()
            ?.addOnSuccessListener { document ->
                if (document.exists()) {
                    val userdata = document.data
                    val email = userdata?.get("email") as? String
                    Log.d("MYPAGEDBINPO",email.toString())
                    val nickname = userdata?.get("nickname") as? String
                    Log.d("MYPAGEDBINPO",nickname.toString())

                    nicknameText.text = nickname?.let { "   $it 님" } ?: ""
                    emailText.text = email?.let { "   $it" } ?: ""
                } else {
                    Log.d("Mypagefail", "document를 찾을 수 없습니다.")
                }
            }
            ?.addOnFailureListener { e ->
                Log.d(TAG, "실 패 ! $e")
            }
    }

    private fun changetextbutton(){
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val logoutButton = view?.findViewById<Button>(R.id.mypage_logout_button)
        val kakaouser = App.kakaoUser?.email
        if(user != null || kakaouser != null ){
            logoutButton?.text = "로그아웃"
        }

        else{
            logoutButton?.text = "로그인"
        }

    }

    private fun navigateToFragment(fragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        App.firebaseUser = null
        App.kakaoUser = null
        val intent = Intent(context, LogInActivity::class.java)
        startActivity(intent)
    }

    private fun showUserDialog() {
        val myPageDialog = MyPageDialog(requireContext())
        myPageDialog.show(parentFragmentManager, dialogTag)
    }

    private fun runOpenSourceDialog() {
        AlertDialog.Builder(requireActivity())
            .setView(R.layout.dialog_opensource)
            .setTitle("오픈소스 라이센스")
            .setPositiveButton("확인"){_, _ ->}
            .create()
            .show()
    }

    companion object {
        const val TAG = "MY_PAGE_FRAGMENT"

        fun newInstance() = MyPageFragment()
    }
}
