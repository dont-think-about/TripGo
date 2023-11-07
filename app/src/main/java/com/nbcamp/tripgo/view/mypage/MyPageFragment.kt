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
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.login.LogInActivity
import com.nbcamp.tripgo.view.mypage.favorite.FavoriteFragment
import com.nbcamp.tripgo.view.mypage.favorite.MypageAppInpo
import com.nbcamp.tripgo.view.review.mypage.ReviewFragment



class MyPageFragment : Fragment() {

    private var dbinpo: DocumentReference? = null

    private val dialogTag = "MyDialog"

    private lateinit var emailText: TextView
    private lateinit var nicknameText: TextView



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_page, container, false)

        emailText = view.findViewById(R.id.mypage_signin_up_inpo)
        nicknameText= view.findViewById(R.id.mypage_signin_up_text)





        fetchFirebaseDataAndUIUpdate(view)


        val reviewLayout = view.findViewById<LinearLayout>(R.id.review_layout)
        val zzimLayout = view.findViewById<LinearLayout>(R.id.mypage_zzim_layout)
        val logoutButton = view.findViewById<Button>(R.id.mypage_logout_button)
        val userLayout = view.findViewById<LinearLayout>(R.id.mypage_userlayout)
        val openSourceLicenseTextView = view.findViewById<TextView>(R.id.mypage_opensource_textview)
        val appInpo = view.findViewById<TextView>(R.id.mypage_appinpo_textview)
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser


        reviewLayout.setOnClickListener { navigateToFragment(ReviewFragment()) }
        zzimLayout.setOnClickListener { navigateToFragment(FavoriteFragment()) }
        logoutButton.setOnClickListener { logout() }

        userLayout.setOnClickListener {
            if (user!=null){
                showUserDialog()
            }else{
                startActivity(Intent(requireContext(), LogInActivity::class.java))
            }
        }

        openSourceLicenseTextView.setOnClickListener { runOpenSourceDialog() }
        appInpo.setOnClickListener {
            val appinfodialog = MypageAppInpo()
            appinfodialog.show(parentFragmentManager, "app_info_dialog")
        }

        fetchFirebaseDataAndUIUpdate(view)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val email = arguments?.getString("email")
        val nickname = arguments?.getString("nickname")
        emailText?.text = email
        nicknameText?.text = nickname
        Log.d("MYPAGELELEL",email.toString())
        Log.d("MYPAGELELEL",nickname.toString())



        // Firebase 정보 가져오고 UI 업데이트

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

                        imageView?.load(profileImageUrl) {
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

        Log.d("mypageemail", userEmail.toString())

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
                    Log.d("MYPAGEDBINPO", email.toString())
                    val nickname = userdata?.get("nickname") as? String
                    Log.d("MYPAGEDBINPO", nickname.toString())

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

    private fun changetextbutton() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val logoutButton = view?.findViewById<Button>(R.id.mypage_logout_button)
        val withdrawalButton = view?.findViewById<TextView>(R.id.mypage_withdrawal_textivew)
        val kakaouser = App.kakaoUser?.email
        if (user != null || kakaouser != null) {
            logoutButton?.visibility = View.VISIBLE
            withdrawalButton?.visibility = View.VISIBLE
        } else {
            logoutButton?.visibility = View.GONE
            withdrawalButton?.visibility = View.GONE
        }

    }

    private fun navigateToFragment(fragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun logout() {
        val progressBar = view?.findViewById<ProgressBar>(R.id.mypage_progressBar)  // 프로그래스 바
        val logoutButton = view?.findViewById<Button>(R.id.mypage_logout_button)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val kakaouser = App.kakaoUser?.email


        if (user != null || kakaouser != null) {
            // 사용자가 로그인한 상태이면 로그아웃 처리

            progressBar?.visibility = View.VISIBLE
            lifecycleScope.launch {
                delay(2000)  //  대기
                logoutButton?.text = "로그인"
                updateUIAfterLogout()
                // 일정 시간 후에 프로그래스 바를 숨기고 화면을 전환
                progressBar?.visibility = View.GONE

            }

            if (kakaouser != null) {
                // 카카오 로그아웃
                UserApiClient.instance.logout { error ->
                    if (error != null) {
                        Log.e("MYPAGEFRAGMENT", "I'm kakao user but failed : $error")
                    } else {
                        App.kakaoUser = null
                        // 로그아웃 후 화면 갱신
                        logoutButton?.text = "로그인"
                        updateUIAfterLogout()
                    }
                }
            } else {
                // Firebase Auth 로그아웃
                FirebaseAuth.getInstance().signOut()
                App.firebaseUser = null
                // 로그아웃 후 화면 갱신
                logoutButton?.text = "로그인"
                updateUIAfterLogout()
            }
        } else {
            // 사용자가 로그인하지 않은 상태이면 로그인 화면으로 이동
            val intent = Intent(requireContext(), LogInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateUIAfterLogout() {
        val emailText = view?.findViewById<TextView>(R.id.mypage_signin_up_inpo)
        val nicknameText = view?.findViewById<TextView>(R.id.mypage_signin_up_text)
        val logoutbutton = view?.findViewById<Button>(R.id.mypage_logout_button)

        nicknameText?.text = getString(R.string.mypage_signin_up)
        emailText?.text = getString(R.string.mypage_signin_up_inpor)
        logoutbutton?.text = getString(R.string.mypage_logout)

        // 다른 로그아웃 관련 작업을 수행할 수 있으면 여기에 추가
    }



    private fun showUserDialog() {
        val myPageDialog = MyPageDialog(requireContext())
        myPageDialog.show(parentFragmentManager, dialogTag)
    }

    private fun runOpenSourceDialog() {
        AlertDialog.Builder(requireActivity())
            .setView(R.layout.dialog_opensource)
            .setTitle("오픈소스 라이센스")
            .setPositiveButton("확인") { _, _ -> }
            .create()
            .show()
    }

    companion object {
        const val TAG = "MY_PAGE_FRAGMENT"

        fun newInstance() = MyPageFragment()
    }
}