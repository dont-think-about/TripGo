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
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.nbcamp.tripgo.BuildConfig
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivityLogInBinding
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.main.MainActivity
import com.nbcamp.tripgo.view.signup.SignUpActivity

class LogInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var emailid: AppCompatEditText
    lateinit var emailpwd: AppCompatEditText
    lateinit var loginbtn: AppCompatButton
    lateinit var kakaoLoginButton: AppCompatImageView
    var fireStore: FirebaseFirestore = Firebase.firestore

    // firestore 연결
    val firestore = Firebase.firestore
    // firebaseUID 가져오기

    // google login
    private val binding by lazy { ActivityLogInBinding.inflate(layoutInflater) }
    private lateinit var GoogleSignInClient: GoogleSignInClient
    private lateinit var startGoogleLoginForResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Firebaseauth 로그인
        auth = FirebaseAuth.getInstance()
        emailid = findViewById(R.id.sign_up_email_edit_text)
        emailpwd = findViewById(R.id.log_in_password_edit_text)
        loginbtn = findViewById(R.id.log_in_login_button)

        // Firebase loginbtn click event
        loginbtn.setOnClickListener {
            effectiveness()
        }

        // google login 선언 시작~
        auth = Firebase.auth

        googleInit()

        val signInButton = findViewById<SignInButton>(R.id.log_in_google_login_button)

        signInButton.setOnClickListener {
            val signInIntent = GoogleSignInClient.signInIntent
            startGoogleLoginForResult.launch(signInIntent)
        }

        // kakao 선언 시작 ~
        /** KakaoSDK init */
        KakaoSdk.init(this, BuildConfig.KAKAO_API_KEY)
        Log.d("kakaoappkey", "kakaoappkey" + BuildConfig.KAKAO_API_KEY)

        kakaoLoginButton = findViewById(R.id.log_in_kakao_login_button)
        kakaoLoginButton.setOnClickListener {
            kakaoLogin()
        }
        val snackbarMessage = intent.getStringExtra("snackbarMessage")
        if (snackbarMessage != null) {
            Snackbar.make(binding.root, snackbarMessage, Snackbar.LENGTH_LONG).show()
        }
        passwordFind()
        signUp()
    }

    private fun effectiveness() {
        var email = emailid.text.toString()
        var password = emailpwd.text.toString()

        if (email.isNotBlank() && password.isNotBlank()) {
            login(email, password)
        } else if (email.isBlank()) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
        } else if (password.isBlank()) {
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Email 또는 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { authResult ->
                if (authResult.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        toast("이메일 인증을 완료해 주세요")
                    }
                } else {
                    Toast.makeText(this, "로그인 실패.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // https://jgeun97.tistory.com/233
    // https://github.com/firebase/snippets-android/blob/b8f65e9150fe927a5f0473e15e16fa5803189b60/auth/app/src/main/java/com/google/firebase/quickstart/auth/kotlin/GoogleSignInActivity.kt#L43-L44
    private fun googleInit() {
        val default_web_client_id =
            "1094795130006-9tkks7qfjnls7rtpijm4phspvurfscl0.apps.googleusercontent.com" // Android id X

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(default_web_client_id)
            .requestEmail()
            .build()

        GoogleSignInClient = GoogleSignIn.getClient(this, gso)

        startGoogleLoginForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.let { data ->

                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            val account = task.getResult(ApiException::class.java)!!
                            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)

                            // firestore google inpo 저장
                            val user = hashMapOf(
                                "email" to account.email,
                                "nickname" to account.displayName,
                                "profileImage" to null,
                                "reviewCount" to 0
                            )

                            fireStore.collection("users").document(account.email.toString())
                                .set(user)

                            // val keyHash = Utility.getKeyHash(this)
                            // Log.d("Hash", keyHash)

                            finish()

                            firebaseAuthWithGoogle(account.idToken!!)
                        } catch (e: ApiException) {
                            // Google Sign In failed, update UI appropriately
                            Log.w(TAG, "Google sign in failed", e)
                        }
                    }
                    // Google Login Success
                } else {
                    Log.e(TAG, "Google Result Error $result")
                }
            }
    }

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun kakaoLogin() {
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.d("카톡계정 로그인 실패 @@@@@@@@@", "카카오계정으로 로그인 실패 : $error")
                setLogin(false)
            } else if (token != null) {
                // TODO: 최종적으로 카카오로그인 및 유저정보 가져온 결과
                UserApiClient.instance.me { user, userError ->
                    if (userError != null) {
                        // 사용자 정보 가져오기에 실패한 경우, 에러 처리를 수행
                        Log.e("사용자 정보 가져오기 오류", userError.toString())
                    } else if (user != null) {
                        val email = user.kakaoAccount?.email
                        val nickname = user.kakaoAccount?.profile?.nickname

                        if (email != null) {
                            val userDocument = hashMapOf(
                                "email" to email,
                                "nickname" to nickname,
                                "profileImage" to null,
                                "reviewCount" to 0
                            )
                            fireStore.collection("users").document(email)
                                .set(userDocument)
                                .addOnSuccessListener {
                                    Log.d(ContentValues.TAG, "사용자 정보 Firestore에 저장 성공")
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }
                                .addOnFailureListener { e ->
                                    Log.w(ContentValues.TAG, "사용자 정보 Firestore에 저장 실패", e)
                                }
                        }
                    }
                }
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun kakaoLogout() {
        // 로그아웃
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.d("this", "로그아웃 실패. SDK에서 토큰 삭제됨: $error")
            } else {
                Log.d("this", "로그아웃 성공. SDK에서 토큰 삭제됨")
                setLogin(false)
            }
        }
    }

    private fun kakaoUnlink() {
        // 연결 끊기
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Log.d("this", "연결 끊기 실패: $error")
            } else {
                Log.d("this", "연결 끊기 성공. SDK에서 토큰 삭제 됨")
                setLogin(false)
            }
        }
    }

    private fun setLogin(bool: Boolean) {
        kakaoLoginButton.visibility = if (bool) View.GONE else View.VISIBLE
    }

    private fun passwordFind() {
        binding.logInFindPasswordTextView.setOnClickListener {
            val fragment = PasswordFindFragment()
            fragment.show(supportFragmentManager, null)
        }
    }

    private fun signUp() {
        binding.logInSignUpTextView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
