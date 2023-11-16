package com.nbcamp.tripgo.view.mypage

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.user.UserApiClient
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.login.LogInActivity
import com.nbcamp.tripgo.view.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileModifyFragment : Fragment() {
    private lateinit var refreshNickText: AppCompatEditText
    private lateinit var loadingDialog: LoadingDialog
    private var selectedImageUri: Uri? = null // 이미지 URI를 저장할 변수
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            val imageView = view?.findViewById<ImageView>(R.id.profile_edit_user_imageview)
            imageView?.setImageURI(selectedImageUri)
        }
    }
    private val profileModifyViewModel: ProfileModifyViewModel by viewModels()

    @SuppressLint("MissingInflatedId")
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
        backButton.setOnClickListener { navigateToMyPageFragment() }

        val imageButton = view?.findViewById<ImageView>(R.id.profile_edit_user_imageview)
        imageButton?.setOnClickListener { changeImage() }

        val logoutButton = view?.findViewById<AppCompatButton>(R.id.profile_modify_logout_button)
        logoutButton?.setOnClickListener { login() }

        val deleteUserButton = view.findViewById<TextView>(R.id.profile_modify_withdrawal_textivew)
        deleteUserButton.setOnClickListener { withDrawlUser() }

        imageUpdate()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
    }

    private fun initViewModel() {
        profileModifyViewModel.deleteStatus.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UserDeleteStatus.Initialize -> {
                    loadingDialog.setVisible()
                }

                is UserDeleteStatus.Error -> {
                    loadingDialog.setInvisible()
                    requireActivity().toast("회원 탈퇴에 실패하였습니다.")
                }

                is UserDeleteStatus.Success -> {
                    loadingDialog.setVisible()
                    loadingDialog.setText(state.message)
                    if (state.message == "모든 정보 삭제 완료") {
                        loadingDialog.setInvisible()
                        requireActivity().toast("회원 탈퇴가 완료되었습니다.")
                        startActivity(
                            Intent(
                                requireActivity(),
                                MainActivity::class.java
                            ).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun withDrawlUser() {
        AlertDialog.Builder(requireActivity())
            .setTitle("회원 탈퇴")
            .setMessage("회원 탈퇴를 하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                profileModifyViewModel.withDrawlUser()
            }.setNegativeButton("아니오") { _, _ -> }
            .create()
            .show()
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
        loadingDialog.setVisible()
        loadingDialog.setText("수정 중..")
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val firestore = FirebaseFirestore.getInstance()
        val userId = userEmail()
        Log.d("ProfileModify", userId + user)
        val userRef = firestore.collection("users").document(userId)

        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    checkIfNicknameExists(editNickname) { nicknameExists ->
                        if (!nicknameExists) {
                            val data = hashMapOf("nickname" to editNickname)

                            // 이미지를 Firebase Storage에 업로드하고 업로드가 완료되면 Firestore에 이미지 URL을 업데이트합니다.
                            uploadImageToFirebaseStorage(selectedImageUri) { imageUrl ->
                                if (imageUrl.isNotEmpty()) {
                                    // 리뷰의 유저 정보도 함께 바꿈
                                    profileModifyViewModel.updateReviewNickName(userId, imageUrl, editNickname)
                                    data["profileImage"] = imageUrl // 이미지 URL을 데이터에 추가
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
        val kakaoUser = App.kakaoUser != null

        return if (kakaoUser) {
            App.kakaoUser?.email ?: ""
        } else {
            // 일반 Firebase 로그인일 경우
            val user = auth.currentUser
            user?.email ?: ""
        }
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
        loadingDialog.setInvisible()
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

        uploadTask.addOnSuccessListener { _ ->
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
                        modifyImageView?.load(profileImageUrl) {
                            transformations(CircleCropTransformation())
                        }
                    }
                }
            }
        }
    }

    private fun login() {

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
                        updateUIAfterLogout()
                    }
                }
            } else {
                // Firebase Auth 로그아웃
                FirebaseAuth.getInstance().signOut()
                App.firebaseUser = null
                modifyToFragment(MyPageFragment())
                // 로그아웃 후 화면 갱신
                updateUIAfterLogout()
                // 로그아웃버튼클릭시 firestore 불러온정보 클리어
                clearFirestoreUserData()
            }
        } else {
            // 사용자가 로그인하지 않은 상태이면 로그인 화면으로 이동
            val intent = Intent(requireContext(), LogInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun clearFirestoreUserData() {
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.email

        if (userId != null) {
            val userDocumentRef = firestore.collection("users").document(userId)
            userDocumentRef.delete().addOnSuccessListener {
                Log.d("ProFileModify", "Success")
            }
                .addOnFailureListener { _ ->
                    Log.w("ProFileModify", "Fail")
                }
        }
    }

    private fun updateUIAfterLogout() {
        val emailText = view?.findViewById<TextView>(R.id.mypage_signin_up_inpo)
        val nicknameText = view?.findViewById<TextView>(R.id.mypage_signin_up_text)
        nicknameText?.text = getString(R.string.mypage_signin_up)
        emailText?.text = getString(R.string.mypage_signin_up_inpor)
        loading()
    }

    private fun loading() {
        loadingDialog.run {
            setVisible()
            setText("로딩중 ... ")
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500)
            loadingDialog.setInvisible()
        }
    }

    private fun modifyToFragment(fragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 123
    }
}
