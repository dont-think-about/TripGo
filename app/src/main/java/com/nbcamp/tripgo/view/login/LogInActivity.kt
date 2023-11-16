package com.nbcamp.tripgo.view.login

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.nbcamp.tripgo.BuildConfig
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivityLogInBinding
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.main.MainActivity
import com.nbcamp.tripgo.view.mypage.MyPageFragment
import com.nbcamp.tripgo.view.signup.SignUpActivity

class LogInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailid: AppCompatEditText
    private lateinit var emailpwd: AppCompatEditText
    private lateinit var loginbtn: AppCompatButton
    private lateinit var kakaoLoginButton: AppCompatImageView
    private val firestore: FirebaseFirestore = Firebase.firestore

    // Google login
    private val binding by lazy { ActivityLogInBinding.inflate(layoutInflater) }
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var startGoogleLoginForResult: ActivityResultLauncher<Intent>

    // loading Dialog
    private lateinit var loadingDialog: LoadingDialog

    companion object {
        const val TAG = "LogInActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)

        loadingDialog.setInvisible() // 로딩 화면 숨기기

        initializeViews()
        setupListeners()
        configureGoogleLogin()
        configureKakaoLogin()

        val snackbarMessage = intent.getStringExtra("snackbarMessage")
        if (snackbarMessage != null) {
            Snackbar.make(binding.root, snackbarMessage, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun initializeViews() {
        auth = FirebaseAuth.getInstance()
        emailid = findViewById(R.id.sign_up_email_edit_text)
        emailpwd = findViewById(R.id.log_in_password_edit_text)
        loginbtn = findViewById(R.id.log_in_login_button)
        kakaoLoginButton = findViewById(R.id.log_in_kakao_login_button)
    }

    private fun setupListeners() {
        loginbtn.setOnClickListener {
            handleLoginButtonClick()
        }

        binding.logInGoogleLoginButton.setOnClickListener {
            startGoogleSignIn()
        }

        binding.logInSignUpTextView.setOnClickListener {
            navigateToSignUp()
        }

        binding.logInFindPasswordTextView.setOnClickListener {
            showPasswordFindFragment()
        }
    }

    private fun handleLoginButtonClick() {
        val email = emailid.text.toString()
        val password = emailpwd.text.toString()

        when {
            email.isBlank() -> Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            password.isBlank() -> Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            else -> {
                showLoadingDialog() // ProgressBar 표시
                login(email, password)
            }
        }
    }

    private fun showLoadingDialog() {
        loadingDialog.run {
            setVisible()
            setText("로딩중...")
        }
    }

    private fun hideLoadingDialog() {
        loadingDialog.setInvisible()
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { authResult ->
                if (authResult.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        hideLoadingDialog()
                        val intent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        hideLoadingDialog()
                        toast("이메일 인증을 완료해 주세요")
                    }
                } else {
                    hideLoadingDialog()
                    Toast.makeText(this, "로그인 실패.", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "로그인 실패: ${authResult.exception}")
                    authResult.exception?.localizedMessage?.let { Log.d(TAG, it) }
                }
            }
    }

    private fun configureGoogleLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        startGoogleLoginForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                loadingDialog.setVisible()
                loadingDialog.setText("로그인 중..")
                if (result.resultCode == RESULT_OK) {
                    result.data?.let { data ->
                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                        try {
                            val account = task.getResult(ApiException::class.java)!!
                            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                            firebaseAuthWithGoogle(account.idToken!!)
                            startGoogleSignIn()
                            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                            auth.signInWithCredential(credential)
                                .addOnCompleteListener { authResult ->
                                    if (authResult.isSuccessful) {
                                        val user = auth.currentUser
                                        if (user != null) {
                                            val email = user.email
                                            val nickname = user.displayName

                                            // Firestore에서 해당 이메일이 이미 존재하는지 확인
                                            firestore.collection("users").document(email.toString())
                                                .get()
                                                .addOnSuccessListener { documentSnapshot ->
                                                    if (documentSnapshot.exists()) {
                                                        // 이미 존재하는 경우
                                                        Toast.makeText(
                                                            this,
                                                            "기존 회원으로 로그인합니다.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        loadingDialog.setInvisible()
                                                        finish()
                                                    } else {
                                                        // 존재하지 않는 경우, Firestore에 새로운 사용자 정보 추가
                                                        val userDocument = hashMapOf(
                                                            "email" to email,
                                                            "nickname" to nickname,
                                                            "profileImage" to user.photoUrl,
                                                            "reviewCount" to 0
                                                        )
                                                        firestore.collection("users").document(email.toString())
                                                            .set(userDocument)
                                                            .addOnSuccessListener {
                                                                // 새로운 사용자 정보가 Firestore에 추가된 경우
                                                                loadingDialog.setInvisible()
                                                                finish()
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Log.w(TAG, "Firestore에 사용자 정보 저장 실패", e)
                                                            }
                                                    }
                                                }
                                        }
                                    } else {
                                        Log.w(TAG, "Firebase에 Google 로그인 실패", authResult.exception)
                                    }
                                }
                        } catch (e: ApiException) {
                            Log.w(TAG, "Google sign in failed", e)
                        }
                    }
                } else {
                    Log.e(TAG, "Google Result Error $result")
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun configureKakaoLogin() {
        KakaoSdk.init(this, BuildConfig.KAKAO_API_KEY)
        kakaoLoginButton.setOnClickListener {
            kakaoLogin()
        }
    }

    private fun kakaoLogin() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.d("Kakao Login Failure", "Kakao login failed: $error")
                setKakaoLoginButtonVisible(true)
            } else if (token != null) {
                loadingDialog.setVisible()
                loadingDialog.setText("로그인 중..")
                UserApiClient.instance.me { user, userError ->
                    if (userError != null) {
                        Log.e("Error fetching user info", userError.toString())
                    } else if (user != null) {
                        val email = user.kakaoAccount?.email
                        val nickname = user.kakaoAccount?.profile?.nickname

                        if (email != null) {
                            val userDocument = hashMapOf(
                                "email" to email,
                                "nickname" to nickname,
                                "profileImage" to user.kakaoAccount?.profile?.profileImageUrl,
                                "reviewCount" to 0
                            )
                            firestore.collection("users").document(email)
                                .set(userDocument)
                                .addOnSuccessListener {
                                    Log.d(ContentValues.TAG, "사용자 정보 Firestore에 저장 성공")
                                    loadingDialog.setInvisible()
                                    val intent = Intent(this, MainActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    }
                                    onLoginSuccess(email, nickname.toString())
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(ContentValues.TAG, "사용자 정보 Firestore에 저장 실패", e)
                                }
                        }
                    }
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun setKakaoLoginButtonVisible(visible: Boolean) {
        kakaoLoginButton.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startGoogleLoginForResult.launch(signInIntent)
    }

    private fun showPasswordFindFragment() {
        val fragment = PasswordFindFragment()
        fragment.show(supportFragmentManager, null)
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun onLoginSuccess(email: String, nickname: String) {
        // 로그인 성공 후 데이터를 설정
        val myPageFragment = MyPageFragment.newInstance()
        myPageFragment.arguments = Bundle().apply {
            putString("email", email)
            putString("nickname", nickname)
        }
    }
}
