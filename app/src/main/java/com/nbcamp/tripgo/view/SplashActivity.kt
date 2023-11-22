package com.nbcamp.tripgo.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivitySplashBinding
import com.nbcamp.tripgo.view.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val animation = binding.splashLottie
        animation.run {
            setAnimation("lottie_5.json")
            speed = 4f
            playAnimation()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up_enter, R.anim.slide_down_exit)
        }, 2500)
    }
}
