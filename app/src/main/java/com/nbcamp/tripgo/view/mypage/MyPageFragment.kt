package com.nbcamp.tripgo.view.mypage

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.login.LogInActivity
import com.nbcamp.tripgo.view.mypage.favorite.FavoriteFragment
import com.nbcamp.tripgo.view.mypage.favorite.MypageAppInpo
import com.nbcamp.tripgo.view.review.mypage.ReviewFragment
import com.nbcamp.tripgo.view.signup.RulesFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MyPageFragment : Fragment() {

    private val viewModel: MyPageViewModel by viewModels()
    private val dialogTag = "MyDialog"
    private lateinit var emailText: TextView
    private lateinit var nicknameText: TextView
    private lateinit var loadingDialog: LoadingDialog


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_page, container, false)
        loadingDialog = LoadingDialog(requireActivity())
        emailText = view.findViewById(R.id.mypage_signin_up_inpo)
        nicknameText = view.findViewById(R.id.mypage_signin_up_text)

        val reviewLayout = view.findViewById<LinearLayout>(R.id.review_layout)
        val zzimLayout = view.findViewById<LinearLayout>(R.id.mypage_zzim_layout)
        val userLayout = view.findViewById<LinearLayout>(R.id.mypage_userlayout)
        val openSourceLicenseTextView = view.findViewById<TextView>(R.id.mypage_opensource_textview)
        val appInpo = view.findViewById<TextView>(R.id.mypage_appinpo_textview)
        reviewLayout.setOnClickListener { navigateToFragment(ReviewFragment()) }
        zzimLayout.setOnClickListener { navigateToFragment(FavoriteFragment()) }
        val userInfoTextView = view.findViewById<TextView>(R.id.mypage_userinpo_textview)
        userInfoTextView.setOnClickListener {
            showFullDialog("privacy")
        }
        val questionTextView = view.findViewById<TextView>(R.id.mypage_question_textview)
        questionTextView.setOnClickListener {
            requireContext().toast("추후 업데이트 예정 입니다")
        }

        val auth = FirebaseAuth.getInstance()
        userLayout.setOnClickListener {
            if (auth.currentUser != null) {
                showUserDialog()
            } else {
                loading()
                startActivity(Intent(requireContext(), LogInActivity::class.java))
            }
        }
        openSourceLicenseTextView.setOnClickListener { runOpenSourceDialog() }
        appInpo.setOnClickListener {
            val appinfodialog = MypageAppInpo()
            appinfodialog.show(parentFragmentManager, "app_info_dialog")
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if (UserLoggedIn()) {
            userinpo()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun UserLoggedIn(): Boolean {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        return currentUser != null
    }

    private fun userinpo() {
        loadingDialog.run {
            setVisible()
            setText("로딩중 ...")
        }
        viewModel.email.observe(viewLifecycleOwner) { email ->
            emailText.text = "   $email"
            checkAndDismissLoadingDialog()
        }
        viewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            nicknameText.text = "   $nickname 님"
            checkAndDismissLoadingDialog()
        }
        viewModel.fetchDataFromFirebase()
        imageupdate()
    }

    private fun checkAndDismissLoadingDialog() {
        // email과 nickname이 모두 채워졌는지 확인
        if (emailText.text.isNotBlank() && nicknameText.text.isNotBlank()) {
            loadingDialog.setInvisible()
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
                        val imageView = view?.findViewById<AppCompatImageView>(R.id.mypage_usericon)
                        imageView?.load(profileImageUrl) {
                            transformations(CircleCropTransformation())
                            listener(onSuccess = { _, _ ->
                                // 이미지 로드가 성공한 경우, 로딩 화면을 숨깁니다.
                                checkAndDismissLoadingDialog()
                            })
                        }
                    }
                }
            }
        }
    }

    private fun loading() {
        loadingDialog.run {
            setVisible()
            setText("로딩중 ... ")
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(5500) //
            loadingDialog.setInvisible() // 로딩 화면 숨기기
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
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

    fun showFullDialog(data: String) {
        val dialogFragment = RulesFragment()
        val args = Bundle()
        args.putString("data", data)
        dialogFragment.arguments = args
        dialogFragment.show(parentFragmentManager, null)
    }

    companion object {
        const val TAG = "MY_PAGE_FRAGMENT"

        fun newInstance() = MyPageFragment()
    }
}
