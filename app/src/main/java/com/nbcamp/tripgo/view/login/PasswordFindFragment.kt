package com.nbcamp.tripgo.view.login

import android.app.Dialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.DialogPasswordFindBinding
import com.nbcamp.tripgo.util.extension.ContextExtension.toast

class PasswordFindFragment : DialogFragment() {
    private var _binding: DialogPasswordFindBinding? = null
    private val binding get() = _binding!!
    var firebaseAuth: FirebaseAuth = Firebase.auth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Dialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogPasswordFindBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
    }
    fun initViewModel() = binding.apply{
        logInLoginButton.setOnClickListener {
            firebaseAuth.sendPasswordResetEmail(passwordFindEmailEditText.text.toString()).addOnCompleteListener {
                if (it.isSuccessful) {
                    requireActivity().toast("비밀번호를 초기화 하였습니다. 이메일에 작성된 링크로 비밀번호를 재설정 하세요")
                    dismiss()
                }
                else{
                    requireActivity().toast("가입된 이메일이 없습니다. 이메일을 다시 확인해주세요")
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}