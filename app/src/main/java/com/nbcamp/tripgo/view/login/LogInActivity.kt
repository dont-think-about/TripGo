package com.nbcamp.tripgo.view.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.google.firebase.auth.FirebaseAuth
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.view.main.MainActivity

class LogInActivity : AppCompatActivity() {

    lateinit var auth : FirebaseAuth
    lateinit var emailid: AppCompatEditText
    lateinit var emailpwd: AppCompatEditText
    lateinit var loginbtn: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)



        //firebaseauth 로그인

        auth = FirebaseAuth.getInstance()
        emailid = findViewById(R.id.sign_up_email_edit_text)
        emailpwd = findViewById(R.id.log_in_password_edit_text)
        loginbtn = findViewById(R.id.log_in_login_button)

        loginbtn.setOnClickListener{
            var email = emailid.text.toString()
            var password = emailpwd.text.toString()

            if( email.isNotBlank() && password.isNotBlank()) {

                login(email, password)
            }

            else {
                if (email.isBlank()){
                    Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
                if (password.isBlank())
                {
                    Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    fun login(email:String,password:String){
        auth.signInWithEmailAndPassword(email,password) // 로그인
            .addOnCompleteListener {
                    result->
                if(result.isSuccessful){
                    var intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else{
                    Toast.makeText(this, "로그인 실패.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
