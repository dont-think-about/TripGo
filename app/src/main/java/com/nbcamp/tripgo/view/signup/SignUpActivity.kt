package com.nbcamp.tripgo.view.signup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivitySignUpBinding
import com.nbcamp.tripgo.view.login.LogInActivity
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    private val signUpViewModel: SignUpViewModel by viewModels()

    private var emailCheck = false
    private var passwordCheck = false
    private var passwordRepeatCheck = false
    private var nicknameCheck = false
    private var agreeCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        binding.viewModel = signUpViewModel
        binding.lifecycleOwner = this
        initViewModel()
        initView()
//        editTextMemberInformation()
    }

    private fun initView() = binding.apply {
        signUpFullAgreementCheckBox.setOnClickListener {
            if (signUpFullAgreementCheckBox.isChecked) {
                signUpAgeLimitCheckBox.isChecked = true
                signUpAppAlarmCheckBox.isChecked = true
                signUpPrivacyPolicyCheckBox.isChecked = true
                signUpTermsOfUseCheckBox.isChecked = true
                signUpEssentialAgreementErrorTextView.visibility = View.GONE
                signUpTermsAndConditionsAgreementLayout.setBackgroundResource(R.drawable.background_edit_text)
                agreeCheck = true
                checkBoxAllChecked()
            } else {
                signUpAgeLimitCheckBox.isChecked = false
                signUpAppAlarmCheckBox.isChecked = false
                signUpPrivacyPolicyCheckBox.isChecked = false
                signUpTermsOfUseCheckBox.isChecked = false
                signUpEssentialAgreementErrorTextView.visibility = View.VISIBLE
                signUpTermsAndConditionsAgreementLayout.setBackgroundResource(R.drawable.background_edit_text_error)
                agreeCheck = false
//                binding.signUpSignUpCompleteButton.isEnabled = false
            }
        }

        signUpAgeLimitCheckBox.setOnClickListener {
            checkBoxEssentialChecked()
        }

        signUpPrivacyPolicyCheckBox.setOnClickListener {
            checkBoxEssentialChecked()
        }
        signUpTermsOfUseCheckBox.setOnClickListener {
            checkBoxEssentialChecked()
        }
    }

    private fun initViewModel() {

        signUpViewModel.signUpButton.observe(this) {
            if (it) {
                finish()
                startActivity(Intent(this, LogInActivity::class.java))
            }
        }
    }

    private fun editTextMemberInformation() = binding.apply {

        val editTexts = arrayOf(
            signUpEmailEditText,
            signUpPasswordEditText,
            signUpCorrectPasswordEditText,
            signUpNickNameEditText
        )

        val errorViews = arrayOf(
            signUpEmailErrorTextView,
            signUpPasswordErrorTextView,
            signUpCorrectPasswordErrorTextView,
            signUpNickNameErrorTextView
        )

        val bgViews = arrayOf(
            signUpEmailLayout,
            signUpPasswordLayout,
            signUpCorrectPasswordLayout,
            signUpNickNameLayout
        )

        fun EditText.showError() {
            val index = editTexts.indexOf(this)
            errorViews[index].visibility = View.VISIBLE
            bgViews[index].setBackgroundResource(R.drawable.background_edit_text_error)

        }

        fun EditText.hideError() {
            val index = editTexts.indexOf(this)
            errorViews[index].visibility = View.GONE
            bgViews[index].setBackgroundResource(R.drawable.background_edit_text)

        }

        fun EditText.addValidation(validation: (String) -> Boolean) {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (validation(text.toString())) {
                        hideError()
                    } else {
                        showError()
                    }
                }
            }
            )
        }

        signUpEmailEditText.addValidation { email ->
            email.matches(Regex("[0-9a-zA-Z]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$"))

        }

        signUpPasswordEditText.addValidation { password ->
            password.matches(
                Regex("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]{8,20}$")
            )
        }

        signUpCorrectPasswordEditText.addValidation { passwordRepeat ->
            val password = signUpPasswordEditText.text.toString()
            passwordRepeat.isEmpty() || passwordRepeat == password
        }

        signUpNickNameEditText.addValidation { nickname ->
            nickname.length in 2..15
        }
    }

    private fun checkBoxEssentialChecked() = binding.apply {

        if (signUpAgeLimitCheckBox.isChecked && signUpPrivacyPolicyCheckBox.isChecked && signUpTermsOfUseCheckBox.isChecked) {
            signUpEssentialAgreementErrorTextView.visibility = View.GONE
            signUpTermsAndConditionsAgreementLayout.setBackgroundResource(R.drawable.background_edit_text)
            agreeCheck = true
            checkBoxAllChecked()
        } else {
            binding.signUpEssentialAgreementErrorTextView.visibility = View.VISIBLE
            signUpTermsAndConditionsAgreementLayout.setBackgroundResource(R.drawable.background_edit_text_error)
            agreeCheck = false
//            binding.signUpSignUpCompleteButton.isEnabled = false
        }
    }

    private fun checkBoxAllChecked() = binding.apply {
        if (emailCheck && passwordCheck && passwordRepeatCheck && nicknameCheck && agreeCheck) {
            signUpSignUpCompleteButton.isEnabled = true
            signUpSignUpCompleteButton.setBackgroundResource(R.color.main)
        } else {
//            signUpSignUpCompleteButton.isEnabled = false
        }
    }

    fun text() {
        binding.signUpEmailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                val ps: Pattern =
                    Pattern.compile("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$")

                if (!ps.matcher(binding.signUpEmailEditText.text.toString()).matches()) {
                    if (binding.signUpEmailEditText.text.toString().isEmpty()) {
                        binding.signUpEmailErrorTextView.visibility = View.VISIBLE
                        binding.signUpEmailLayout.setBackgroundResource(R.drawable.background_edit_text_error)
                        emailCheck = false
                        binding.signUpSignUpCompleteButton.isEnabled = false
                    } else {
                        binding.signUpEmailErrorTextView.visibility = View.VISIBLE
                        binding.signUpEmailLayout.setBackgroundResource(R.drawable.background_edit_text_error)
                        emailCheck = false
                        binding.signUpSignUpCompleteButton.isEnabled = false
                    }
                } else {
                    binding.signUpEmailErrorTextView.visibility = View.GONE
                    binding.signUpEmailLayout.setBackgroundResource(R.drawable.background_edit_text)
                    emailCheck = true
                    if (emailCheck and passwordCheck and passwordRepeatCheck and nicknameCheck and agreeCheck) {
                        binding.signUpSignUpCompleteButton.isEnabled = true
                        binding.signUpSignUpCompleteButton.setBackgroundResource(R.color.main)
                    } else {
                        binding.signUpSignUpCompleteButton.isEnabled = false
                    }
                }
            }
        })

    }
}