package com.nbcamp.tripgo.view.signup

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivitySignUpBinding
import com.nbcamp.tripgo.view.login.LogInActivity

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    private val signUpViewModel: SignUpViewModel by viewModels()

    private var emailCheck = false
    var pwCheck = false
    var pwRepeatCheck = false
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
        rules()
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
            binding.signUpSignUpCompleteButton.isEnabled =
                emailCheck && pwCheck && pwRepeatCheck && nicknameCheck and agreeCheck
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
        signUpAppAlarmCheckBox.setOnClickListener {
            checkBoxEssentialChecked()
        }

        signUpEmailAuthButton.setOnClickListener {
            signUpViewModel.checkEmailDuplication(signUpEmailEditText.text.toString())
        }
        signUpNickNameAuthButton.setOnClickListener {
            signUpViewModel.checkNickNameDuplication(signUpNickNameEditText.text.toString())
        }
        btnBack.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, LogInActivity::class.java))
            finish()
        }
    }

    private fun initViewModel() {

        signUpViewModel.signUpButton.observe(this) {
            if (it) {
                val snackbarText = "작성하신 이메일로 인증 주소를 보냈습니다.\n가입 인증을 진행해주세요"
                startActivity(Intent(this, LogInActivity::class.java).putExtra("snackbarMessage", snackbarText))
                finish()
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
                View.OnKeyListener { _, keyCode, _ ->
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
                View.OnKeyListener { _, keyCode, _ ->
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
            signUpFullAgreementCheckBox.isChecked = false
            binding.signUpSignUpCompleteButton.isEnabled =
                emailCheck && pwCheck && pwRepeatCheck && nicknameCheck and agreeCheck
            signUpFullAgreementCheckBox.isChecked = signUpAppAlarmCheckBox.isChecked
        } else {
            signUpFullAgreementCheckBox.isChecked = false
            binding.signUpEssentialAgreementErrorTextView.visibility = View.VISIBLE
            signUpTermsAndConditionsAgreementLayout.setBackgroundResource(R.drawable.background_edit_text_error)
            agreeCheck = false
        }
        signUpSignUpCompleteButton.isEnabled = emailCheck && nicknameCheck && agreeCheck
    }

    private fun editTextMemberInformation() = binding.apply {

        signUpEmailEditText.doOnTextChanged { _, _, _, _ ->
            val emailTextCheck = signUpEmailEditText.text.toString().matches(
                Regex("[0-9a-zA-Z]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$")
            )
            if (!emailTextCheck) {
                signUpEmailErrorTextView.visibility = View.VISIBLE
                signUpEmailLayout.setBackgroundResource(R.drawable.background_edit_text_error)
                emailCheck = false
                signUpSignUpCompleteButton.isEnabled = false
            } else {
                signUpEmailErrorTextView.visibility = View.GONE
                signUpEmailLayout.setBackgroundResource(R.drawable.background_edit_text)
                emailCheck = true
                binding.signUpSignUpCompleteButton.isEnabled =
                    emailCheck && pwCheck && pwRepeatCheck && nicknameCheck and agreeCheck
            }
        }

        signUpPasswordEditText.doOnTextChanged { _, _, _, _ ->
            val passwordTextCheck = signUpPasswordEditText.text.toString().matches(
                Regex("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]{8,20}$")
            )
            if (!passwordTextCheck) {
                signUpPasswordErrorTextView.visibility = View.VISIBLE
                signUpPasswordLayout.setBackgroundResource(R.drawable.background_edit_text_error)
                pwCheck = false
                signUpSignUpCompleteButton.isEnabled = false
            } else {
                signUpPasswordErrorTextView.visibility = View.GONE
                signUpPasswordLayout.setBackgroundResource(R.drawable.background_edit_text_correct)
                pwCheck = true
                binding.signUpSignUpCompleteButton.isEnabled =
                    emailCheck && pwCheck && pwRepeatCheck && nicknameCheck and agreeCheck
            }
        }

        signUpCorrectPasswordEditText.doOnTextChanged { _, _, _, _ ->
            val passwordCorrectTextCheck = signUpPasswordEditText.text.toString()

            if (passwordCorrectTextCheck == signUpCorrectPasswordEditText.text.toString()) {
                signUpCorrectPasswordErrorTextView.visibility = View.GONE
                signUpCorrectPasswordLayout.setBackgroundResource(R.drawable.background_edit_text_correct)
                pwRepeatCheck = true
                binding.signUpSignUpCompleteButton.isEnabled =
                    emailCheck && pwCheck && pwRepeatCheck && nicknameCheck and agreeCheck
            } else {
                signUpCorrectPasswordErrorTextView.visibility = View.VISIBLE
                signUpCorrectPasswordLayout.setBackgroundResource(R.drawable.background_edit_text_error)
                pwRepeatCheck = false
                signUpSignUpCompleteButton.isEnabled = false
            }
        }
    }

    private fun rules() = binding.apply {
        signUpRulesTermsOfUseTextView.setOnClickListener {
            showFullDialog("terms")
        }
        signUpRulesPrivacyPolicyTextView.setOnClickListener {
            showFullDialog("privacy")
        }
    }

    fun showFullDialog(data: String) {
        val dialogFragment = RulesFragment()
        val args = Bundle()
        args.putString("data", data)
        dialogFragment.arguments = args
        dialogFragment.show(supportFragmentManager, null)
    }
}