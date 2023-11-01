package com.nbcamp.tripgo.view.signup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivitySignUpBinding
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.login.LogInActivity

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    private val signUpViewModel: SignUpViewModel by viewModels()

    private var emailCheck = false
    private var nicknameCheck = false
    private var agreeCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        binding.viewModel = signUpViewModel
        binding.lifecycleOwner = this
        initViewModel()
        initView()
        editTextMemberInformation()
    }

    private fun initView() = binding.apply {
        signUpFullAgreementCheckBox.setOnClickListener {
            if (signUpFullAgreementCheckBox.isChecked) {
                signUpAgeLimitCheckBox.isChecked = true
                signUpAppAlarmCheckBox.isChecked = true
                signUpPrivacyPolicyCheckBox.isChecked = true
                signUpTermsOfUseCheckBox.isChecked = true
                signUpEssentialAgreementErrorTextView.visibility = View.GONE
                signUpTermsAndConditionsAgreementLayout.setBackgroundResource(R.drawable.background_edit_text_correct)
                agreeCheck = true
            } else {
                signUpAgeLimitCheckBox.isChecked = false
                signUpAppAlarmCheckBox.isChecked = false
                signUpPrivacyPolicyCheckBox.isChecked = false
                signUpTermsOfUseCheckBox.isChecked = false
                signUpEssentialAgreementErrorTextView.visibility = View.VISIBLE
                signUpTermsAndConditionsAgreementLayout.setBackgroundResource(R.drawable.background_edit_text_error)
                agreeCheck = false
            }
            signUpSignUpCompleteButton.isEnabled =
                emailCheck && nicknameCheck && agreeCheck && signUpPasswordEditText.text.toString().isNotEmpty() && signUpCorrectPasswordEditText.text.toString().isNotEmpty()
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

        signUpEmailAuthButton.setOnClickListener {
            signUpViewModel.checkEmailDuplication(signUpEmailEditText.text.toString())


        }
        signUpNickNameAuthButton.setOnClickListener {
            signUpViewModel.checkNickNameDuplication(signUpNickNameEditText.text.toString())
        }

    }

    private fun initViewModel() {

        signUpViewModel.signUpButton.observe(this) {
            if (it) {
                finish()
                startActivity(Intent(this, LogInActivity::class.java))
            }
        }

        signUpViewModel.isEmailRegistered.observe(this) {
            if (it == false) {
                binding.signUpEmailLayout.setBackgroundResource(R.drawable.background_edit_text_error)
                binding.signUpEmailErrorDuplicationTextView.visibility = View.VISIBLE
                emailCheck = false

            } else {
                binding.signUpEmailLayout.setBackgroundResource(R.drawable.background_edit_text_correct)
                binding.signUpEmailErrorDuplicationTextView.visibility = View.GONE
                emailCheck = true
            }
            binding.signUpEmailEditText.setOnKeyListener(
                View.OnKeyListener { v, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        binding.signUpEmailErrorDuplicationTextView.visibility = View.GONE
                    }
                    false
                }
            )
        }

        signUpViewModel.isNickNameRegistered.observe(this) {
            if (it == false) {
                binding.signUpNickNameLayout.setBackgroundResource(R.drawable.background_edit_text_error)
                binding.signUpNickNameErrorTextView.visibility = View.VISIBLE
                nicknameCheck = false
            } else {
                binding.signUpNickNameLayout.setBackgroundResource(R.drawable.background_edit_text_correct)
                binding.signUpNickNameErrorTextView.visibility = View.GONE
                nicknameCheck = true
            }
            binding.signUpNickNameEditText.setOnKeyListener(
                View.OnKeyListener { v, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        binding.signUpNickNameLayout.setBackgroundResource(R.drawable.background_edit_text)
                        binding.signUpNickNameErrorTextView.visibility = View.GONE
                    }
                    false
                }
            )
        }
    }

    private fun checkBoxEssentialChecked() = binding.apply {
        if (signUpAgeLimitCheckBox.isChecked && signUpPrivacyPolicyCheckBox.isChecked && signUpTermsOfUseCheckBox.isChecked) {
            signUpEssentialAgreementErrorTextView.visibility = View.GONE
            signUpTermsAndConditionsAgreementLayout.setBackgroundResource(R.drawable.background_edit_text_correct)
            agreeCheck = true
        } else {
            binding.signUpEssentialAgreementErrorTextView.visibility = View.VISIBLE
            signUpTermsAndConditionsAgreementLayout.setBackgroundResource(R.drawable.background_edit_text_error)
            agreeCheck = false
        }
        signUpSignUpCompleteButton.isEnabled = emailCheck && nicknameCheck && agreeCheck

    }

    private fun editTextMemberInformation() = binding.apply {

        signUpEmailEditText.addValidation(
            validation = {
                it.matches(
                    Regex("[0-9a-zA-Z]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$")
                )
            },
            errorText = {
                signUpEmailErrorTextView
            },
            background = {
                signUpEmailLayout
            }
        )

        signUpPasswordEditText.addValidation(
            validation = {
                it.matches(
                    Regex("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]{8,20}$")
                )
            },
            errorText = {
                signUpPasswordErrorTextView
            },
            background = {
                signUpPasswordLayout
            }
        )

        signUpCorrectPasswordEditText.addValidation(
            validation = {
                val password = signUpPasswordEditText.text.toString()
                it.isEmpty() || it == password
            },
            errorText = {
                signUpCorrectPasswordErrorTextView
            },
            background = {
                signUpCorrectPasswordLayout
            }
        )

    }

    private fun EditText.addValidation(
        validation: (String) -> Boolean,
        errorText: () -> AppCompatTextView,
        background: () -> LinearLayout
    ) {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (validation(text.toString())) {
                    hideError(errorText, background)
                } else {
                    showError(errorText, background)
                }
            }
        })
    }

    private fun showError(errorText: () -> AppCompatTextView, background: () -> LinearLayout) {
        errorText().visibility = View.VISIBLE
        background().setBackgroundResource(R.drawable.background_edit_text_error)

    }

    private fun hideError(errorText: () -> AppCompatTextView, background: () -> LinearLayout) {
        errorText().visibility = View.GONE
        background().setBackgroundResource(R.drawable.background_edit_text)
    }
}
