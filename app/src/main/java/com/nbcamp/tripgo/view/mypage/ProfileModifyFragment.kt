package com.nbcamp.tripgo.view.mypage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nbcamp.tripgo.R
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.load
import coil.transform.CircleCropTransformation
import com.kakao.sdk.user.UserApiClient
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.login.LogInActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.util.UUID

class ProfileModifyFragment : Fragment() {
    private lateinit var refreshNickText: AppCompatEditText
    private lateinit var loadingDialog: LoadingDialog
    private var selectedImageUri: Uri? = null  // 이미지 URI를 저장할 변수
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            val imageView = view?.findViewById<ImageView>(R.id.profile_edit_user_imageview)
            imageView?.setImageURI(selectedImageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        loadingDialog = LoadingDialog(requireActivity())

        val view = inflater.inflate(R.layout.fragment_profile_modify, container, false)
        refreshNickText = view.findViewById(R.id.profile_edit_username)

        val saveButton = view.findViewById<AppCompatImageView>(R.id.profile_edit_complete_textivew)
        saveButton.setOnClickListener { updateProfile() }

        val backButton = view.findViewById<AppCompatImageView>(R.id.profile_back_imagebutton)
        backButton.setOnClickListener{ navigateToMyPageFragment() }

        val imageButton = view?.findViewById<ImageView>(R.id.profile_edit_user_imageview)
        imageButton?.setOnClickListener { changeImage() }

        val logoutButton = view?.findViewById<AppCompatButton>(R.id.profile_modify_logout_button)
        logoutButton?.setOnClickListener { login() }

        imageUpdate()

        return view
    }
    private fun updateProfile() {
        val editNickname = refreshNickText.text.toString()
        if (editNickname.isEmpty()) {
            showToast("닉네임을 입력하세요.")
            return
        }
        if (selectedImageUri == null) {
            showToast("프로필 이미지를 선택하세요.")
            return
        }

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

                            // 이미지를 Firebase Storage에 업로드하고 업로드가 완료되면 Firestore에 이미지 URL을 업데이트합니다.
                            uploadImageToFirebaseStorage(selectedImageUri) { imageUrl ->
                                if (imageUrl.isNotEmpty()) {
                                    data["profileImage"] = imageUrl  // 이미지 URL을 데이터에 추가
                                    userRef.update(data as Map<String, Any>).addOnSuccessListener {
                                        showToast("프로필이 업데이트되었습니다.")
                                        navigateToMyPageFragment()
                                    }.addOnFailureListener {
                                        showToast("프로필 업데이트에 실패했습니다.")
                                    }
                                } else {
                                    showToast("프로필 이미지 업데이트에 실패했습니다.")
                                }
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

    private fun changeImage() {
        galleryLauncher.launch("image/*")
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri?, callback: (String) -> Unit) {
        val userId = userEmail()
        val storagePath = "users/$userId/profileImage/${UUID.randomUUID()}.jpg"
        val storageReference = FirebaseStorage.getInstance().getReference(storagePath)

        val uploadTask = storageReference.putFile(imageUri!!)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                callback(imageUrl)
            }
        }.addOnFailureListener { exception ->
            Log.e("ProfileModifyFragment", "Storage에 업로드 오류 : $exception")
            callback("")
        }
    }

    private fun imageUpdate() {
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

                        val modifyImageView = view?.findViewById<ImageView>(R.id.profile_edit_user_imageview)

                        Log.d("MYpageurl", profileImageUrl)

                        modifyImageView?.load(profileImageUrl){
                            transformations(CircleCropTransformation())
                        }
                    }
                }
            }
        }
    }

    private fun login() {

        val loginbutton = view?.findViewById<Button>(R.id.mypage_login_button)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val kakaouser = App.kakaoUser?.email


        if (user != null || kakaouser != null) {
            // 사용자가 로그인한 상태이면 로그아웃 처리

            if (kakaouser != null) {
                // 카카오 로그아웃
                UserApiClient.instance.logout { error ->
                    if (error != null) {
                        Log.e("MYPAGEFRAGMENT", "I'm kakao user but failed : $error")
                    } else {
                        App.kakaoUser = null
                        // 로그아웃 후 화면 갱신
                        modifyToFragment(MyPageFragment())
                        loginbutton?.text = "로그인"
                        updateUIAfterLogout()
                    }
                }
            } else {
                // Firebase Auth 로그아웃
                FirebaseAuth.getInstance().signOut()
                App.firebaseUser = null
                modifyToFragment(MyPageFragment())
                // 로그아웃 후 화면 갱신
                loginbutton?.text = "로그인"
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
        val logoutbutton = view?.findViewById<Button>(R.id.mypage_login_button)

        nicknameText?.text = getString(R.string.mypage_signin_up)
        emailText?.text = getString(R.string.mypage_signin_up_inpor)
        logoutbutton?.text = getString(R.string.mypage_logout)

        loading()
    }

    private fun loading(){

        loadingDialog.run {
            setVisible()
            setText("로딩중 ... ")
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(1500)
            loadingDialog.hide()
        }

    }
    private fun modifyToFragment(fragment: Fragment){
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 123
    }
}
